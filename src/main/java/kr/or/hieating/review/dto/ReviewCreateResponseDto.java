package kr.or.hieating.review.dto;

public record ReviewCreateResponseDto(
    Long reviewId, Long productId, Long purchaseId, String imgSrc, String redirectUrl) {}
