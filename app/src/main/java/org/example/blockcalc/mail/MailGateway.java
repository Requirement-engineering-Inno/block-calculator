package org.example.blockcalc.mail;

/** Sends plain text e-mail. Implementations: SMTP (app.mail.enabled=true) or a log-only stub. */
public interface MailGateway {

    /** False when mail is not configured, so nothing is really sent. */
    boolean enabled();

    void send(String to, String subject, String body);
}
