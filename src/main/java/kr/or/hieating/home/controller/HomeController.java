package kr.or.hieating.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("contentTemplate", "home/index");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "home");
    return "layout/base";
  }
}
