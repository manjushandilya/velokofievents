package com.velokofi.events.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velokofi.events.Application;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.model.RefreshTokenRequest;
import com.velokofi.events.model.RefreshTokenResponse;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import com.velokofi.events.persistence.TeamsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.stream.Stream;

@RestController
public class ActivityController {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @Autowired
    private OAuthorizedClientRepository oAuthClientRepo;

    @GetMapping("/activities/{clientId}")
    public String getActivities(@PathVariable("clientId") String clientId) throws Exception {
        final AthleteActivity[] activities;
        try {
            activities = getAthleteActivities(clientId);
            if (activities.length > 0) {
                LOG.info("Saving " + activities.length + " activities for clientId: " + clientId);
                Stream.of(activities)
                        .filter(a -> Application.SUPPORTED_RIDE_TYPES.contains(a.getType()))
                        .forEach(activity -> athleteActivityRepo.save(activity));
            }
        } catch (final Exception e) {
            LOG.info("Refreshing auth token for clientId: " + clientId + ", old value: " + getTokenValue(clientId));
            refresh(clientId);
            LOG.info("Successfully refreshed token for clientId: " + clientId + ", new value: " + getTokenValue(clientId));
            return getActivities(clientId);
        }
        return Application.MAPPER.writeValueAsString(activities);
    }

    private AthleteActivity[] getAthleteActivities(final String clientId) throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getTokenValue(clientId));

        final HttpEntity<String> request = new HttpEntity<>(headers);
        final ResponseEntity<String> response = restTemplate.exchange(getUri(1), HttpMethod.GET, request, String.class);
        return Application.MAPPER.readValue(response.getBody(), AthleteActivity[].class);
    }

    private void refresh(final String clientId) throws Exception {
        final OAuthorizedClient client = oAuthClientRepo.findById(clientId).get();
        final OAuth2AuthorizedClient authorizedClient = OAuthorizedClient.fromBytes(client.getBytes());
        final ObjectMapper mapper = new ObjectMapper();

        final StringBuilder builder = new StringBuilder();
        builder.append("https://www.strava.com/api/v3/oauth/token");

        URI uri = new URI(builder.toString());

        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final RefreshTokenRequest requestObj = getRefreshTokenRequest(authorizedClient);
        final String body = mapper.writeValueAsString(requestObj);

        LOG.debug("Refresh token request: " + body);

        final HttpEntity<String> request = new HttpEntity<>(body, headers);

        final ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        LOG.debug("Refresh token response: " + response);

        final RefreshTokenResponse refreshTokenResponse = mapper.readValue(response.getBody(), RefreshTokenResponse.class);
        oAuthClientRepo.deleteById(authorizedClient.getPrincipalName());

        final OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                refreshTokenResponse.getAccess_token(),
                Instant.ofEpochSecond(refreshTokenResponse.getExpires_in()),
                Instant.ofEpochSecond(refreshTokenResponse.getExpires_at())
        );

        final OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                refreshTokenResponse.getRefresh_token(),
                Instant.ofEpochSecond(refreshTokenResponse.getExpires_in())
        );

        final OAuth2AuthorizedClient newClient = new OAuth2AuthorizedClient(
                authorizedClient.getClientRegistration(),
                authorizedClient.getPrincipalName(),
                accessToken,
                refreshToken
        );

        final OAuthorizedClient OAuthorizedClient = new OAuthorizedClient();
        OAuthorizedClient.setPrincipalName(authorizedClient.getPrincipalName());
        OAuthorizedClient.setBytes(com.velokofi.events.model.OAuthorizedClient.toBytes(newClient));
        oAuthClientRepo.save(OAuthorizedClient);
    }

    private RefreshTokenRequest getRefreshTokenRequest(final OAuth2AuthorizedClient authorizedClient) {
        final RefreshTokenRequest requestObj = new RefreshTokenRequest();
        requestObj.setClient_id(authorizedClient.getClientRegistration().getClientId());
        requestObj.setClient_secret(authorizedClient.getClientRegistration().getClientSecret());
        requestObj.setGrant_type("refresh_token");
        requestObj.setRefresh_token(authorizedClient.getRefreshToken().getTokenValue());
        return requestObj;
    }

    private URI getUri(final int pageNumber) throws URISyntaxException {
        final StringBuilder builder = new StringBuilder();
        builder.append("https://www.strava.com/api/v3/athlete/activities");
        builder.append("?per_page=").append(Application.ACTIVITIES_PER_PAGE);
        builder.append("&after=").append(Application.START_TIMESTAMP);
        builder.append("&before=").append(Application.END_TIMESTAMP);
        builder.append("&page=").append(pageNumber);

        return new URI(builder.toString());
    }

    private String getTokenValue(final String clientId) {
        final OAuthorizedClient client = oAuthClientRepo.findById(clientId).get();
        final OAuth2AuthorizedClient entry = OAuthorizedClient.fromBytes(client.getBytes());
        final String tokenValue = entry.getAccessToken().getTokenValue();
        return tokenValue;
    }

}