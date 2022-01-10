package com.velokofi.events.controller;

import com.velokofi.events.Application;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.AthleteSummary;
import com.velokofi.events.model.hungryvelos.LeaderBoard;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamMember;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import com.velokofi.events.persistence.TeamsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.velokofi.events.util.Formatter.convertMetersToKilometers;
import static com.velokofi.events.util.Formatter.humanReadableFormat;
import static com.velokofi.events.util.NumberCruncher.*;
import static java.util.stream.Collectors.*;

@RestController
public class HomePageController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);

    private final RestTemplate restTemplate;

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    public HomePageController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public ModelAndView execute(final HttpServletRequest request) throws Exception {

        String clientId = null;
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (cookie.getName().equals(Application.COOKIE_ID)) {
                    clientId = cookie.getValue();
                    break;
                }
            }
        }

        if (clientId == null || clientId.isBlank()) {
            return new ModelAndView("redirect:/login");
        }

        final LeaderBoard leaderBoard = new LeaderBoard();

        final List<Team> teams = teamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());
        final List<AthleteActivity> activities = athleteActivityRepo.findAll().stream().filter(
                a -> ((Long) a.getAthlete().getId()) != null
        ).collect(toList());

        LOG.info("Fetched " + activities.size() + " activities from db...");

        { // event totals
            final Double totalDistance = round(activities.stream().collect(summingDouble(a -> a.getDistance())) / 1000);

            final Double totalElevation = round(activities.stream().collect(summingDouble(a -> a.getTotal_elevation_gain())));

            final int totalRides = activities.size();

            final long movingTimeInSeconds = activities.stream().collect(summingLong(a -> a.getMoving_time()));
            final long movingTimeInHours = movingTimeInSeconds / 3600;

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
                summary.setDistance(round(athleteDistanceMap.containsKey(id) ? athleteDistanceMap.get(id) : 0));
                summary.setElevation(round(athleteElevationMap.containsKey(id) ? athleteElevationMap.get(id) : 0));
                summary.setAvgSpeed(round(athleteAvgSpeedMap.containsKey(id) ? athleteAvgSpeedMap.get(id) : 0));
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
                            e -> e.getKey(), e -> e.getValue() / getTeamMemberCount(e.getKey(), teams)
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
                            e -> e.getKey(), e -> e.getValue() / getTeamMemberCount(e.getKey(), teams)
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
        }

        leaderBoard.setMrAlemaari(summingAggregateDouble(activities, teamMembers, "M", Application.MetricType.DISTANCE));
        leaderBoard.setMsAlemaari(summingAggregateDouble(activities, teamMembers, "F", Application.MetricType.DISTANCE));

        leaderBoard.setBettappa(summingAggregateDouble(activities, teamMembers, "M", Application.MetricType.ELEVATION));
        leaderBoard.setBettamma(summingAggregateDouble(activities, teamMembers, "F", Application.MetricType.ELEVATION));

        leaderBoard.setMinchinaOtappa(averagingAggregateDouble(activities, teamMembers, "M", Application.MetricType.AVG_SPEED));
        leaderBoard.setMinchinaOtamma(averagingAggregateDouble(activities, teamMembers, "F", Application.MetricType.AVG_SPEED));

        leaderBoard.setMrThuliMaga(summingAggregateLong(activities, teamMembers, "M"));
        leaderBoard.setMsThuliMaga(summingAggregateLong(activities, teamMembers, "F"));

        final ModelAndView mav = new ModelAndView("index");
        mav.addObject("leaderBoard", leaderBoard);
        mav.addObject("principalName", clientId);
        return mav;
    }

    private String getResponse(final String tokenValue, final String url) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + tokenValue);
        HttpEntity<String> request = new HttpEntity<String>(headers);

        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody();
    }

    public enum MetricType {DISTANCE, ELEVATION, AVG_SPEED}

}
