package com.velokofi.events.controller;

import com.velokofi.events.Application;
import com.velokofi.events.model.*;
import com.velokofi.events.model.hungryvelos.LeaderBoard;
import com.velokofi.events.model.hungryvelos.RogueActivities;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamMember;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import com.velokofi.events.persistence.TeamsRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.velokofi.events.util.Formatter.convertMetersToKilometers;
import static com.velokofi.events.util.Formatter.humanReadableFormat;
import static com.velokofi.events.util.NumberCruncher.*;
import static java.util.stream.Collectors.*;

@RestController
@Getter
@Setter
public final class HungryVelosController {

    private static final Logger LOG = LoggerFactory.getLogger(HungryVelosController.class);

    private final RestTemplate restTemplate;

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    public HungryVelosController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("name", principal.getAttribute("name"));
    }

    @GetMapping("/")
    public ModelAndView execute(/*@RegisteredOAuth2AuthorizedClient final OAuth2AuthorizedClient client*/) throws Exception {
        final LeaderBoard leaderBoard = new LeaderBoard();

        final List<Team> teams = teamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());
        /*final Optional<TeamMember> teamMemberLogin = teamMembers.stream().filter(tm -> String.valueOf(tm.getId()).equals(client.getPrincipalName())).findFirst();

        LOG.info("Team member logged in? " + teamMemberLogin.isPresent() + ", strava id: " + client.getPrincipalName());

        final String tokenValue = client.getAccessToken().getTokenValue();

        final String profileResponse = getResponse(tokenValue, "https://www.strava.com/api/v3/athlete");
        final AthleteProfile athleteProfile = Application.MAPPER.readValue(profileResponse, AthleteProfile.class);

        leaderBoard.setAthleteProfile(athleteProfile);

        if (teamMemberLogin.isPresent()) {
            final OAuthorizedClient OAuthorizedClient = new OAuthorizedClient();
            OAuthorizedClient.setPrincipalName(client.getPrincipalName());
            OAuthorizedClient.setBytes(com.velokofi.events.model.OAuthorizedClient.toBytes(client));
            authorizedClientRepo.save(OAuthorizedClient);
        }*/

        /*if (teamMemberLogin.isPresent()) {
            for (int page = 1; ; page++) {
                final StringBuilder url = new StringBuilder();
                url.append("https://www.strava.com/api/v3/athlete/activities");
                url.append("?per_page=200");
                url.append("&after=").append(Application.START_TIMESTAMP);
                url.append("&before=").append(Application.END_TIMESTAMP);
                url.append("&page=").append(page);

                LOG.debug("Hitting url: " + url);

                final String activitiesResponse = getResponse(tokenValue, url.toString());

                final AthleteActivity[] activitiesArray = Application.MAPPER.readValue(activitiesResponse, AthleteActivity[].class);
                Stream.of(activitiesArray)
                        .filter(a -> ((Long) a.getAthlete().getId() != null) && a.getType().equalsIgnoreCase("ride"))
                        .forEach(activity -> athleteActivityRepo.save(activity));

                if (activitiesArray.length < 200) {
                    break;
                }
            }
        }*/

        final List<AthleteActivity> activities = athleteActivityRepo.findAll().stream().filter(
                a -> ((Long) a.getAthlete().getId()) != null
        ).collect(toList());
        LOG.info("Fetched " + activities.size() + " activities from db...");
        LOG.debug("Activities: " + activities);

        final Field[] rogueActivitiesAsFields = RogueActivities.class.getDeclaredFields();
        LOG.info("Adding " + rogueActivitiesAsFields.length + " rogue activities...");
        for (final Field field : rogueActivitiesAsFields) {
            final AthleteActivity rogueActivity = Application.MAPPER.readValue(field.get(null).toString(), AthleteActivity.class);
            activities.add(rogueActivity);
        }

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
        //mav.addObject("principalName", client.getPrincipalName());
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

}
