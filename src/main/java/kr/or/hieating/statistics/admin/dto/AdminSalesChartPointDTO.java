package kr.or.hieating.statistics.admin.dto;

public record AdminSalesChartPointDTO(
    String label, Long salesAmount, Integer orderCount, Integer salesRate, String color) {}
