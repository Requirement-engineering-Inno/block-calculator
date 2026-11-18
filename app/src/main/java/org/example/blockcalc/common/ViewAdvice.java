package org.example.blockcalc.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.blockcalc.demo.DemoData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Model attributes needed by every page plus the handler that shows business errors. */
@ControllerAdvice
public class ViewAdvice {

    private final List<String> languages;
    private final MessageSource messages;
    private final boolean demo;
    private final String adminPassword;

    public ViewAdvice(@Value("#{'${app.languages:en,ru}'.split(',')}") List<String> languages, MessageSource messages,
                      @Value("${app.demo.enabled:false}") boolean demo,
                      @Value("${app.admin.password:admin}") String adminPassword) {
        this.languages = languages;
        this.messages = messages;
        this.demo = demo;
        this.adminPassword = adminPassword;
    }

    /** Accounts offered on the login page in demo mode, empty otherwise. */
    @ModelAttribute("demoAccounts")
    public List<DemoAccount> demoAccounts() {
        if (!demo) {
            return List.of();
        }
        List<DemoAccount> list = new ArrayList<>();
        list.add(new DemoAccount("admin", adminPassword, "ADMIN", "Administrator"));
        DemoData.ACCOUNTS.forEach(a -> list.add(new DemoAccount(a.username(), DemoData.PASSWORD, a.role(), a.name())));
        return list;
    }

    public record DemoAccount(String username, String password, String role, String name) {
    }

    @ModelAttribute("languages")
    public List<String> languages() {
        return languages;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("me")
    public CurrentUser me(@AuthenticationPrincipal CurrentUser me) {
        return me;
    }

    @ExceptionHandler(BusinessException.class)
    public String businessError(BusinessException e, Model model, HttpServletResponse response, Locale locale) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        model.addAttribute("errorMessage", messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessageKey(), locale));
        return "error";
    }
}
