package kr.or.hieating.common;

import java.util.List;
import kr.or.hieating.auth.domain.Users;
import kr.or.hieating.auth.dto.AdminUserDto;
import kr.or.hieating.auth.mapper.AuthMapper;
import kr.or.hieating.category.dto.CategoryMenuResponseDto;
import kr.or.hieating.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalModelAttributeAdvice {

  private final CategoryService categoryService;
  private final AuthMapper authMapper;

  @ModelAttribute("headerCategories")
  public List<CategoryMenuResponseDto> headerCategories() {
    return categoryService.findCategories();
  }

  @ModelAttribute("currentUser")
  public AdminUserDto currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return null;
    }

    try {
      Users user = authMapper.findByEmail(authentication.getName());
      return user == null
          ? null
          : AdminUserDto.builder()
              .id(user.getId())
              .name(user.getName())
              .email(user.getEmail())
              .build();
    } catch (RuntimeException e) {
      log.warn("Failed to load current user for the global model attribute", e);
      return null;
    }
  }
}
