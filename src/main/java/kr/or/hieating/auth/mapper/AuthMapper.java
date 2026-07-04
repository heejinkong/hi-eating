package kr.or.hieating.auth.mapper;

import java.time.LocalDate;
import java.util.List;
import kr.or.hieating.auth.domain.Users;
import kr.or.hieating.auth.dto.SignupUserParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
  Users findByEmail(String email);

  Users findById(Long id);

  List<String> findAuthoritiesByUserId(Long userId);

  int countByEmail(String email);

  int insertUser(SignupUserParam param);

  int insertUserAuth(@Param("userId") long userId, @Param("auth") String auth);

  int updateUserProfile(
      @Param("userId") long userId,
      @Param("name") String name,
      @Param("birth") LocalDate birth,
      @Param("gender") String gender);

  int withdrawUser(@Param("userId") long userId);
}
