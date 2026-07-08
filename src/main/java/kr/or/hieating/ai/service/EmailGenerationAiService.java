package kr.or.hieating.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/** 이메일 "생성" 전담 서비스. emailGenerationChatClient(생성용 원격 Ollama)를 사용한다. */
@Service
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true) // greenfood.ai.enabled=false면 이 Bean 자체가 생성되지 않음
public class EmailGenerationAiService {

  private final ChatClient chatClient;
  private final org.springframework.ai.converter.BeanOutputConverter<
          kr.or.hieating.ai.dto.GeneratedHotDealEmailDto>
      outputConverter =
          new org.springframework.ai.converter.BeanOutputConverter<>(
              kr.or.hieating.ai.dto.GeneratedHotDealEmailDto.class);

  // @Qualifier로 여러 ChatClient Bean 중 "생성용" Bean을 명시적으로 주입받음
  public EmailGenerationAiService(@Qualifier("emailGenerationChatClient") ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  /**
   * 주어진 프롬프트로 이메일 본문을 생성한다.
   *
   * @param prompt LLM에게 전달할 지시문
   * @return 생성된 이메일 텍스트
   */
  public String generate(String prompt) {
    Assert.hasText(prompt, "prompt must not be blank"); // 빈 프롬프트 방어
    String response =
        chatClient
            .prompt()
            .user(prompt)
            .options(
                org.springframework.ai.ollama.api.OllamaChatOptions.builder()
                    .format(outputConverter.getJsonSchemaMap())
                    .numPredict(2048) // 이메일 생성 글자 수 제한 넉넉히 설정
                    .build())
            .call()
            .content();
    Assert.state(StringUtils.hasText(response), "AI generation response must not be blank");
    return response;
  }
}
