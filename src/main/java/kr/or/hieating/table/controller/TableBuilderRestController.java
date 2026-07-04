package kr.or.hieating.table.controller;

import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.table.dto.TableCaptureResponseDto;
import kr.or.hieating.table.service.TableBuilderService;
import kr.or.hieating.utils.UserResolver;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TableBuilderRestController {

  private final TableBuilderService tableBuilderService;
  private final UserResolver userResolver;

  public TableBuilderRestController(
      TableBuilderService tableBuilderService, UserResolver userResolver) {
    this.tableBuilderService = tableBuilderService;
    this.userResolver = userResolver;
  }

  @PostMapping(
      value = "/api/table-builder/captures",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<TableCaptureResponseDto> createCapture(
      @RequestParam("captureImage") MultipartFile captureImage) {
    TableCaptureResponseDto result =
        tableBuilderService.createCapturePost(userResolver.requireCurrentUserId(), captureImage);
    return ApiResponse.onSuccess(result);
  }
}
