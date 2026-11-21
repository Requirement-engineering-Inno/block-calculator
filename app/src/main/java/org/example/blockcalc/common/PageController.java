package org.example.blockcalc.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/projects";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
