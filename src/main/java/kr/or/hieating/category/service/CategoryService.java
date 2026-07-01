package kr.or.hieating.category.service;

import java.util.List;
import kr.or.hieating.category.dto.CategoryMenuResponseDto;
import kr.or.hieating.category.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryMapper categoryMapper;

  public List<CategoryMenuResponseDto> findCategories() {
    return categoryMapper.findCategories();
  }

  public CategoryMenuResponseDto findCategoryById(Long categoryId) {
    return categoryMapper.findCategoryById(categoryId);
  }
}
