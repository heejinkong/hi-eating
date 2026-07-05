package kr.or.hieating.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/** 이메일 "품질 검증" 전담 서비스. emailValidationChatClient(검증용 원격 Ollama)를 사용한다. */
@Service
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true) // greenfood.ai.enabled=false면 이 Bean 자체가 생성되지 않음
public class EmailValidationAiService {

  private final ChatClient chatClient;

  // @Qualifier로 여러 ChatClient Bean 중 "검증용" Bean을 명시적으로 주입받음
  public EmailValidationAiService(@Qualifier("emailValidationChatClient") ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  /**
   * 주어진 프롬프트(검증 대상 이메일 포함)로 품질 검증 결과를 반환한다.
   *
   * @param prompt 검증할 이메일이 포함된 지시문
   * @return PASS 또는 FAIL로 시작하는 검증 결과 텍스트
   */
  public String validate(String prompt) {
    Assert.hasText(prompt, "prompt must not be blank"); // 빈 프롬프트 방어
    String response = chatClient.prompt().user(prompt).call().content();
    Assert.state(StringUtils.hasText(response), "AI validation response must not be blank");
    return response;
  }
}
