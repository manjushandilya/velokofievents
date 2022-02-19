package com.velokofi.events;

import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.PushTheLimit;
import com.velokofi.events.model.hungryvelos.RogueActivities;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamMember;
import com.velokofi.events.persistence.TeamsRepository;
import com.velokofi.events.util.Formatter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class PushTheLimits {

    @Test
    public void test() throws Exception {
        final TeamsRepository teamsRepository = new TeamsRepository();
        final List<Team> teams = teamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());

        final Path path = Paths.get("src", "test", "resources", "activities/hungryVelos2.json");
        final byte[] bytes = Files.readAllBytes(path);
        final AthleteActivity[] allActivitiesArray = Application.MAPPER.readValue(bytes, AthleteActivity[].class);
        final List<AthleteActivity> allActivities = Arrays.stream(allActivitiesArray).collect(toList());

        final Field[] rogueActivitiesAsFields = RogueActivities.class.getDeclaredFields();
        for (final Field field : rogueActivitiesAsFields) {
            final AthleteActivity rogueActivity = Application.MAPPER.readValue(field.get(null).toString(), AthleteActivity.class);
            allActivities.add(rogueActivity);
        }

        System.out.println("Read " + allActivities.size() + " activities");

        final LocalDateTime phaseOneAfter = Formatter.parse("2022-01-08T00:00:00Z");
        final LocalDateTime phaseOneBefore = Formatter.parse("2022-02-12T23:59:59Z");

        final List<AthleteActivity> phaseOneActivities = allActivities.stream()
                .filter(a -> Formatter.isBetween(phaseOneAfter, phaseOneBefore, Formatter.parse(a.getStart_date_local())))
                .collect(Collectors.toList());

        System.out.println("Filtered " + phaseOneActivities.size() + " activities for phase 1");

        final LocalDateTime phaseTwoAfter = Formatter.parse("2022-02-13T00:00:00Z");
        final LocalDateTime phaseTwoBefore = Formatter.parse("2022-02-18T23:59:59Z");

        final List<AthleteActivity> phaseTwoActivities = allActivities.stream()
                .filter(a -> Formatter.isBetween(phaseTwoAfter, phaseTwoBefore, Formatter.parse(a.getStart_date_local())))
                .collect(Collectors.toList());

        System.out.println("Filtered " + phaseTwoActivities.size() + " activities for phase 2");
        System.out.println("VeloKofian,Phase 1 Total Distance,Phase 2 Total Distance,Ratio (Phase 2 Total Distance / Phase 1 Total Distance");

        final List<PushTheLimit> pushTheLimits = new ArrayList<>();

        for (final TeamMember teamMember : teamMembers) {
            final long athleteId = teamMember.getId();
            final List<AthleteActivity> athletePhaseOneActivities = phaseOneActivities.stream().filter(a -> a.getAthlete().getId() == athleteId).collect(toList());
            final Long phaseOneDistance = athletePhaseOneActivities.stream().map(a -> a.getDistance()).collect(Collectors.summingLong(Long::longValue));

            final List<AthleteActivity> athletePhaseTwoActivities = phaseTwoActivities.stream().filter(a -> a.getAthlete().getId() == athleteId).collect(toList());
            final Long phaseTwoDistance = athletePhaseTwoActivities.stream().map(a -> a.getDistance()).collect(Collectors.summingLong(Long::longValue));

            //System.out.println(teamMember.getName() + "," + phaseOneDistance + "," + phaseTwoDistance + "," + ((double) phaseTwoDistance / phaseOneDistance));
            final PushTheLimit pushTheLimit = new PushTheLimit(phaseOneDistance, phaseTwoDistance, teamMember.getName());
            pushTheLimits.add(pushTheLimit);
        }

        Collections.sort(pushTheLimits);

        pushTheLimits.stream().forEach(System.out::println);

    }

}
