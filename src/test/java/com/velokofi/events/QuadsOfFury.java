package com.velokofi.events;

import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamMember;
import com.velokofi.events.persistence.TeamsRepository;
import com.velokofi.events.util.Formatter;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.velokofi.events.util.NumberCruncher.*;
import static java.util.stream.Collectors.toList;

public class QuadsOfFury {

    @Test
    public void test() throws Exception {
        final TeamsRepository teamsRepository = new TeamsRepository();
        final List<Team> teams = teamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());

        final Path path = Paths.get("src", "test", "resources", "activities/allActivitiesTill15022022.json");
        final byte[] bytes = Files.readAllBytes(path);
        final AthleteActivity[] allActivities = VeloKofiEventsApplication.MAPPER.readValue(bytes, AthleteActivity[].class);
        System.out.println("Read " + allActivities.length + " activities");

        final LocalDateTime after = Formatter.parse("2022-02-03T00:00:00Z");
        final LocalDateTime before = Formatter.parse("2022-02-10T23:59:59Z");

        final List<AthleteActivity> factivities = Arrays.stream(allActivities)
                .filter(a -> a.getType().equalsIgnoreCase("Ride"))
                .filter(a -> filterBasedOnGender(a.getAthlete(), teamMembers, "F") && a.getDistance() >= 40 * 1000)
                .filter(a -> Formatter.isBetween(after, before, Formatter.parse(a.getStart_date_local())))
                .collect(Collectors.toList());

        System.out.println("Selected " + factivities.size() + " activities for female gender:");

        final List<Map.Entry<String, Double>> fsentries = averagingAggregateDoubleNoLimit(factivities, teamMembers, "F", VeloKofiEventsApplication.MetricType.AVG_SPEED);
        final List<Map.Entry<String, Double>> fdentries = summingAggregateDoubleNoLimit(factivities, teamMembers, "F", VeloKofiEventsApplication.MetricType.DISTANCE);
        for (final Map.Entry<String, Double> fsentry : fsentries) {
            final Double distance = getValue(fsentry.getKey(), fdentries);
            if (distance >= 200D) {
                System.out.println(fsentry.getKey() + "," + round(fsentry.getValue()) + "," + round(distance));
            }
        }

        final List<AthleteActivity> mactivities = Arrays.stream(allActivities)
                .filter(a -> a.getType().equalsIgnoreCase("Ride"))
                .filter(a -> filterBasedOnGender(a.getAthlete(), teamMembers, "M") && a.getDistance() >= 75 * 1000)
                .filter(a -> Formatter.isBetween(after, before, Formatter.parse(a.getStart_date_local())))
                .collect(Collectors.toList());

        System.out.println("Selected " + mactivities.size() + " activities for male gender:");

        final List<Map.Entry<String, Double>> msentries = averagingAggregateDoubleNoLimit(mactivities, teamMembers, "M", VeloKofiEventsApplication.MetricType.AVG_SPEED);
        final List<Map.Entry<String, Double>> mdentries = summingAggregateDoubleNoLimit(mactivities, teamMembers, "M", VeloKofiEventsApplication.MetricType.DISTANCE);
        for (final Map.Entry<String, Double> msentry : msentries) {
            final Double distance = getValue(msentry.getKey(), mdentries);
            if (distance >= 200D) {
                System.out.println(msentry.getKey() + "," + round(msentry.getValue()) + "," + round(distance));
            }
        }
    }

    private Double getValue(final String key, final List<Map.Entry<String, Double>> entries) {
        for (final Map.Entry<String, Double> entry : entries) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

}
