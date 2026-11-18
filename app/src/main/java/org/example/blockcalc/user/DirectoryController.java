package org.example.blockcalc.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Small HTML fragments for the worker pickers (project assignment, sharing). Any signed-in user may search. */
@Controller
@RequestMapping("/directory")
public class DirectoryController {

    private final UserService users;

    public DirectoryController(UserService users) {
        this.users = users;
    }

    @GetMapping("/workers")
    public String workers(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "12") int limit, Model model) {
        model.addAttribute("results", users.searchWorkers(q, Math.max(1, Math.min(limit, 30))));
        return "directory :: workers";
    }
}
