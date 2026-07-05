package kr.or.hieating.tabledecor.controller;

import kr.or.hieating.tabledecor.dto.TableDecorPostPageResponseDto;
import kr.or.hieating.tabledecor.dto.TableDecorPostSearchCondition;
import kr.or.hieating.tabledecor.service.TableDecorPostService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class TableDecorPostController {

  private final TableDecorPostService tableDecorPostService;
  private final UserResolver userResolver;

  @GetMapping("/table-decorations")
  public String tableDecorations(@RequestParam(defaultValue = "1") Integer page, Model model) {
    Long currentUserId = userResolver.currentUserIdOrNull();
    TableDecorPostSearchCondition condition =
        new TableDecorPostSearchCondition(page, currentUserId);
    TableDecorPostPageResponseDto postPage = tableDecorPostService.findPosts(condition);

    model.addAttribute("contentTemplate", "tabledecor/list");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "tabledecor");
    model.addAttribute("pageScript", "tabledecor");
    model.addAttribute("tableDecorAuthenticated", currentUserId != null);
    model.addAttribute("condition", condition);
    model.addAttribute("postPage", postPage);
    return "layout/base";
  }

  @GetMapping("/api/table-decorations/posts")
  @ResponseBody
  public TableDecorPostPageResponseDto tableDecorationPosts(
      @RequestParam(defaultValue = "1") Integer page) {
    Long currentUserId = userResolver.currentUserIdOrNull();
    TableDecorPostSearchCondition condition =
        new TableDecorPostSearchCondition(page, currentUserId);
    return tableDecorPostService.findPosts(condition);
  }
}
