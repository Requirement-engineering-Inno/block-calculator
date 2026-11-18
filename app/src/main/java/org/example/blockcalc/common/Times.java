package org.example.blockcalc.common;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Times are stored and sent as UTC instants. Pages show them in GMT and the browser converts them
 * to the user's own time zone (see app.js). Exports use the time zone sent by the browser.
 */
@Component("times")
public class Times {

    private static final DateTimeFormatter GMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z", Locale.ENGLISH);

    public static String formatGmt(Instant t) {
        return t == null ? "" : GMT.format(t) + " GMT";
    }

    /** Used from templates: ${@times.gmt(value)}. */
    public String gmt(Instant t) {
        return formatGmt(t);
    }

    public static String formatIn(Instant t, ZoneId zone) {
        return t == null ? "" : LOCAL.format(t.atZone(zone));
    }

    /** Time zone id sent by the browser; falls back to UTC when missing or invalid. */
    public static ZoneId zoneOf(String id) {
        try {
            return id == null || id.isBlank() ? ZoneOffset.UTC : ZoneId.of(id);
        } catch (RuntimeException e) {
            return ZoneOffset.UTC;
        }
    }
}
