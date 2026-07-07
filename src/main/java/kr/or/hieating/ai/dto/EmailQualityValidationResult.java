package kr.or.hieating.ai.dto;

import java.util.List;

public record EmailQualityValidationResult(
    boolean spellingValid,
    boolean contextValid,
    boolean productInfoValid,
    boolean discountRateValid,
    boolean exaggerationFree,
    boolean lengthValid,
    List<String> issues) {

  public EmailQualityValidationResult {
    issues = issues == null ? List.of() : List.copyOf(issues);
  }

  public boolean isPass() {
    return spellingValid
        && contextValid
        && productInfoValid
        && discountRateValid
        && exaggerationFree
        && lengthValid;
  }

  public String reason() {
    return isPass() ? "모든 이메일 품질 검증 항목을 통과했습니다." : String.join("; ", issues);
  }
}
