package kr.or.hieating.promotion.admin.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.promotion.admin.dto.PromotionUpdateRequestDTO;
import kr.or.hieating.promotion.admin.service.AdminPromotionService;
import kr.or.hieating.promotion.domain.Promotions;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/api/promotions")
@RequiredArgsConstructor
@Validated
public class AdminPromotionRestController {

  private final AdminPromotionService adminPromotionService;

  @PostMapping
  public ApiResponse<Promotions> uploadBanner(
      @RequestParam("file") MultipartFile file,
      @RequestParam("title") @NotBlank(message = "프로모션 제목은 필수 입력값입니다.") String title,
      @RequestParam("link") String link,
      @RequestParam("startsAt")
          @NotNull(message = "프로모션 시작일은 필수 입력값입니다.") @DateTimeFormat(pattern = "yyyy.MM.dd")
          LocalDate startsAt,
      @RequestParam("endsAt")
          @NotNull(message = "프로모션 종료일은 필수 입력값입니다.") @DateTimeFormat(pattern = "yyyy.MM.dd")
          LocalDate endsAt) {
    if (startsAt.isAfter(endsAt)) {
      throw new GeneralException(ErrorStatus.INVALID_PROMOTION_DATE);
    }
    Promotions promotion =
        adminPromotionService.registerPromotion(file, title, link, startsAt, endsAt);
    return ApiResponse.onSuccess(promotion);
  }

  @PutMapping("/{id}")
  public ApiResponse<Void> updatePromotion(
      @PathVariable("id") int id, @Valid @RequestBody PromotionUpdateRequestDTO request) {
    adminPromotionService.updatePromotionDetails(
        id,
        null,
        request.getTitle(),
        request.getLink(),
        request.getStartsAt(),
        request.getEndsAt());
    return ApiResponse.onSuccess(null);
  }

  @PostMapping("/{id}")
  public ApiResponse<Void> updatePromotionMultipart(
      @PathVariable("id") int id,
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam("title") @NotBlank(message = "프로모션 제목은 필수 입력값입니다.") String title,
      @RequestParam("link") String link,
      @RequestParam("startsAt")
          @NotNull(message = "프로모션 시작일은 필수 입력값입니다.") @DateTimeFormat(pattern = "yyyy.MM.dd")
          LocalDate startsAt,
      @RequestParam("endsAt")
          @NotNull(message = "프로모션 종료일은 필수 입력값입니다.") @DateTimeFormat(pattern = "yyyy.MM.dd")
          LocalDate endsAt) {
    if (startsAt.isAfter(endsAt)) {
      throw new GeneralException(ErrorStatus.INVALID_PROMOTION_DATE);
    }
    adminPromotionService.updatePromotionDetails(id, file, title, link, startsAt, endsAt);
    return ApiResponse.onSuccess(null);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> deletePromotion(@PathVariable("id") int id) {
    adminPromotionService.deletePromotion(id);
    return ApiResponse.onSuccess(null);
  }
}
