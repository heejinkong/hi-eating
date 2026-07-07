package kr.or.hieating.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/chats")
public class AdminChatController {

  @GetMapping
  public String chats(Model model) {
    model.addAttribute("contentTemplate", "admin/chats/index");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "admin-chat");
    model.addAttribute("pageScript", "admin-chat");
    return "layout/admin-base";
  }
}
