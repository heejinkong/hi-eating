package kr.or.hieating.ai.dto;

import java.util.List;

public record HotDealTargetInfoDto(
    Long hotDealId,
    String title,
    String description,
    List<String> categoryNames,
    List<HotDealProductInfoDto> products) {

  public static HotDealTargetInfoDto from(
      HotDealInfoRow row, List<HotDealProductInfoDto> products) {
    return new HotDealTargetInfoDto(
        row.hotDealId(),
        row.title(),
        row.description(),
        UserProfileDto.splitCsv(row.categoryNames()),
        List.copyOf(products));
  }
}
