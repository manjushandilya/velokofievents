package com.velokofi.events.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.AthleteProfile;
import com.velokofi.events.model.AthleteSummary;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.model.hungryvelos.LeaderBoard;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamMember;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import com.velokofi.events.persistence.TeamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.velokofi.events.util.Formatter.humanReadableFormat;
import static com.velokofi.events.util.NumberCruncher.averagingAggregateDouble;
import static com.velokofi.events.util.NumberCruncher.getAthleteAggregate;
import static com.velokofi.events.util.NumberCruncher.getAthleteAggregateDouble;
import static com.velokofi.events.util.NumberCruncher.getNameFromId;
import static com.velokofi.events.util.NumberCruncher.getTeamMemberCount;
import static com.velokofi.events.util.NumberCruncher.round;
import static com.velokofi.events.util.NumberCruncher.summingAggregateDouble;
import static com.velokofi.events.util.NumberCruncher.summingAggregateLong;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RestController
public final class HungryVelosController {

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

    @GetMapping("/hungryvelos")
    public ModelAndView build(@RegisteredOAuth2AuthorizedClient final OAuth2AuthorizedClient client,
                              @RequestParam(required = false, defaultValue = "false") boolean debug) throws Exception {

        final List<Team> teams = teamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());
        final Optional<TeamMember> teamMemberLogin = teamMembers.stream().filter(tm -> String.valueOf(tm.getId()).equals(client.getPrincipalName())).findFirst();

        System.out.println("Team member logged in? " + teamMemberLogin.isPresent() + ", strava id: " + client.getPrincipalName());

        final LeaderBoard leaderBoard = new LeaderBoard();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final String tokenValue = client.getAccessToken().getTokenValue();

        final String profileResponse = getResponse(tokenValue, "https://www.strava.com/api/v3/athlete");
        final AthleteProfile athleteProfile = mapper.readValue(profileResponse, AthleteProfile.class);

        leaderBoard.setAthleteProfile(athleteProfile);

        if (teamMemberLogin.isPresent()) {
            final OAuthorizedClient OAuthorizedClient = new OAuthorizedClient();
            OAuthorizedClient.setPrincipalName(client.getPrincipalName());
            OAuthorizedClient.setBytes(com.velokofi.events.model.OAuthorizedClient.toBytes(client));
            authorizedClientRepo.save(OAuthorizedClient);
        }

        if (teamMemberLogin.isPresent() && Boolean.getBoolean("events.fetch.activity.on.login")) {
            for (int page = 1; ; page++) {
                final StringBuilder url = new StringBuilder();
                url.append("https://www.strava.com/api/v3/athlete/activities");
                url.append("?per_page=200");
                url.append("&after=").append("1609631999"); // Start of 3 Jan 2021
                url.append("&page=").append(page);

                if (debug) {
                    System.out.println("Hitting url: " + url);
                }

                final String activitiesResponse = getResponse(tokenValue, url.toString());

                if (debug) {
                    System.out.println(activitiesResponse);
                }

                final AthleteActivity[] activitiesArray = mapper.readValue(activitiesResponse, AthleteActivity[].class);
                Stream.of(activitiesArray)
                        .filter(a -> ((Long) a.getAthlete().getId() != null) && a.getType().equalsIgnoreCase("ride"))
                        .forEach(activity -> athleteActivityRepo.save(activity));

                if (activitiesArray.length < 200) {
                    break;
                }
            }
        }

        final List<AthleteActivity> activities = athleteActivityRepo.findAll().stream().filter(
                a -> ((Long) a.getAthlete().getId()) != null
        ).collect(toList());
        System.out.println("Fetched " + activities.size() + " activities from db...");
        //System.out.println("Activities: " + activities);

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
            final Map<Long, Double> athleteDistanceMap = activities.stream().collect(
                    groupingBy(a -> a.getAthlete().getId(), summingDouble(a -> a.getDistance() / 1000))
            );
            //System.out.println("athleteDistanceMap: " + athleteDistanceMap);

            // Calculate athlete elevation
            final Map<Long, Double> athleteElevationMap = activities.stream().collect(
                    groupingBy(a -> a.getAthlete().getId(), summingDouble(a -> a.getTotal_elevation_gain()))
            );
            //System.out.println("athleteElevationMap: " + athleteElevationMap);

