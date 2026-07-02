package kr.or.hieating.auth.mapper;

import java.util.List;
import kr.or.hieating.auth.domain.Users;
import kr.or.hieating.auth.dto.SignupUserParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
  Users findByEmail(String email);

  List<String> findAuthoritiesByUserId(Long userId);

  int countByEmail(String email);

  int insertUser(SignupUserParam param);

  int insertUserAuth(@Param("userId") long userId, @Param("auth") String auth);
}
