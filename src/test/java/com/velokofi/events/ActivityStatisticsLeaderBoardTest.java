package com.velokofi.events;

import com.velokofi.events.model.ActivityStatistics;
import com.velokofi.events.model.ActivityStatisticsSummary;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.velokofi.events.util.Formatter.convertMetersToKilometers;
import static java.util.stream.Collectors.toList;

public class ActivityStatisticsLeaderBoardTest {

    public static final String SEPARATOR = ",";

    @Test
    public void test() throws Exception {
        final Path path = Paths.get("src", "test", "resources", "activities", "allActivityStats.json");
        final byte[] bytes = Files.readAllBytes(path);
        final ActivityStatistics[] activityStats = VeloKofiEventsApplication.MAPPER.readValue(bytes, ActivityStatistics[].class);
        //System.out.println("Read " + activityStats.length + " activity stats");

        Arrays.sort(activityStats, (o1, o2) -> Float.compare(o2.getYtd_ride_totals().getDistance(), o1.getYtd_ride_totals().getDistance()));

        final List<String> list = Arrays.stream(activityStats).map(
                a -> getAthleteStatisticsSummary(a)
        ).collect(toList());

        list.forEach(System.out::println);
    }

    public String getAthleteStatisticsSummary(final ActivityStatistics activityStatistics) {
        final StringBuilder sb = new StringBuilder();

        final ActivityStatisticsSummary summary = new ActivityStatisticsSummary(activityStatistics);
        sb.append(summary.getAthleteName()).append(SEPARATOR);
        sb.append(summary.getYtdDistance()).append(SEPARATOR);
        sb.append(summary.getAthleteName()).append(SEPARATOR);
        sb.append(summary.getPercentComplete()).append(SEPARATOR);
        sb.append(summary.getPercentCompleteString());
        return sb.toString();
    }

}
