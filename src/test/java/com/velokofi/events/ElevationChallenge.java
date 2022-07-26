package com.velokofi.events;

import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamMember;
import com.velokofi.events.persistence.HungryVelos2022TeamsRepository;
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

import static com.velokofi.events.util.NumberCruncher.summingAggregateDouble;
import static java.util.stream.Collectors.toList;

public class ElevationChallenge {

    @Test
    public void testBettappa() throws Exception {
        final HungryVelos2022TeamsRepository hungryVelos2022TeamsRepository = new HungryVelos2022TeamsRepository();
        final List<Team> teams = hungryVelos2022TeamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());

        final Path path = Paths.get("src", "test", "resources", "activities/allActivitiesTill25012022.json");
        final byte[] bytes = Files.readAllBytes(path);
        final AthleteActivity[] allActivities = VeloKofiEventsApplication.MAPPER.readValue(bytes, AthleteActivity[].class);
        System.out.println("Read " + allActivities.length + " activities");

        final LocalDateTime after = Formatter.parse("2022-01-18T00:00:00Z");
        final LocalDateTime before = Formatter.parse("2022-01-24T23:59:59Z");

        final List<AthleteActivity> activities = Arrays.stream(allActivities)
                .filter(a -> Formatter.isBetween(after, before, Formatter.parse(a.getStart_date_local())))
                .collect(Collectors.toList());

        final List<Map.Entry<String, Double>> entries = summingAggregateDouble(activities, teamMembers, "M", VeloKofiEventsApplication.MetricType.ELEVATION);
        System.out.println(entries);
    }

    @Test
    public void testBettamma() throws Exception {
        final HungryVelos2022TeamsRepository hungryVelos2022TeamsRepository = new HungryVelos2022TeamsRepository();
        final List<Team> teams = hungryVelos2022TeamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());

        final Path path = Paths.get("src", "test", "resources", "activities/allActivitiesTill25012022.json");
        final byte[] bytes = Files.readAllBytes(path);
        final AthleteActivity[] allActivities = VeloKofiEventsApplication.MAPPER.readValue(bytes, AthleteActivity[].class);
        System.out.println("Read " + allActivities.length + " activities");

        final LocalDateTime after = Formatter.parse("2022-01-18T00:00:00Z");
        final LocalDateTime before = Formatter.parse("2022-01-24T23:59:59Z");

        final List<AthleteActivity> activities = Arrays.stream(allActivities)
                .filter(a -> Formatter.isBetween(after, before, Formatter.parse(a.getStart_date_local())))
                .collect(Collectors.toList());

        final List<Map.Entry<String, Double>> entries = summingAggregateDouble(activities, teamMembers, "F", VeloKofiEventsApplication.MetricType.ELEVATION);
        System.out.println(entries);
    }

    @Test()
    public void testDateFormat() {
        System.out.println(Formatter.parse("2022-01-24T15:53:53Z"));
    }

}
