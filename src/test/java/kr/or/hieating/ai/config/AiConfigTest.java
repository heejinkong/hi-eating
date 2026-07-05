package kr.or.hieating.ai.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class AiConfigTest {

  @Test
  void createsIndependentGenerationAndValidationClients() {
    AiProperties properties =
        new AiProperties(
            new AiProperties.Ollama(
                "http://generation:11434",
                "generation-model",
                0.7,
                Duration.ofSeconds(3),
                Duration.ofSeconds(120)),
            new AiProperties.Ollama(
                "http://validation:11435",
                "validation-model",
                0.2,
                Duration.ofSeconds(3),
                Duration.ofSeconds(120)));
    AiConfig config = new AiConfig();

    ChatClient generationClient = config.emailGenerationChatClient(properties);
    ChatClient validationClient = config.emailValidationChatClient(properties);

    assertThat(generationClient).isNotNull();
    assertThat(validationClient).isNotNull().isNotSameAs(generationClient);
  }
}
