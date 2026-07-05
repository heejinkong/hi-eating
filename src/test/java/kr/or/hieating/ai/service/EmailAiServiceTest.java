package kr.or.hieating.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class EmailAiServiceTest {

  @Test
  void generationServiceReturnsGeneratedEmail() {
    ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
    EmailGenerationAiService service = new EmailGenerationAiService(chatClient);
    when(chatClient.prompt().user("이메일 생성").call().content()).thenReturn("생성된 이메일");

    String response = service.generate("이메일 생성");

    assertThat(response).isEqualTo("생성된 이메일");
  }

  @Test
  void validationServiceReturnsValidationResult() {
    ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
    EmailValidationAiService service = new EmailValidationAiService(chatClient);
    when(chatClient.prompt().user("이메일 검증").call().content()).thenReturn("PASS");

    String response = service.validate("이메일 검증");

    assertThat(response).isEqualTo("PASS");
  }

  @Test
  void servicesRejectBlankPrompts() {
    ChatClient chatClient = mock(ChatClient.class);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new EmailGenerationAiService(chatClient).generate(" "));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new EmailValidationAiService(chatClient).validate(" "));
  }

  @Test
  void generationServiceRejectsNullResponse() {
    ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
    EmailGenerationAiService service = new EmailGenerationAiService(chatClient);
    when(chatClient.prompt().user("이메일 생성").call().content()).thenReturn(null);

    assertThatIllegalStateException()
        .isThrownBy(() -> service.generate("이메일 생성"))
        .withMessage("AI generation response must not be blank");
  }

  @Test
  void validationServiceRejectsBlankResponse() {
    ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
    EmailValidationAiService service = new EmailValidationAiService(chatClient);
    when(chatClient.prompt().user("이메일 검증").call().content()).thenReturn(" ");

    assertThatIllegalStateException()
        .isThrownBy(() -> service.validate("이메일 검증"))
        .withMessage("AI validation response must not be blank");
  }
}
