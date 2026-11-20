package org.example.blockcalc.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Used when e-mail is not configured. The message text (it contains a password) is never logged. */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LogMailGateway implements MailGateway {

    private static final Logger log = LoggerFactory.getLogger(LogMailGateway.class);

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public void send(String to, String subject, String body) {
        log.info("E-mail is not configured (app.mail.enabled=false). Not sent to {}: {}", to, subject);
    }
}
