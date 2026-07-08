package kr.or.hieating.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneratedHotDealEmailDto(
    @NotBlank @Size(min = 5, max = 255) String subject,
    @NotBlank @Size(min = 50, max = 2000) String content) {}
