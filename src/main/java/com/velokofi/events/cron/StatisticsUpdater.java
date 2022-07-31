package com.velokofi.events.cron;

import com.velokofi.events.VeloKofiEventsApplication;
import com.velokofi.events.model.ActivityStatistics;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.persistence.ActivityStatisticsRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Getter
@Setter
public final class StatisticsUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsUpdater.class);

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;

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
