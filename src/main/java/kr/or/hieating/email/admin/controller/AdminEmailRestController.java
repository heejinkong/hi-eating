package kr.or.hieating.email.admin.controller;

import kr.or.hieating.email.admin.dto.AdminEmailDashboardDto;
import kr.or.hieating.email.admin.dto.AdminEmailPublishBatchResponseDto;
import kr.or.hieating.email.admin.dto.AdminEmailPublishRequestDto;
import kr.or.hieating.email.admin.dto.AdminEmailUpdateRequestDto;
import kr.or.hieating.email.admin.service.AdminEmailService;
import kr.or.hieating.email.dto.EmailDraftDto;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/emails")
public class AdminEmailRestController {

  private final AdminEmailService adminEmailService;

  @GetMapping
  public ApiResponse<AdminEmailDashboardDto> emails(@RequestParam(required = false) Long emailId) {
    return ApiResponse.onSuccess(adminEmailService.getDashboard(emailId));
  }

  @GetMapping("/{emailDraftId}")
  public ApiResponse<EmailDraftDto> email(@PathVariable Long emailDraftId) {
    try {
      return ApiResponse.onSuccess(adminEmailService.getEmailDraft(emailDraftId));
    } catch (IllegalArgumentException exception) {
      return ApiResponse.onFailure(
          ErrorStatus._BAD_REQUEST.getCode(), exception.getMessage(), null);
    }
  }

  @PatchMapping("/{emailDraftId}/content")
  public ApiResponse<EmailDraftDto> updateEmailContent(
      @PathVariable Long emailDraftId, @RequestBody AdminEmailUpdateRequestDto request) {
    try {
      return ApiResponse.onSuccess(
          adminEmailService.updateFailedEmailContent(emailDraftId, request));
    } catch (IllegalArgumentException exception) {
      return ApiResponse.onFailure(
          ErrorStatus._BAD_REQUEST.getCode(), exception.getMessage(), null);
    }
  }

  @PostMapping("/{emailDraftId}/publish")
  public ApiResponse<EmailDraftDto> publishEmail(@PathVariable Long emailDraftId) {
    try {
      return ApiResponse.onSuccess(adminEmailService.publishEmailDraft(emailDraftId));
    } catch (IllegalArgumentException exception) {
      return ApiResponse.onFailure(
          ErrorStatus._BAD_REQUEST.getCode(), exception.getMessage(), null);
    }
  }

  @PostMapping("/publish")
  public ApiResponse<AdminEmailPublishBatchResponseDto> publishEmails(
      @RequestBody AdminEmailPublishRequestDto request) {
    try {
      return ApiResponse.onSuccess(adminEmailService.publishEmailDrafts(request));
    } catch (IllegalArgumentException exception) {
      return ApiResponse.onFailure(
          ErrorStatus._BAD_REQUEST.getCode(), exception.getMessage(), null);
    }
  }

  @PostMapping("/publish/validation-pass")
  public ApiResponse<AdminEmailPublishBatchResponseDto> publishValidationPassedEmails() {
    try {
      return ApiResponse.onSuccess(adminEmailService.publishValidationPassedReadyEmails());
    } catch (IllegalArgumentException exception) {
      return ApiResponse.onFailure(
          ErrorStatus._BAD_REQUEST.getCode(), exception.getMessage(), null);
    }
  }
}
