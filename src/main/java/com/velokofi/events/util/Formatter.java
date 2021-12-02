package com.velokofi.events.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Formatter {

    public static String humanReadableFormat(final Duration duration) {
        return String.format("%sd %sh %sm", duration.toDays(),
                duration.toHours() - TimeUnit.DAYS.toHours(duration.toDays()),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }

}
