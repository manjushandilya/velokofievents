package com.velokofi.events.controller;

import com.velokofi.events.Application;
import com.velokofi.events.model.*;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamMember;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
public class PledgeController {

    private static final Logger LOG = LoggerFactory.getLogger(PledgeController.class);

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @GetMapping("/pledge")
    public ModelAndView execute() throws Exception {
        final TeamsRepository teamsRepository = new TeamsRepository();
        final List<Team> teams = teamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());

        final List<ActivityStatistics> activityStatistics = getActivityStatistics();
        final List<ActivityStatisticsSummary> activityStatisticsSummaries = activityStatistics.stream().map(
                as -> new ActivityStatisticsSummary(as, teamMembers)
        ).collect(toList());

        final ModelAndView mav = new ModelAndView("pledge");
        mav.addObject("activityStatisticsSummaries", activityStatistics);
        return mav;
    }

    private List<ActivityStatistics> getActivityStatistics() {
        final List<ActivityStatistics> activityStatisticsList = new ArrayList<>();
        final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
        for (final OAuthorizedClient client : clients) {
            for (int attempt = 0; attempt < 3; attempt++) {
                final String clientId = client.getPrincipalName();
                LOG.info("Fetching statistics for clientId: " + clientId);
                try {
                    final ResponseEntity<String> response = getStatisticsResponse(clientId);

                    LOG.debug("Fetched statistics response: " + response);

                    final ActivityStatistics activityStatistics = Application.MAPPER.readValue(response.getBody(), ActivityStatistics.class);
                    activityStatistics.setAthleteId(clientId);
                    activityStatisticsList.add(activityStatistics);

                    LOG.debug("Added activityStatisticsList bean to collection: " + activityStatistics);
                    break;
                } catch (final Exception e) {
                    LOG.error("Exception while fetching: " + e.getMessage());
                    if (e.getMessage().indexOf("401") > -1) {
                        LOG.info("Refreshing auth token for clientId: " + clientId + ", old value: " + getTokenValue(clientId));
                        try {
                            refreshToken(clientId);
                            LOG.debug("Successfully refreshed token for clientId: " + clientId + ", new value: " + getTokenValue(clientId));
                        } catch (final Exception re) {
                            LOG.error("Error while refreshing token for clientId: " + clientId + " " + re.getMessage());
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return activityStatisticsList;
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

        URI uri = new URI(builder.toString());

        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final RefreshTokenRequest requestObj = getRefreshTokenRequest(authorizedClient);
        final String body = Application.MAPPER.writeValueAsString(requestObj);

        LOG.debug("Refresh token request: " + body);

        final HttpEntity<String> request = new HttpEntity<>(body, headers);

        final ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        LOG.debug("Refresh token response: " + response);

        final RefreshTokenResponse refreshTokenResponse = Application.MAPPER.readValue(response.getBody(), RefreshTokenResponse.class);
        authorizedClientRepo.deleteById(authorizedClient.getPrincipalName());

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
        authorizedClientRepo.save(OAuthorizedClient);
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
