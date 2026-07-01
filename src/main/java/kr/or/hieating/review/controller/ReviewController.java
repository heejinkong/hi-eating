package kr.or.hieating.review.controller;

import kr.or.hieating.review.domain.Reviews;
import kr.or.hieating.review.dto.ReviewFormResponseDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReviewController {

  @GetMapping("/reviews/new")
  public String newReview(Model model) {
    ReviewFormResponseDto reviewForm = createSampleReviewForm();

    model.addAttribute("contentTemplate", "review/new");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "review");
    model.addAttribute("pageScript", "review");
    model.addAttribute("reviewForm", reviewForm);
    return "layout/base";
  }

  private ReviewFormResponseDto createSampleReviewForm() {
    Reviews review = new Reviews();
    review.setId(0);
    review.setUserId(1);
    review.setProductId(12);
    review.setPurchaseId(34);
    review.setRating(0);
    review.setContent("");
    review.setImgSrc("");

    ReviewFormResponseDto reviewForm = new ReviewFormResponseDto();
    reviewForm.setPurchaseId(34);
    reviewForm.setProductId(12);
    reviewForm.setBrandName("오도어");
    reviewForm.setProductName("Molly wide jogger in basic");
    reviewForm.setOptionName("1 · BLACK");
    reviewForm.setProductImageUrl(
        "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=320&q=80");
    reviewForm.setReview(review);
    return reviewForm;
  }
}
