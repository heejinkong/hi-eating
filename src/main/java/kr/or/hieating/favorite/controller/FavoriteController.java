package kr.or.hieating.favorite.controller;

import kr.or.hieating.favorite.dto.FavoriteProductListPageResponseDto;
import kr.or.hieating.favorite.dto.FavoriteProductSearchCondition;
import kr.or.hieating.favorite.service.FavoriteService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final UserResolver userResolver;

  @GetMapping("/favorites")
  public String favorites(
      @RequestParam(defaultValue = "latest") String sort,
      @RequestParam(defaultValue = "1") Integer page,
      Model model) {
    Long userId = userResolver.currentUserId();
    FavoriteProductSearchCondition condition =
        new FavoriteProductSearchCondition(userId, sort, page);
    FavoriteProductListPageResponseDto productPage =
        favoriteService.findFavoriteProducts(condition);

    model.addAttribute("contentTemplate", "favorite/list");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "favorite");
    model.addAttribute("condition", condition);
    model.addAttribute("productPage", productPage);
    return "layout/base";
  }
}
