package com.velokofi.events.cron;

import com.velokofi.events.VeloKofiEventsApplication;
import com.velokofi.events.model.ActivityStatistics;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.model.RefreshTokenRequest;
import com.velokofi.events.model.RefreshTokenResponse;
import com.velokofi.events.persistence.ActivityStatisticsRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Getter
@Setter
public final class StatisticsUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityUpdater.class);

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;

    public static final Map<String, String> STRAVA_ID_VS_NAME_MAP;

    static {
        STRAVA_ID_VS_NAME_MAP = new HashMap<>();

        STRAVA_ID_VS_NAME_MAP.put("43378948", "Shruthi Subbanna");
        STRAVA_ID_VS_NAME_MAP.put("35940578", "Sai Srinivas");
        STRAVA_ID_VS_NAME_MAP.put("37177283", "Manjunath Sathyanarayana");
        STRAVA_ID_VS_NAME_MAP.put("28234693", "Goutham YV");
        STRAVA_ID_VS_NAME_MAP.put("74963850", "Nandini Kishor");
        STRAVA_ID_VS_NAME_MAP.put("69245380", "Siddharth Horee");
        STRAVA_ID_VS_NAME_MAP.put("14463022", "Sagar MC");
        STRAVA_ID_VS_NAME_MAP.put("3225831", "Prashanth S");
        STRAVA_ID_VS_NAME_MAP.put("73010512", "Chandrashekar K");
        STRAVA_ID_VS_NAME_MAP.put("15589851", "Sun'J Sharma");
        STRAVA_ID_VS_NAME_MAP.put("3377091", "Sunil Gopala Chari");
        STRAVA_ID_VS_NAME_MAP.put("9125629", "Deepu Fixie Beardo");
        STRAVA_ID_VS_NAME_MAP.put("37178147", "Sandeep B N");
        STRAVA_ID_VS_NAME_MAP.put("58389372", "Rupesh 🏃🚴🚵🏊");
        STRAVA_ID_VS_NAME_MAP.put("6848467", "Shreenivasa K s");
        STRAVA_ID_VS_NAME_MAP.put("62516649", "Anil Kumar");
        STRAVA_ID_VS_NAME_MAP.put("20378999", "Rav Indra");
        STRAVA_ID_VS_NAME_MAP.put("62411206", "Arun Bhaskar");
        STRAVA_ID_VS_NAME_MAP.put("75083256", "Manu Gopinath");
        STRAVA_ID_VS_NAME_MAP.put("38684955", "Premchand Ryali");
        STRAVA_ID_VS_NAME_MAP.put("83728576", "Shyam Sundar K");
        STRAVA_ID_VS_NAME_MAP.put("12244586", "Kirti Chalam");
        STRAVA_ID_VS_NAME_MAP.put("64757576", "Bharadwaja S R");
        STRAVA_ID_VS_NAME_MAP.put("78896766", "Rahul Padmanabha");
        STRAVA_ID_VS_NAME_MAP.put("69757350", "Ravi Sunderrajan");
        STRAVA_ID_VS_NAME_MAP.put("51400681", "Roopa Rupesh");
        STRAVA_ID_VS_NAME_MAP.put("17249418", "Chandan Gaddehosur");
        STRAVA_ID_VS_NAME_MAP.put("75500724", "Revathi M P");
        STRAVA_ID_VS_NAME_MAP.put("65820756", "Nanda Kishor G D");
        STRAVA_ID_VS_NAME_MAP.put("65392239", "Vipin Devis");
        STRAVA_ID_VS_NAME_MAP.put("64907699", "Amarnath Vali");
        STRAVA_ID_VS_NAME_MAP.put("63014939", "Srinidhi Bharadwaj");
        STRAVA_ID_VS_NAME_MAP.put("61999014", "Vidyaprasanna Kambalur");
        STRAVA_ID_VS_NAME_MAP.put("61335755", "Sampige Santhosh");
        STRAVA_ID_VS_NAME_MAP.put("68355712", "Sandeep Vishwanath");
        STRAVA_ID_VS_NAME_MAP.put("33590187", "Faraz Umar");
        STRAVA_ID_VS_NAME_MAP.put("25409226", "Lakshmi Narasu");
        STRAVA_ID_VS_NAME_MAP.put("16222927", "Mythri Sunil");
        STRAVA_ID_VS_NAME_MAP.put("12920635", "Sukumar Sundaram");
        STRAVA_ID_VS_NAME_MAP.put("19218746", "Raghu B V");
        STRAVA_ID_VS_NAME_MAP.put("85387376", "Nischal Kumar");
        STRAVA_ID_VS_NAME_MAP.put("74710582", "Zainab Shoaib");
        STRAVA_ID_VS_NAME_MAP.put("74590313", "Venkat");
        STRAVA_ID_VS_NAME_MAP.put("73629852", "Bharathi Malavalli");
        STRAVA_ID_VS_NAME_MAP.put("73492491", "Savitha Naik");
        STRAVA_ID_VS_NAME_MAP.put("71154685", "Ashwini Nischal (Ashu)");
        STRAVA_ID_VS_NAME_MAP.put("70101578", "Chidanand Kawri Lokesh");
        STRAVA_ID_VS_NAME_MAP.put("67288162", "Harsha Gandhi");
        STRAVA_ID_VS_NAME_MAP.put("62471947", "Girish Jain Jain");
        STRAVA_ID_VS_NAME_MAP.put("38148750", "Chandu :)");
        STRAVA_ID_VS_NAME_MAP.put("33762572", "Arun Bastin");
        STRAVA_ID_VS_NAME_MAP.put("28550167", "Sudarshan Gopinath");
        STRAVA_ID_VS_NAME_MAP.put("41081264", "Subhra Kanti Das");
        STRAVA_ID_VS_NAME_MAP.put("62775438", "Sandhya Santhosh");
        STRAVA_ID_VS_NAME_MAP.put("42776148", "Manas Chand Ryali");
        STRAVA_ID_VS_NAME_MAP.put("42777287", "Manavchand Ryali");
        STRAVA_ID_VS_NAME_MAP.put("68380635", "Latha Rani");
        STRAVA_ID_VS_NAME_MAP.put("36760093", "Manasa Bharadwaj");
        STRAVA_ID_VS_NAME_MAP.put("78551177", "samrudh kishor");
        STRAVA_ID_VS_NAME_MAP.put("88072624", "Niharikaa N");
        STRAVA_ID_VS_NAME_MAP.put("88076285", "Tanu Pradeep Gorur");
        STRAVA_ID_VS_NAME_MAP.put("88054379", "Aditya V K");
        STRAVA_ID_VS_NAME_MAP.put("87964523", "Atharva Subramanya Kashyap");
        STRAVA_ID_VS_NAME_MAP.put("81228572", "Jayashree Nandakumar");
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        LOG.info("Running StatisticsUpdater scheduled task at: " + LocalDateTime.now());
        activityStatisticsRepo.deleteAll();

        final List<ActivityStatistics> activityStatisticsList = getActivityStatisticsList();
        activityStatisticsRepo.saveAll(activityStatisticsList);
    }

    public List<ActivityStatistics> getActivityStatisticsList() {
        final List<ActivityStatistics> activityStatisticsList = new ArrayList<>();
        final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
        for (final OAuthorizedClient client : clients) {
            final ActivityStatistics activityStatistics = getActivityStatistics(client);
            if (activityStatistics != null) {
                activityStatisticsList.add(activityStatistics);
            }
        }
        return activityStatisticsList;
    }

    public ActivityStatistics getActivityStatistics(final OAuthorizedClient client) {
        for (int attempt = 0; attempt < 3; attempt++) {
            final String clientId = client.getPrincipalName();
            LOG.info("Fetching statistics for clientId: " + clientId);
            try {
                final ResponseEntity<String> response = getStatisticsResponse(clientId);

                LOG.debug("Fetched statistics response: " + response);

                final ActivityStatistics activityStatistics = VeloKofiEventsApplication.MAPPER.readValue(response.getBody(), ActivityStatistics.class);
                activityStatistics.setAthleteId(clientId);
                activityStatistics.setAthleteName(client.getAthleteName());

                LOG.debug("Added activityStatisticsList bean to collection: " + activityStatistics);
                return activityStatistics;
            } catch (final Exception e) {
                if (e.getMessage().indexOf("401") > -1) {
                    LOG.info("Refreshing auth token for clientId: " + clientId + ", old value: " +
                            RefreshTokenHelper.getTokenValue(authorizedClientRepo, clientId));
                    try {
                        RefreshTokenHelper.refreshToken(authorizedClientRepo, clientId);
                        LOG.debug("Successfully refreshed token for clientId: " + clientId + ", new value: " +
                                RefreshTokenHelper.getTokenValue(authorizedClientRepo, clientId));
                    } catch (final Exception re) {
                        LOG.error("Error while refreshing token for clientId: " + clientId + " " + re.getMessage());
                    }
                } else {
                    LOG.error("Exception while fetching statistics for clientId " + clientId + ": " + e.getMessage());
                }
            }
        }
        return null;
    }

    private ResponseEntity<String> getStatisticsResponse(final String clientId) throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + RefreshTokenHelper.getTokenValue(authorizedClientRepo, clientId));

        final HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(getUri(clientId), HttpMethod.GET, request, String.class);
    }

    private URI getUri(final String athleteId) throws URISyntaxException {
        final StringBuilder builder = new StringBuilder();
        builder.append("https://www.strava.com/api/v3/athletes/");
        builder.append(athleteId);
        builder.append("/stats");

        return new URI(builder.toString());
    }

}
