package kr.or.hieating.ai.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "greenfood.ai")
public record AiProperties(@Valid Ollama generation, @Valid Ollama validation) {

  public record Ollama(
      @NotBlank String baseUrl,
      @NotBlank String model,
      @NotNull Double temperature,
      @NotNull Duration connectTimeout,
      @NotNull Duration readTimeout) {}
}
