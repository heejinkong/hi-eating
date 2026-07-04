package kr.or.hieating.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ReviewCreateRequestDto {

  @NotNull(message = "구매 정보가 필요합니다.") private Long purchaseId;

  @NotNull(message = "상품 정보가 필요합니다.") private Long productId;

  @Min(value = 1, message = "별점은 1점 이상이어야 합니다.") @Max(value = 5, message = "별점은 5점 이하여야 합니다.") private int rating;

  @NotBlank(message = "리뷰 내용을 입력해주세요.") @Size(max = 500, message = "리뷰 내용은 500자 이하여야 합니다.") private String content;

  private MultipartFile reviewImage;
}
