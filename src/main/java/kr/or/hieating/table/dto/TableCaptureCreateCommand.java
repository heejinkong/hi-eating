package kr.or.hieating.table.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TableCaptureCreateCommand {

  private Long id;
  private Long userId;
  private String imgSrc;
}
