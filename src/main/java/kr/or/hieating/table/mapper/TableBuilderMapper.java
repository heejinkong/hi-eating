package kr.or.hieating.table.mapper;

import java.util.List;
import kr.or.hieating.table.dto.TableBuilderProductDto;
import kr.or.hieating.table.dto.TableCaptureCreateCommand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TableBuilderMapper {

  List<TableBuilderProductDto> findTableBuilderProducts(@Param("userId") Long userId);

  void insertPost(TableCaptureCreateCommand command);
}
