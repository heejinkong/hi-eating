package kr.or.hieating.statistics.admin.controller;

import java.time.LocalDate;
import java.util.List;
import kr.or.hieating.statistics.admin.dto.AdminSalesChartDTO;
import kr.or.hieating.statistics.admin.dto.AdminSalesChartPointDTO;
import kr.or.hieating.statistics.admin.dto.AdminStatisticsMetricDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

  private static final String CHART_JS_CDN =
      "https://cdn.jsdelivr.net/npm/chart.js@4.4.9/dist/chart.umd.min.js";

  @GetMapping
  public String getStatisticsPage(Model model) {
    model.addAttribute("metrics", metrics());
    model.addAttribute("periodStart", LocalDate.of(2026, 6, 1));
    model.addAttribute("periodEnd", LocalDate.of(2026, 7, 1));
    model.addAttribute("ageSalesChart", ageSalesChart());
    model.addAttribute("categorySalesChart", categorySalesChart());
    model.addAttribute("genderSalesChart", genderSalesChart());
    model.addAttribute("priceSalesChart", priceSalesChart());
    model.addAttribute("contentTemplate", "admin/statistics/index");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "admin-statistics");
    model.addAttribute("pageVendorScript", CHART_JS_CDN);
    model.addAttribute("pageScript", "admin-statistics");
    return "layout/admin-base";
  }

  private List<AdminStatisticsMetricDTO> metrics() {
    return List.of(
        new AdminStatisticsMetricDTO("일일 매출", 128000L, "원", "전일 대비 ▲ 12.5%", "bi-stack"),
        new AdminStatisticsMetricDTO("오늘 총 주문", 42L, "건", "전일 대비 ▲ 8건", "bi-bag"),
        new AdminStatisticsMetricDTO(
            "한달 매출", 1500000L, "원", "전월 대비 ▲ 120,000원", "bi-bar-chart-line"),
        new AdminStatisticsMetricDTO("평균 주문 금액", 35714L, "원", "전일 대비 ▲ 2,345원", "bi-wallet2"));
  }

  private AdminSalesChartDTO ageSalesChart() {
    return new AdminSalesChartDTO(
        "나이별 매출 조회",
        List.of(
            new AdminSalesChartPointDTO("10대", 85000L, 7, 6, "#ff8a3d"),
            new AdminSalesChartPointDTO("20대", 250000L, 18, 17, "#ff7433"),
            new AdminSalesChartPointDTO("30대", 175000L, 12, 12, "#ff8a3d"),
            new AdminSalesChartPointDTO("40대", 158000L, 10, 11, "#ff8a3d"),
            new AdminSalesChartPointDTO("50대", 126000L, 8, 8, "#ff8a3d"),
            new AdminSalesChartPointDTO("60대 이상", 76000L, 5, 5, "#ff8a3d")));
  }

  private AdminSalesChartDTO categorySalesChart() {
    return new AdminSalesChartDTO(
        "카테고리별 매출 조회",
        List.of(
            new AdminSalesChartPointDTO("간편식", 450000L, 14, 30, "#ff7a35"),
            new AdminSalesChartPointDTO("신선식품", 300000L, 10, 20, "#ffd861"),
            new AdminSalesChartPointDTO("음료/간식", 270000L, 8, 18, "#73c56b"),
            new AdminSalesChartPointDTO("건강식품", 210000L, 6, 14, "#5ab3d8"),
            new AdminSalesChartPointDTO("생활용품", 150000L, 3, 10, "#8d62d6"),
            new AdminSalesChartPointDTO("기타", 120000L, 1, 8, "#d38bea")));
  }

  private AdminSalesChartDTO genderSalesChart() {
    return new AdminSalesChartDTO(
        "성별 매출 조회",
        List.of(
            new AdminSalesChartPointDTO("여성", 900000L, 26, 60, "#ff7a35"),
            new AdminSalesChartPointDTO("남성", 600000L, 16, 40, "#ffd861")));
  }

  private AdminSalesChartDTO priceSalesChart() {
    return new AdminSalesChartDTO(
        "가격별 매출 조회",
        List.of(
            new AdminSalesChartPointDTO("~ 10,000원", 120000L, 12, 8, "#ff8a3d"),
            new AdminSalesChartPointDTO("10,000 ~ 30,000원", 375000L, 15, 25, "#ff7433"),
            new AdminSalesChartPointDTO("30,000 ~ 50,000원", 315000L, 8, 21, "#ff8a3d"),
            new AdminSalesChartPointDTO("50,000 ~ 100,000원", 185000L, 5, 12, "#ff8a3d"),
            new AdminSalesChartPointDTO("100,000원 ~", 82000L, 2, 5, "#ff8a3d")));
  }
}
