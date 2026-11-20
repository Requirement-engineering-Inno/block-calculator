package org.example.blockcalc.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Real delivery through the SMTP server from spring.mail.* (for example Mailpit in docker-compose). */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpMailGateway implements MailGateway {

    private final JavaMailSender sender;
    private final String from;

    public SmtpMailGateway(JavaMailSender sender, @Value("${app.mail.from:no-reply@blockcalc.local}") String from) {
        this.sender = sender;
        this.from = from;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        sender.send(message);
    }
}
