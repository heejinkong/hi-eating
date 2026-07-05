package kr.or.hieating.tabledecor.controller;

import kr.or.hieating.tabledecor.dto.TableDecorLikeToggleResponseDto;
import kr.or.hieating.tabledecor.service.TableDecorPostService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TableDecorLikeRestController {

  private final TableDecorPostService tableDecorPostService;
  private final UserResolver userResolver;

  @PostMapping("/api/table-decorations/{postId}/likes/toggle")
  public TableDecorLikeToggleResponseDto toggleLike(@PathVariable Long postId) {
    Long userId = userResolver.requireCurrentUserId();
    return tableDecorPostService.toggleLike(userId, postId);
  }
}
