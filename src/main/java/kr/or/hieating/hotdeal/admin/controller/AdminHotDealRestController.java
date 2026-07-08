package kr.or.hieating.hotdeal.admin.controller;

import jakarta.validation.Valid;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.hotdeal.admin.dto.HotDealCreateRequestDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealDetailResponseDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealUpdateRequestDTO;
import kr.or.hieating.hotdeal.admin.service.AdminHotDealService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/hotdeals")
public class AdminHotDealRestController {

  private final AdminHotDealService adminHotDealService;

  @PostMapping
  public ApiResponse<Long> createHotDeal(@RequestBody @Valid HotDealCreateRequestDTO request) {
    Long newHotDealId = adminHotDealService.createHotDeal(request);
    return ApiResponse.onSuccess(newHotDealId);
  }

  @PutMapping("/{id}")
  public ApiResponse<Void> updateHotDeal(
      @PathVariable("id") Long id, @RequestBody @Valid HotDealUpdateRequestDTO request) {

    adminHotDealService.updateHotDeal(id, request);
    return ApiResponse.onSuccess(null);
  }

  @GetMapping("/{id}")
  public ApiResponse<HotDealDetailResponseDTO> getHotDealDetail(@PathVariable("id") Long id) {
    HotDealDetailResponseDTO detail = adminHotDealService.getHotDealDetail(id);
    return ApiResponse.onSuccess(detail);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> deleteHotDeal(@PathVariable("id") Long id) {
    adminHotDealService.deleteHotDeal(id);
    return ApiResponse.onSuccess(null);
  }
}
