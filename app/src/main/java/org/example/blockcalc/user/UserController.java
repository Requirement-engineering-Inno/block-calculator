package org.example.blockcalc.user;

import org.example.blockcalc.common.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Account management. Access is limited to admin and manager in SecurityConfig. */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final int PAGE_SIZE = 15;

    private final UserService users;
    private final ProfileService profiles;

    public UserController(UserService users, ProfileService profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, @RequestParam(required = false) Role role,
                       @RequestParam(required = false) Boolean active, @RequestParam(defaultValue = "1") int page,
                       @AuthenticationPrincipal CurrentUser me, Model model) {
        model.addAttribute("page", users.search(me, q, role, active, page, PAGE_SIZE));
        model.addAttribute("q", q);
        model.addAttribute("roleFilter", role == null ? "" : role.name());
        model.addAttribute("activeFilter", active == null ? "" : active.toString());
        model.addAttribute("roles", me.isAdmin() ? List.of(Role.MANAGER, Role.WORKER) : List.of(Role.WORKER));
        model.addAttribute("filterRoles", me.isAdmin() ? List.of(Role.ADMIN, Role.MANAGER, Role.WORKER) : List.of(Role.WORKER));
        return "user/list";
    }

    @GetMapping("/{id}")
    public String profile(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, Model model) {
        model.addAttribute("p", profiles.of(me, id));
        return "user/profile";
    }

    @PostMapping
    public String create(@RequestParam String username, @RequestParam String password,
                         @RequestParam(required = false) String displayName, @RequestParam Role role,
                         @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        users.create(me, username, password, displayName, role);
        ra.addFlashAttribute("flash", "flash.user.created");
        return "redirect:/users";
    }

    @PostMapping("/{id}/block")
    public String block(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        users.setActive(me, id, false);
        ra.addFlashAttribute("flash", "flash.user.blocked");
        return "redirect:/users";
    }

    @PostMapping("/{id}/unblock")
    public String unblock(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        users.setActive(me, id, true);
        ra.addFlashAttribute("flash", "flash.user.unblocked");
        return "redirect:/users";
    }
}
