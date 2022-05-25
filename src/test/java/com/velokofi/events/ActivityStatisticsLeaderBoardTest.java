package com.velokofi.events;

import com.velokofi.events.model.ActivityStatistics;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamMember;
import com.velokofi.events.persistence.TeamsRepository;
import com.velokofi.events.util.NumberCruncher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
        final TeamsRepository teamsRepository = new TeamsRepository();
        final List<Team> teams = teamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());

        final Path path = Paths.get("src", "test", "resources", "activities", "allActivityStats.json");
        final byte[] bytes = Files.readAllBytes(path);
        final ActivityStatistics[] activityStats = Application.MAPPER.readValue(bytes, ActivityStatistics[].class);
        //System.out.println("Read " + activityStats.length + " activity stats");

        Arrays.sort(activityStats, (o1, o2) -> Float.compare(o2.getYtd_ride_totals().getDistance(), o1.getYtd_ride_totals().getDistance()));

        final List<String> list = Arrays.stream(activityStats).map(
                a -> getAthleteStatisticsSummary(a, teamMembers)
        ).collect(toList());

        list.forEach(System.out::println);
    }

    public String getAthleteStatisticsSummary(final ActivityStatistics activityStatistics, final List<TeamMember> teamMembers) {
        final StringBuilder sb = new StringBuilder();
        final String athleteId = activityStatistics.getAthleteId();
        //sb.append(athleteId).append(SEPARATOR);
        sb.append(NumberCruncher.getNameFromId(Long.parseLong(athleteId), teamMembers)).append(SEPARATOR);

        final BigDecimal ytdDistance = new BigDecimal(convertMetersToKilometers(
                NumberCruncher.getValue(Application.MetricType.DISTANCE, activityStatistics.getYtd_ride_totals().getDistance()
                )));
        sb.append(ytdDistance).append(SEPARATOR);

        final BigDecimal allTimeDistance = new BigDecimal(convertMetersToKilometers(
                NumberCruncher.getValue(Application.MetricType.DISTANCE, activityStatistics.getAll_ride_totals().getDistance()
                )));
        sb.append(allTimeDistance);
        return sb.toString();
    }

}
