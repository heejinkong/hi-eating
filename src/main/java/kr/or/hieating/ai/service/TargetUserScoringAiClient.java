package kr.or.hieating.ai.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.or.hieating.ai.config.AiProperties;
import kr.or.hieating.ai.dto.HotDealTargetInfoDto;
import kr.or.hieating.ai.dto.UserProfileDto;
import kr.or.hieating.ai.dto.UserScoreDto;
import kr.or.hieating.ai.prompt.TargetSelectionPromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class TargetUserScoringAiClient {

  private final ChatClient chatClient;
  private final TargetSelectionPromptBuilder promptBuilder;
  private final TargetScoreResponseParser responseParser;
  private final int retryCount;

  public TargetUserScoringAiClient(
      // 사용자 적합도 판정은 temperature 0.2의 검증용 모델을 사용해 결과 편차를 줄인다.
      @Qualifier("emailValidationChatClient") ChatClient chatClient,
      TargetSelectionPromptBuilder promptBuilder,
      TargetScoreResponseParser responseParser,
      AiProperties properties) {
    this.chatClient = chatClient;
    this.promptBuilder = promptBuilder;
    this.responseParser = responseParser;
    this.retryCount = properties.targetSelection().retryCount();
  }

  public List<UserScoreDto> score(HotDealTargetInfoDto hotDeal, List<UserProfileDto> userProfiles) {
    try {
      return scoreBatch(hotDeal, userProfiles);
    } catch (RuntimeException exception) {
      if (userProfiles.size() <= 1 || !isAiResponseFormatFailure(exception)) {
        throw exception;
      }

      int middle = userProfiles.size() / 2;
      List<UserProfileDto> left = userProfiles.subList(0, middle);
      List<UserProfileDto> right = userProfiles.subList(middle, userProfiles.size());
      log.warn(
          "[대상선정] 배치 평가 실패로 분할 재시도합니다. size={} left={} right={}",
          userProfiles.size(),
          left.size(),
          right.size());

      List<UserScoreDto> scores = new ArrayList<>(userProfiles.size());
      scores.addAll(score(hotDeal, left));
      scores.addAll(score(hotDeal, right));
      return scores;
    }
  }

  private List<UserScoreDto> scoreBatch(
      HotDealTargetInfoDto hotDeal, List<UserProfileDto> userProfiles) {
    String userPrompt = promptBuilder.build(hotDeal, userProfiles);
    RuntimeException lastFailure = null;

    // 호출 실패뿐 아니라 JSON 파싱 실패도 재시도
    for (int attempt = 0; attempt <= retryCount; attempt++) {
      try {
        String response =
            chatClient
                .prompt()
                .system(promptBuilder.systemPrompt())
                .user(userPrompt)
                .options(
                    OllamaChatOptions.builder()
                        .format(scoreSchema(userProfiles))
                        .numPredict(512)
                        .build())
                .call()
                .content();
        Map<Long, Integer> scoreValues = responseParser.parseScoreMap(response);
        List<UserScoreDto> scores = toScores(userProfiles, scoreValues);
        validateCompleteResponse(userProfiles, scores);
        return scores;
      } catch (RuntimeException exception) {
        lastFailure = exception;
        log.warn(
            "[대상선정] AI 배치 평가 실패 type={} attempt={}/{} userIds={}",
            failureType(exception),
            attempt + 1,
            retryCount + 1,
            userProfiles.stream().map(UserProfileDto::userId).toList(),
            exception);
      }
    }

    String lastMessage =
        lastFailure == null || lastFailure.getMessage() == null
            ? "원인을 확인할 수 없습니다."
            : lastFailure.getMessage();

    if (userProfiles.size() == 1 && isAiResponseFormatFailure(lastFailure)) {
      UserProfileDto profile = userProfiles.get(0);
      log.error(
          "[대상선정] Ollama 응답 형식 재시도 실패로 활동 정책 fallback을 적용합니다. userId={} cause={}",
          profile.userId(),
          lastMessage);
      return List.of(
          new UserScoreDto(
              profile.userId(), 50, "Ollama 응답 형식 오류로 AI 해석을 사용할 수 없어 사용자 활동 기반 점수 정책을 적용했습니다."));
    }

    throw new IllegalStateException("AI 대상 선정 재시도 횟수를 초과했습니다. 마지막 오류: " + lastMessage, lastFailure);
  }

  private Map<String, Object> scoreSchema(List<UserProfileDto> userProfiles) {
    Map<String, Object> properties = new LinkedHashMap<>();
    List<String> required = new ArrayList<>();
    for (UserProfileDto profile : userProfiles) {
      String userId = String.valueOf(profile.userId());
      properties.put(userId, Map.of("type", "integer", "minimum", 0, "maximum", 100));
      required.add(userId);
    }
    return Map.of(
        "type",
        "object",
        "properties",
        properties,
        "required",
        required,
        "additionalProperties",
        false);
  }

  private List<UserScoreDto> toScores(
      List<UserProfileDto> profiles, Map<Long, Integer> scoreValues) {
    List<UserScoreDto> scores = new ArrayList<>(profiles.size());
    for (UserProfileDto profile : profiles) {
      Integer score = scoreValues.get(profile.userId());
      if (score == null) {
        continue;
      }
      scores.add(
          new UserScoreDto(profile.userId(), score, "활동 정보를 종합한 AI 적합도 %d점".formatted(score)));
    }
    return scores;
  }

  private boolean isAiResponseFormatFailure(Throwable failure) {
    Throwable current = failure;
    while (current != null) {
      String message = current.getMessage();
      if (message != null && message.startsWith("AI 대상 선정 응답")) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  private String failureType(Throwable failure) {
    if (isAiResponseFormatFailure(failure)) {
      return "RESPONSE_FORMAT";
    }
    Throwable current = failure;
    while (current != null) {
      if (current instanceof java.net.SocketTimeoutException) {
        return "TIMEOUT";
      }
      current = current.getCause();
    }
    return "REMOTE_CALL";
  }

  private void validateCompleteResponse(
      List<UserProfileDto> userProfiles, List<UserScoreDto> scores) {
    Set<Long> expectedIds = new HashSet<>();
    userProfiles.forEach(profile -> expectedIds.add(profile.userId()));

    Set<Long> responseIds = new HashSet<>();
    for (UserScoreDto score : scores) {
      if (score == null
          || score.userId() == null
          || score.score() == null
          || score.score() < 0
          || score.score() > 100
          || score.reason() == null
          || score.reason().isBlank()) {
        throw new IllegalStateException("AI 대상 선정 응답에 유효하지 않은 평가가 있습니다.");
      }
      if (!responseIds.add(score.userId())) {
        throw new IllegalStateException("AI 대상 선정 응답에 중복 사용자 ID가 있습니다: " + score.userId());
      }
    }

    Set<Long> missingIds = new HashSet<>(expectedIds);
    missingIds.removeAll(responseIds);
    Set<Long> unexpectedIds = new HashSet<>(responseIds);
    unexpectedIds.removeAll(expectedIds);
    if (!missingIds.isEmpty() || !unexpectedIds.isEmpty()) {
      throw new IllegalStateException(
          "AI 대상 선정 응답 사용자 ID가 일치하지 않습니다. 누락=" + missingIds + ", 예상 외=" + unexpectedIds);
    }
  }
}
