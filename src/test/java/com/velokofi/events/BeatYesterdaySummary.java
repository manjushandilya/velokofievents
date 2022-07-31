package com.velokofi.events;

import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.util.Formatter;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import static com.velokofi.events.VeloKofiEventsApplication.MAPPER;

public class BeatYesterdaySummary {

    @Test
    public void test() throws Exception {
        final Path path = Paths.get("src", "main", "resources", "static", "json", "beatYesterdayBeta.json");
        final byte[] bytes = Files.readAllBytes(path);
        final AthleteActivity[] athleteActivities = MAPPER.readValue(bytes, AthleteActivity[].class);
        Arrays.sort(athleteActivities, Comparator.comparing(aa -> Formatter.parse(aa.getStart_date_local())));
        //System.out.println(athleteActivities.length);
        Arrays.stream(athleteActivities).forEach(
                a -> System.out.println(a.toCSV())
        );
    }

}
