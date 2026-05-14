package com.example.demospringai.ai.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

class DateTimeToolsTest {

    private static final DateTimeFormatter SPANISH_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("EEEE, d 'de' MMMM 'de' uuuu, HH:mm:ss", Locale.forLanguageTag("es"));

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void currentDateTimeIncludesConsistentFormattedDateAndZone() {
        LocaleContextHolder.setTimeZone(TimeZone.getTimeZone("America/La_Paz"));
        DateTimeTools tools = new DateTimeTools();

        String result = tools.getCurrentDateTime();

        String isoDateTime = result.lines()
                .filter(line -> line.startsWith("ISO-8601: "))
                .findFirst()
                .orElseThrow()
                .replace("ISO-8601: ", "");
        ZonedDateTime parsedDateTime = ZonedDateTime.parse(isoDateTime);

        assertThat(result).contains("Zona horaria: America/La_Paz");
        assertThat(result).contains("Fecha y hora: " + SPANISH_DATE_TIME_FORMATTER.format(parsedDateTime));
    }
}
