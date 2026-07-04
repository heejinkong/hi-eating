package kr.or.hieating.table.controller;

import kr.or.hieating.table.service.TableBuilderService;
import kr.or.hieating.utils.UserResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TableBuilderController {

  private final TableBuilderService tableBuilderService;
  private final UserResolver userResolver;

  public TableBuilderController(
      TableBuilderService tableBuilderService, UserResolver userResolver) {
    this.tableBuilderService = tableBuilderService;
    this.userResolver = userResolver;
  }

  @Value("${greenfood.table-builder.table-model-src:/models/table.glb}")
  private String tableModelSrc;

  @GetMapping("/table-builder")
  public String tableBuilder(Model model) {
    model.addAttribute("contentTemplate", "table/builder");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "table-builder");
    model.addAttribute(
        "products", tableBuilderService.findProducts(userResolver.currentUserIdOrNull()));
    model.addAttribute("tableModelSrc", tableModelSrc);
    return "layout/base";
  }
}
