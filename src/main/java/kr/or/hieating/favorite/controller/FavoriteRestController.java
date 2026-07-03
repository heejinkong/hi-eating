package kr.or.hieating.favorite.controller;

import kr.or.hieating.favorite.dto.FavoriteToggleResponseDto;
import kr.or.hieating.favorite.service.FavoriteService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FavoriteRestController {

  private final FavoriteService favoriteService;
  private final UserResolver userResolver;

  @PostMapping("/api/favorites/{productId}/toggle")
  public FavoriteToggleResponseDto toggleFavorite(@PathVariable Long productId) {
    Long userId = userResolver.requireCurrentUserId();
    boolean favorite = favoriteService.toggleFavorite(userId, productId);
    return new FavoriteToggleResponseDto(productId, favorite);
  }
}
