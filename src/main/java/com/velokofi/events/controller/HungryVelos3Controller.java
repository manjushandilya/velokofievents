package com.velokofi.events.controller;

import com.velokofi.events.VeloKofiEventsApplication;
import com.velokofi.events.model.*;
import com.velokofi.events.model.hungryvelos3.LeaderBoard;
import com.velokofi.events.model.hungryvelos3.RogueActivities;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.HungryVelos3TeamsRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.velokofi.events.util.Formatter.convertMetersToKilometers;
import static com.velokofi.events.util.Formatter.humanReadableFormat;
import static com.velokofi.events.util.NumberCruncher.*;
import static java.util.stream.Collectors.*;

@RestController
@Getter
@Setter
public final class HungryVelos3Controller {

    private static final Logger LOG = LoggerFactory.getLogger(HungryVelos3Controller.class);

    @Autowired
    private HungryVelos3TeamsRepository hungryVelos3TeamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepository;

    public HungryVelos3Controller() {
    }

    @GetMapping("/hungryvelos3")
    public ModelAndView execute() throws Exception {
        final LeaderBoard leaderBoard = new LeaderBoard();
        final List<AthleteActivity> allActivities = athleteActivityRepository.findAll();
        final List<AthleteActivity> activities = allActivities.stream().filter(a -> ((Long) a.getAthlete().getId()) != null).collect(toList());
        LOG.info("Fetched " + activities.size() + " activities...");

        final Field[] rogueActivitiesAsFields = RogueActivities.class.getDeclaredFields();
        LOG.debug("Adding " + rogueActivitiesAsFields.length + " rogue activities...");
        for (final Field field : rogueActivitiesAsFields) {
            final AthleteActivity rogueActivity = VeloKofiEventsApplication.MAPPER.readValue(field.get(null).toString(), AthleteActivity.class);
            activities.add(rogueActivity);
        }

        final List<Team> teams = hungryVelos3TeamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());

        { // event totals
            final Double totalDistance = round(activities.stream().collect(summingDouble(a -> a.getDistance())) / 1000L);

            final Double totalElevation = round(activities.stream().collect(summingDouble(a -> a.getTotal_elevation_gain())));

            final int totalRides = activities.size();

            final long movingTimeInSeconds = activities.stream().collect(summingLong(a -> a.getMoving_time()));
            final long movingTimeInHours = movingTimeInSeconds / 3600L;

            leaderBoard.setTotalDistance(totalDistance);
            leaderBoard.setTotalElevation(totalElevation);
            leaderBoard.setTotalRides(totalRides);
            leaderBoard.setMovingTime(movingTimeInHours);
            leaderBoard.setMovingTimeInHumanReadableFormat(humanReadableFormat(Duration.ofSeconds(movingTimeInSeconds)));

            final int memberCount = teams.stream().collect(summingInt(t -> t.getMembers().size()));
            leaderBoard.setRiderAverage(round(totalDistance / memberCount));
            leaderBoard.setRiderCount(memberCount);
        }

