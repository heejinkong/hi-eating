package kr.or.hieating.category.dto;

import lombok.Data;

@Data
public class CategoryMenuResponseDto {

  private Long id;
  private String name;
  private long productCount;
}