            // Calculate athlete average speed
            final Map<Long, Double> athleteAvgSpeedMap = activities.stream().collect(
                    groupingBy(a -> a.getAthlete().getId(), averagingDouble(a -> a.getAverage_speed() * 3.6))
            );
            //System.out.println("athleteAvgSpeedMap: " + athleteAvgSpeedMap);

            // Calculate athlete ride count
            final Map<Long, Long> athleteRideCountMap = activities.stream().collect(
                    groupingBy(a -> a.getAthlete().getId(), Collectors.counting())
            );
            //System.out.println("athleteRideCountMap: " + athleteRideCountMap);

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
                athleteSummaries.add(summary);
            }

            final Comparator<AthleteSummary> sortByDistance = (as1, as2) -> (int) (as2.getDistance() - as1.getDistance());
            Collections.sort(athleteSummaries, sortByDistance);

            leaderBoard.setAthleteSummaries(athleteSummaries);

            // Calculate team distance
            final Map<String, Double> teamDistanceMap = teams.stream().collect(
                    groupingBy(t -> t.getName(), summingDouble(t -> getAthleteAggregateDouble(t, athleteDistanceMap)))
            );
            //System.out.println("teamDistanceMap: " + teamDistanceMap);
            leaderBoard.setTeamDistanceMap(teamDistanceMap);

            // Calculate team average distance
            final Map<String, Double> teamAvgDistanceMap = teamDistanceMap.entrySet().stream().collect(
                    toMap(
                            e -> e.getKey(), e -> e.getValue() / getTeamMemberCount(e.getKey(), teams)
                    )
            );
            //System.out.println("teamAvgDistanceMap: " + teamAvgDistanceMap);
            leaderBoard.setTeamAvgDistanceMap(teamAvgDistanceMap);

            // Calculate team elevation
            final Map<String, Double> teamElevationMap = teams.stream().collect(
                    groupingBy(t -> t.getName(), summingDouble(t -> getAthleteAggregateDouble(t, athleteElevationMap)))
            );
            //System.out.println("teamElevationMap: " + teamElevationMap);
            leaderBoard.setTeamElevationMap(teamElevationMap);

            // Calculate team average elevation
            final Map<String, Double> teamAvgElevationMap = teamElevationMap.entrySet().stream().collect(
                    toMap(
                            e -> e.getKey(), e -> e.getValue() / getTeamMemberCount(e.getKey(), teams)
                    )
            );
            //System.out.println("teamAvgElevationMap: " + teamAvgElevationMap);
            leaderBoard.setTeamAvgElevationMap(teamAvgElevationMap);

            // Calculate team ride count
            final Map<String, Double> teamRideCountMap = teams.stream().collect(
                    groupingBy(t -> t.getName(), summingDouble(t -> getAthleteAggregate(t, athleteRideCountMap)))
            );
            //System.out.println("teamRideCountMap: " + teamRideCountMap);
            leaderBoard.setTeamRidesMap(teamRideCountMap);

            // Calculate team average ride count
            final Map<String, Double> teamAvgRidesMap = teamRideCountMap.entrySet().stream().collect(
                    toMap(
                            e -> e.getKey(), e -> round(e.getValue() / getTeamMemberCount(e.getKey(), teams))
                    )
            );
            //System.out.println("teamAvgRidesMap: " + teamAvgRidesMap);
            leaderBoard.setTeamAvgRidesMap(teamAvgRidesMap);
        }

        leaderBoard.setMrAlemaari(summingAggregateDouble(activities, teamMembers, "M", MetricType.DISTANCE));
        leaderBoard.setMsAlemaari(summingAggregateDouble(activities, teamMembers, "F", MetricType.DISTANCE));

        leaderBoard.setBettappa(summingAggregateDouble(activities, teamMembers, "M", MetricType.ELEVATION));
        leaderBoard.setBettamma(summingAggregateDouble(activities, teamMembers, "F", MetricType.ELEVATION));

        leaderBoard.setMinchinaOtappa(averagingAggregateDouble(activities, teamMembers, "M", MetricType.AVG_SPEED));
        leaderBoard.setMinchinaOtamma(averagingAggregateDouble(activities, teamMembers, "F", MetricType.AVG_SPEED));

        leaderBoard.setMrThuliMaga(summingAggregateLong(activities, teamMembers, "M"));
        leaderBoard.setMsThuliMaga(summingAggregateLong(activities, teamMembers, "F"));

        final ModelAndView mav = new ModelAndView("hungryVelos");
        mav.addObject("leaderBoard", leaderBoard);
        mav.addObject("principalName", client.getPrincipalName());
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