        {
            // Calculate athlete distance
            final Map<Long, Long> athleteDistanceInMetersMap = activities.stream().collect(
                    groupingBy(a -> a.getAthlete().getId(), summingLong(a -> a.getDistance()))
            );
            final Map<Long, Double> athleteDistanceMap = athleteDistanceInMetersMap.entrySet().stream().collect(
                    Collectors.toMap(
                            e -> e.getKey(),
                            e -> new BigDecimal(convertMetersToKilometers(e.getValue())).doubleValue())
            );
            LOG.debug("athleteDistanceMap: " + athleteDistanceMap);

            // Calculate athlete elevation
            final Map<Long, Double> athleteElevationMap = activities.stream().collect(
                    groupingBy(a -> a.getAthlete().getId(), summingDouble(a -> a.getTotal_elevation_gain()))
            );
            LOG.debug("athleteElevationMap: " + athleteElevationMap);

            // Calculate athlete average speed
            final Map<Long, Double> athleteAvgSpeedMap = activities.stream().collect(
                    groupingBy(a -> a.getAthlete().getId(), averagingDouble(a -> a.getAverage_speed() * 3.6))
            );
            LOG.debug("athleteAvgSpeedMap: " + athleteAvgSpeedMap);

            // Calculate athlete ride count
            final Map<Long, Long> athleteRideCountMap = activities.stream().collect(
                    groupingBy(a -> a.getAthlete().getId(), Collectors.counting())
            );
            LOG.debug("athleteRideCountMap: " + athleteRideCountMap);

            final List<AthleteSummary> athleteSummaries = new ArrayList<>();
            for (final TeamMember tm : teamMembers) {
                final AthleteSummary summary = new AthleteSummary();
                final long id = tm.getId();
                summary.setId(id);
                summary.setName(getNameFromId(id, teamMembers));
                summary.setDistance(round(athleteDistanceMap.containsKey(id) ? athleteDistanceMap.get(id) : 0.0D));
                summary.setElevation(round(athleteElevationMap.containsKey(id) ? athleteElevationMap.get(id) : 0.0D));
                summary.setAvgSpeed(round(athleteAvgSpeedMap.containsKey(id) ? athleteAvgSpeedMap.get(id) : 0.0D));
                summary.setGender(tm.getGender());
                summary.setCaptain(tm.isCaptain());
                summary.setRides(athleteRideCountMap.containsKey(id) ? athleteRideCountMap.get(id) : 0);
                summary.setTeam(tm.getTeam());
                athleteSummaries.add(summary);
            }

            final Comparator<AthleteSummary> sortByDistance = (as1, as2) -> (int) (as2.getDistance() - as1.getDistance());
            Collections.sort(athleteSummaries, sortByDistance);

            leaderBoard.setAthleteSummaries(athleteSummaries);

            // Calculate team distance
            final Map<String, Double> teamDistanceMap = teams.stream().collect(
                    groupingBy(t -> t.getName(), summingDouble(t -> getAthleteAggregateDouble(t, athleteDistanceMap)))
            );
            LOG.debug("teamDistanceMap: " + teamDistanceMap);
            leaderBoard.setTeamDistanceMap(teamDistanceMap);

            // Calculate team average distance
            final Map<String, Double> teamAvgDistanceMap = teamDistanceMap.entrySet().stream().collect(
                    toMap(
                            e -> e.getKey(), e -> round(e.getValue() / getTeamMemberCount(e.getKey(), teams))
                    )
            );
            LOG.debug("teamAvgDistanceMap: " + teamAvgDistanceMap);
            leaderBoard.setTeamAvgDistanceMap(teamAvgDistanceMap);

            // Calculate team elevation
            final Map<String, Double> teamElevationMap = teams.stream().collect(
                    groupingBy(t -> t.getName(), summingDouble(t -> getAthleteAggregateDouble(t, athleteElevationMap)))
            );
            LOG.debug("teamElevationMap: " + teamElevationMap);
            leaderBoard.setTeamElevationMap(teamElevationMap);

            // Calculate team average elevation
            final Map<String, Double> teamAvgElevationMap = teamElevationMap.entrySet().stream().collect(
                    toMap(
                            e -> e.getKey(), e -> round(e.getValue() / getTeamMemberCount(e.getKey(), teams))
                    )
            );
            LOG.debug("teamAvgElevationMap: " + teamAvgElevationMap);
            leaderBoard.setTeamAvgElevationMap(teamAvgElevationMap);

            // Calculate team ride count
            final Map<String, Double> teamRideCountMap = teams.stream().collect(
                    groupingBy(t -> t.getName(), summingDouble(t -> getAthleteAggregate(t, athleteRideCountMap)))
            );
            LOG.debug("teamRideCountMap: " + teamRideCountMap);
            leaderBoard.setTeamRidesMap(teamRideCountMap);

            // Calculate team average ride count
            final Map<String, Double> teamAvgRidesMap = teamRideCountMap.entrySet().stream().collect(
                    toMap(
                            e -> e.getKey(), e -> round(e.getValue() / getTeamMemberCount(e.getKey(), teams))
                    )
            );
            LOG.debug("teamAvgRidesMap: " + teamAvgRidesMap);
            leaderBoard.setTeamAvgRidesMap(teamAvgRidesMap);

            // Calculate team average speed
            final Map<String, Double> teamAvgSpeedMap = teams.stream().collect(
                    groupingBy(t -> t.getName(), averagingDouble(t -> getAthleteAverageDouble(t, athleteAvgSpeedMap)))
            );
            LOG.debug("teamAvgSpeedMap: " + teamAvgSpeedMap);

            // Calculate team summaries
            final List<TeamSummary> teamSummaries = new ArrayList<>();
            teams.forEach(team -> {
                final TeamSummary teamSummary = new TeamSummary();
                teamSummary.setId(team.getId());
                teamSummary.setName(team.getName());
                teamSummary.setDistance(round(teamDistanceMap.get(team.getName())));
                teamSummary.setAvgDistance(teamAvgDistanceMap.get(team.getName()));
                teamSummary.setElevation(round(teamElevationMap.get(team.getName())));
                teamSummary.setAvgElevation(teamAvgElevationMap.get(team.getName()));
                teamSummary.setRides(teamRideCountMap.get(team.getName()).intValue());
                teamSummary.setAvgRides(teamAvgRidesMap.get(team.getName()));
                teamSummary.setAvgSpeed(round(teamAvgSpeedMap.get(team.getName())));
                teamSummary.setMemberCount(team.getMembers().size());
                teamSummaries.add(teamSummary);
            });

            leaderBoard.setTeamSummaries(teamSummaries);
            LOG.debug("teamSummaries: " + teamSummaries);
        }

        leaderBoard.setMrAlemaari(summingAggregateDouble(activities, teamMembers, "M", VeloKofiEventsApplication.MetricType.DISTANCE));
        leaderBoard.setMsAlemaari(summingAggregateDouble(activities, teamMembers, "F", VeloKofiEventsApplication.MetricType.DISTANCE));

        leaderBoard.setBettappa(summingAggregateDouble(activities, teamMembers, "M", VeloKofiEventsApplication.MetricType.ELEVATION));
        leaderBoard.setBettamma(summingAggregateDouble(activities, teamMembers, "F", VeloKofiEventsApplication.MetricType.ELEVATION));

        leaderBoard.setMinchinaOtappa(averagingAggregateDouble(activities, teamMembers, "M", VeloKofiEventsApplication.MetricType.AVG_SPEED));
        leaderBoard.setMinchinaOtamma(averagingAggregateDouble(activities, teamMembers, "F", VeloKofiEventsApplication.MetricType.AVG_SPEED));

        leaderBoard.setMrThuliMaga(summingAggregateLong(activities, teamMembers, "M"));
        leaderBoard.setMsThuliMaga(summingAggregateLong(activities, teamMembers, "F"));

        final ModelAndView mav = new ModelAndView("hv2023");
        mav.addObject("leaderBoard", leaderBoard);
        return mav;
    }

}
