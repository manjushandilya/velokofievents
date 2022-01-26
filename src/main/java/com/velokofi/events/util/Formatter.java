package com.velokofi.events.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public final class Formatter {

    public static String humanReadableFormat(final Duration duration) {
        return String.format("%sd %sh %sm", duration.toDays(),
                duration.toHours() - TimeUnit.DAYS.toHours(duration.toDays()),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }

    public static String convertMetersToKilometers(final long totalDistance) {
        final double ff = totalDistance / 1000.0;
        BigDecimal bd = BigDecimal.valueOf(ff);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return String.valueOf(bd.doubleValue());
    }

    public static LocalDateTime parse(final String input) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2022-01-24T15:53:53Z
        return LocalDateTime.parse(input, formatter);
    }

    public static boolean isBetween(final LocalDateTime after, final LocalDateTime before, final LocalDateTime actual) {
        return actual.isAfter(after) && actual.isBefore(before);
    }

}
