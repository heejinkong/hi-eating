package kr.or.hieating.statistics.admin.dto;

public record AdminStatisticsMetricDTO(
    String title, Long value, String unit, String comparisonText, String iconClass) {}
