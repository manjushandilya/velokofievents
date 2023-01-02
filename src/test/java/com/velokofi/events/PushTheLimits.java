package com.velokofi.events;

import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.Team;
import com.velokofi.events.model.TeamMember;
import com.velokofi.events.model.PushTheLimit;
import com.velokofi.events.model.hungryvelos2.RogueActivities;
import com.velokofi.events.persistence.HungryVelos2TeamsRepository;
import com.velokofi.events.util.Formatter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class PushTheLimits {

    @Test
    public void test() throws Exception {
        final HungryVelos2TeamsRepository hungryVelos2022TeamsRepository = new HungryVelos2TeamsRepository();
        final List<Team> teams = hungryVelos2022TeamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());

        final Path path = Paths.get("src", "test", "resources", "activities/hungryVelos2.json");
        final byte[] bytes = Files.readAllBytes(path);
        final AthleteActivity[] allActivitiesArray = VeloKofiEventsApplication.MAPPER.readValue(bytes, AthleteActivity[].class);
        final List<AthleteActivity> allActivities = Arrays.stream(allActivitiesArray).collect(toList());

        final Field[] rogueActivitiesAsFields = RogueActivities.class.getDeclaredFields();
        for (final Field field : rogueActivitiesAsFields) {
            final AthleteActivity rogueActivity = VeloKofiEventsApplication.MAPPER.readValue(field.get(null).toString(), AthleteActivity.class);
            allActivities.add(rogueActivity);
        }

        System.out.println("Read " + allActivities.size() + " activities");

        final LocalDateTime phaseOneAfter = Formatter.parse("2022-01-08T00:00:00Z");
        final LocalDateTime phaseOneBefore = Formatter.parse("2022-02-12T23:59:59Z");

        final List<AthleteActivity> phaseOneActivities = allActivities.stream()
                .filter(a -> Formatter.isBetween(phaseOneAfter, phaseOneBefore, Formatter.parse(a.getStart_date_local())))
                .collect(Collectors.toList());

        // Calculate athlete elevation for phase 1
        final Map<Long, Double> phaseOneElevationMap = phaseOneActivities.stream().collect(
                groupingBy(a -> a.getAthlete().getId(), summingDouble(a -> a.getTotal_elevation_gain()))
        );

        final Map<Long, Long> phaseOneMovingTimeMap = phaseOneActivities.stream().collect(
                groupingBy(a -> a.getAthlete().getId(), summingLong(a -> a.getMoving_time()))
        );

        System.out.println("Filtered " + phaseOneActivities.size() + " activities for phase 1");

        final LocalDateTime phaseTwoAfter = Formatter.parse("2022-02-13T00:00:00Z");
        final LocalDateTime phaseTwoBefore = Formatter.parse("2022-02-18T23:59:59Z");

        final List<AthleteActivity> phaseTwoActivities = allActivities.stream()
                .filter(a -> Formatter.isBetween(phaseTwoAfter, phaseTwoBefore, Formatter.parse(a.getStart_date_local())))
                .collect(Collectors.toList());

        // Calculate athlete elevation for phase 2
        final Map<Long, Double> phaseTwoElevationMap = phaseTwoActivities.stream().collect(
                groupingBy(a -> a.getAthlete().getId(), summingDouble(a -> a.getTotal_elevation_gain()))
        );

        final Map<Long, Long> phaseTwoMovingTimeMap = phaseTwoActivities.stream().collect(
                groupingBy(a -> a.getAthlete().getId(), summingLong(a -> a.getMoving_time()))
        );

        System.out.println("Filtered " + phaseTwoActivities.size() + " activities for phase 2");
        System.out.println("VeloKofian,Distance 1,Distance 2,Ratio, Elevation 1, Elevation 2, Moving Time 1, Moving Time 2");

        final List<PushTheLimit> pushTheLimits = new ArrayList<>();

        for (final TeamMember teamMember : teamMembers) {
            final long athleteId = teamMember.getId();
            final List<AthleteActivity> athletePhaseOneActivities = phaseOneActivities.stream().filter(a -> a.getAthlete().getId() == athleteId).collect(toList());
            final Long phaseOneDistance = athletePhaseOneActivities.stream().map(a -> a.getDistance()).collect(summingLong(Long::longValue));

            final List<AthleteActivity> athletePhaseTwoActivities = phaseTwoActivities.stream().filter(a -> a.getAthlete().getId() == athleteId).collect(toList());
            final Long phaseTwoDistance = athletePhaseTwoActivities.stream().map(a -> a.getDistance()).collect(summingLong(Long::longValue));

            //System.out.println(teamMember.getName() + "," + phaseOneDistance + "," + phaseTwoDistance + "," + ((double) phaseTwoDistance / phaseOneDistance));
            final PushTheLimit pushTheLimit = new PushTheLimit();
            pushTheLimit.setTeamMemberName(teamMember.getName());
            pushTheLimit.setPhaseOneDistance(phaseOneDistance);
            pushTheLimit.setPhaseTwoDistance(phaseTwoDistance);
            pushTheLimit.setPhaseOneElevation(phaseOneElevationMap.containsKey(athleteId) ? phaseOneElevationMap.get(athleteId) : 0D);
            pushTheLimit.setPhaseTwoElevation(phaseTwoElevationMap.containsKey(athleteId) ? phaseTwoElevationMap.get(athleteId) : 0D);
            pushTheLimit.setPhaseOneMovingTime(phaseOneMovingTimeMap.containsKey(athleteId) ? phaseOneMovingTimeMap.get(athleteId) : 0L);
            pushTheLimit.setPhaseTwoMovingTime(phaseTwoMovingTimeMap.containsKey(athleteId) ? phaseTwoMovingTimeMap.get(athleteId) : 0L);
            pushTheLimits.add(pushTheLimit);
        }

        Collections.sort(pushTheLimits);

        pushTheLimits.stream().forEach(System.out::println);

    }

}
