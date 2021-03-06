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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Getter
@Setter
public final class StatisticsUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityUpdater.class);

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;

    @Scheduled(cron = "0 0 0 * * *")
    public void run() throws Exception {
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
                    LOG.info("Refreshing auth token for clientId: " + clientId + ", old value: " + getTokenValue(clientId));
                    try {
                        refreshToken(clientId);
                        LOG.debug("Successfully refreshed token for clientId: " + clientId + ", new value: " + getTokenValue(clientId));
                    } catch (final Exception re) {
                        LOG.error("Error while refreshing token for clientId: " + clientId + " " + re.getMessage());
                    }
                } else {
                    LOG.error("Exception while fetching statistics for client " + clientId + ": " + e.getMessage());
                    break;
                }
            }
        }
        return null;
    }

    private ResponseEntity<String> getStatisticsResponse(final String clientId) throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getTokenValue(clientId));

        final HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(getUri(clientId), HttpMethod.GET, request, String.class);
    }

    private void refreshToken(final String clientId) throws Exception {
        final OAuthorizedClient client = authorizedClientRepo.findById(clientId).get();
        final OAuth2AuthorizedClient authorizedClient = OAuthorizedClient.fromBytes(client.getBytes());

        final StringBuilder builder = new StringBuilder();
        builder.append("https://www.strava.com/api/v3/oauth/token");

        final URI uri = new URI(builder.toString());

        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final RefreshTokenRequest requestObj = getRefreshTokenRequest(authorizedClient);
        final String body = VeloKofiEventsApplication.MAPPER.writeValueAsString(requestObj);

        LOG.debug("Refresh token request: " + body);

        final HttpEntity<String> request = new HttpEntity<>(body, headers);

        final ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        LOG.debug("Refresh token response: " + response);

        final RefreshTokenResponse refreshTokenResponse = VeloKofiEventsApplication.MAPPER.readValue(response.getBody(), RefreshTokenResponse.class);
        final Optional<OAuthorizedClient> byId = authorizedClientRepo.findById(authorizedClient.getPrincipalName());
        final String athleteName = byId.get().getAthleteName();
        authorizedClientRepo.deleteById(authorizedClient.getPrincipalName());

        final OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, refreshTokenResponse.getAccess_token(), Instant.ofEpochSecond(refreshTokenResponse.getExpires_in()), Instant.ofEpochSecond(refreshTokenResponse.getExpires_at()));

        final OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(refreshTokenResponse.getRefresh_token(), Instant.ofEpochSecond(refreshTokenResponse.getExpires_in()));

        final OAuth2AuthorizedClient newClient = new OAuth2AuthorizedClient(authorizedClient.getClientRegistration(), authorizedClient.getPrincipalName(), accessToken, refreshToken);

        final OAuthorizedClient oAuthorizedClient = new OAuthorizedClient();
        oAuthorizedClient.setAthleteName(athleteName);
        oAuthorizedClient.setPrincipalName(authorizedClient.getPrincipalName());
        oAuthorizedClient.setBytes(com.velokofi.events.model.OAuthorizedClient.toBytes(newClient));
        authorizedClientRepo.save(oAuthorizedClient);
    }

    private RefreshTokenRequest getRefreshTokenRequest(final OAuth2AuthorizedClient authorizedClient) {
        final RefreshTokenRequest requestObj = new RefreshTokenRequest();
        requestObj.setClient_id(authorizedClient.getClientRegistration().getClientId());
        requestObj.setClient_secret(authorizedClient.getClientRegistration().getClientSecret());
        requestObj.setGrant_type("refresh_token");
        requestObj.setRefresh_token(authorizedClient.getRefreshToken().getTokenValue());
        return requestObj;
    }

    private URI getUri(final String athleteId) throws URISyntaxException {
        final StringBuilder builder = new StringBuilder();
        builder.append("https://www.strava.com/api/v3/athletes/");
        builder.append(athleteId);
        builder.append("/stats");

        return new URI(builder.toString());
    }

    private String getTokenValue(final String clientId) {
        final OAuthorizedClient client = authorizedClientRepo.findById(clientId).get();
        final OAuth2AuthorizedClient entry = OAuthorizedClient.fromBytes(client.getBytes());
        final String tokenValue = entry.getAccessToken().getTokenValue();
        return tokenValue;
    }

}
