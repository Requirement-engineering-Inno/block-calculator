package org.example.blockcalc.user;

import org.example.blockcalc.common.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Remembers that the user has seen the welcome tour, so it opens by itself only once. */
@Controller
@RequestMapping("/onboarding")
public class OnboardingController {

    private final UserService users;

    public OnboardingController(UserService users) {
        this.users = users;
    }

    @PostMapping("/done")
    public ResponseEntity<Void> done(@AuthenticationPrincipal CurrentUser me) {
        users.markOnboardingDone(me.getId());
        return ResponseEntity.noContent().build();
    }
}
