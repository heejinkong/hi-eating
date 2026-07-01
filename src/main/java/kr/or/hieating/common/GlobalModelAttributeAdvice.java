package kr.or.hieating.common;

import java.util.List;
import kr.or.hieating.category.dto.CategoryMenuResponseDto;
import kr.or.hieating.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

  private final CategoryService categoryService;

  @ModelAttribute("headerCategories")
  public List<CategoryMenuResponseDto> headerCategories() {
    return categoryService.findCategories();
  }
}
