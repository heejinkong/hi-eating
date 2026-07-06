package kr.or.hieating.ai.dto;

public record HotDealEmailProductRow(
    String productName,
    String categoryName,
    Integer originalPrice,
    Integer hotDealPrice,
    Integer discountRate) {}
