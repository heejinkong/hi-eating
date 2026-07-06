package kr.or.hieating.ai.dto;

public record TargetSelectionResult(
    Long hotDealId, Integer candidateCount, Integer selectedCount, Integer insertedCount) {

  public static TargetSelectionResult empty(Long hotDealId) {
    return new TargetSelectionResult(hotDealId, 0, 0, 0);
  }
}
