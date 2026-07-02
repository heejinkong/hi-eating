package kr.or.hieating.statistics.admin.dto;

import java.util.List;
import java.util.stream.Collectors;

public record AdminSalesChartDTO(String title, List<AdminSalesChartPointDTO> points) {

  public Long totalSalesAmount() {
    return points.stream().mapToLong(AdminSalesChartPointDTO::salesAmount).sum();
  }

  public String labelsText() {
    return points.stream().map(AdminSalesChartPointDTO::label).collect(Collectors.joining("|"));
  }

  public String valuesText() {
    return points.stream()
        .map(point -> String.valueOf(point.salesAmount()))
        .collect(Collectors.joining(","));
  }

  public String colorsText() {
    return points.stream().map(AdminSalesChartPointDTO::color).collect(Collectors.joining(","));
  }
}
