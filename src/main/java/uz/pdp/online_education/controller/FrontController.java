package uz.pdp.online_education.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontController {

    @GetMapping("/")
    public String index() {
        // templates/registry-telegram-bot.html ni ochadi
        return "registry-telegram-bot";
    }
}
