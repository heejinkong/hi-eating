package kr.or.hieating.ai.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "greenfood.ai")
public record AiProperties(
    @Valid Ollama generation, @Valid Ollama validation, @Valid TargetSelection targetSelection) {

  public record Ollama(
      @NotBlank String baseUrl,
      @NotBlank String model,
      @NotNull Double temperature,
      @NotNull Duration connectTimeout,
      @NotNull Duration readTimeout) {}

  public record TargetSelection(
      @NotNull @Min(0) @Max(100) Integer scoreThreshold, // 이메일 대상 선정 최소 점수
      @NotNull @Positive Integer batchSize, // 한 번의 AI 요청에 포함할 사용자 수
      @NotNull @Positive Integer recentMonths, // 최근 구매/리뷰 조회 기간
      @NotNull @Min(0) Integer retryCount, // AI 호출 또는 파싱 실패 재시도 횟수
      @NotNull @Min(0) Integer jobMaxRetries) {} // 대상 선정 Job 전체 재시도 횟수
}
