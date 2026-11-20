package org.example.blockcalc.settings;

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

import java.math.BigDecimal;

/** Application level settings. Access is limited to admin and manager in SecurityConfig. */
@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final SettingsService settings;

    public SettingsController(SettingsService settings) {
        this.settings = settings;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("wheelbarrow", settings.wheelbarrowKg().stripTrailingZeros().toPlainString());
        model.addAttribute("usage", settings.usage());
        return "settings/index";
    }

    @PostMapping("/wheelbarrow")
    public String wheelbarrow(@RequestParam BigDecimal kg, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        settings.setWheelbarrow(me, kg);
        ra.addFlashAttribute("flash", "flash.settings.saved");
        return "redirect:/settings";
    }

    @PostMapping("/currencies")
    public String addCurrency(@RequestParam String code, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        settings.addCurrency(me, code);
        ra.addFlashAttribute("flash", "flash.currency.added");
        return "redirect:/settings";
    }

    @PostMapping("/currencies/{code}/delete")
    public String removeCurrency(@PathVariable String code, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        settings.removeCurrency(me, code);
        ra.addFlashAttribute("flash", "flash.currency.removed");
        return "redirect:/settings";
    }
}
