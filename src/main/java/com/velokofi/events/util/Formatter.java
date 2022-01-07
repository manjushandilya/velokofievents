package com.velokofi.events.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
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

}
