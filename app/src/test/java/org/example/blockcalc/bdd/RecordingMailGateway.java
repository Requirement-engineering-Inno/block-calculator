package org.example.blockcalc.bdd;

import org.example.blockcalc.mail.MailGateway;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Replaces the real mail gateway in the tests: remembers every message instead of sending it. */
@Component
@Primary
public class RecordingMailGateway implements MailGateway {

    public record Sent(String to, String subject, String body) {
    }

    public final List<Sent> sent = new ArrayList<>();
    public boolean failing;

    public void reset() {
        sent.clear();
        failing = false;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public void send(String to, String subject, String body) {
        if (failing) {
            throw new IllegalStateException("SMTP server is down");
        }
        sent.add(new Sent(to, subject, body));
    }
}
