package kr.or.hieating.mypage.service;

import java.time.LocalDate;
import java.util.Set;
import kr.or.hieating.auth.domain.Users;
import kr.or.hieating.auth.mapper.AuthMapper;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

  private static final Set<String> EDITABLE_GENDERS = Set.of("MALE", "FEMALE");

  private final UserResolver userResolver;
  private final AuthMapper authMapper;

  public Users findCurrentMemberOrNull() {
    Long userId = userResolver.currentUserIdOrNull();
    if (userId == null) {
      return null;
    }

    return authMapper.findById(userId);
  }

  @Transactional
  public String updateCurrentMemberProfile(String name, LocalDate birth, String gender) {
    Users member = requireCurrentMember();
    String normalizedName = validateAndNormalizeName(name);
    validateMemberEdit(birth, gender);

    int updated = authMapper.updateUserProfile(member.getId(), normalizedName, birth, gender);
    if (updated == 0) {
      throw new IllegalArgumentException("회원 정보를 수정할 수 없습니다.");
    }

    return normalizedName;
  }

  @Transactional
  public void withdrawCurrentMember() {
    Users member = requireCurrentMember();
    int withdrawn = authMapper.withdrawUser(member.getId());
    if (withdrawn == 0) {
      throw new GeneralException(ErrorStatus.MEMBER_WITHDRAW_FAILED);
    }
  }

  private Users requireCurrentMember() {
    Users member = findCurrentMemberOrNull();
    if (member == null) {
      throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
    }
    return member;
  }

  private String validateAndNormalizeName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("이름을 입력해 주세요.");
    }

    String normalizedName = name.trim();
    if (normalizedName.length() > 100) {
      throw new IllegalArgumentException("이름은 100자 이하로 입력해 주세요.");
    }

    return normalizedName;
  }

  private void validateMemberEdit(LocalDate birth, String gender) {
    if (birth == null) {
      throw new IllegalArgumentException("생년월일을 입력해 주세요.");
    }

    if (birth.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("생년월일은 오늘 이후 날짜를 입력할 수 없습니다.");
    }

    if (gender == null || !EDITABLE_GENDERS.contains(gender)) {
      throw new IllegalArgumentException("성별 값을 확인해 주세요.");
    }
  }
}
