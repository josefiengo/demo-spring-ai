package com.example.demospringai.ai.tools;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DateTimeTools {

    private static final DateTimeFormatter SPANISH_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("EEEE, d 'de' MMMM 'de' uuuu, HH:mm:ss", Locale.forLanguageTag("es"));

    @Tool(description = "Obtiene la fecha y hora actuales en la zona horaria del usuario")
    public String getCurrentDateTime() {
        ZoneId zoneId = LocaleContextHolder.getTimeZone().toZoneId();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        return """
                Zona horaria: %s
                Fecha y hora: %s
                ISO-8601: %s
                """.formatted(
                zoneId,
                SPANISH_DATE_TIME_FORMATTER.format(now),
                now);
    }
}
