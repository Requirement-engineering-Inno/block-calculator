package org.example.blockcalc.mail;

import org.example.blockcalc.user.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Tells a new employee his login, password and role. */
@Service
public class WelcomeMailService {

    public enum Result { SENT, FAILED, DISABLED }

    private static final Logger log = LoggerFactory.getLogger(WelcomeMailService.class);

    private final MailGateway gateway;
    private final MessageSource messages;
    private final String publicUrl;

    public WelcomeMailService(MailGateway gateway, MessageSource messages,
                              @Value("${app.public-url:http://localhost:8080}") String publicUrl) {
        this.gateway = gateway;
        this.messages = messages;
        this.publicUrl = publicUrl;
    }

    /** A failing mail server must never stop an account from being created, so errors are only reported. */
    public Result sendWelcome(UserAccount user, String rawPassword, Locale locale) {
        if (!gateway.enabled()) {
            gateway.send(user.email, "", "");
            return Result.DISABLED;
        }
        try {
            String role = messages.getMessage("role." + user.role.name(), null, user.role.name(), locale);
            Object[] args = {user.displayName, role, user.username, rawPassword, publicUrl};
            String subject = messages.getMessage("mail.welcome.subject", null, "Your account", locale);
            String body = messages.getMessage("mail.welcome.body", args, "Login: " + user.username, locale);
            gateway.send(user.email, subject, body);
            return Result.SENT;
        } catch (RuntimeException e) {
            log.warn("Could not send the welcome e-mail to {}: {}", user.email, e.getMessage());
            return Result.FAILED;
        }
    }
}
