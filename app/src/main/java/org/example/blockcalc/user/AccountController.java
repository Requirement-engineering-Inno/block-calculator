package org.example.blockcalc.user;

import org.example.blockcalc.common.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Every user changes his own password. */
@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService users;
    private final ProfileService profiles;

    public AccountController(UserService users, ProfileService profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    /** "My account": information about me and the password form. Both URLs show the same page. */
    @GetMapping({"", "/password"})
    public String account(@AuthenticationPrincipal CurrentUser me, Model model) {
        model.addAttribute("p", profiles.of(me, me.getId()));
        return "account/index";
    }

    @PostMapping("/password")
    public String change(@RequestParam String current, @RequestParam("new") String next,
                         @AuthenticationPrincipal CurrentUser me) {
        users.changePassword(me, current, next);
        return "redirect:/account/password?changed";
    }
}
