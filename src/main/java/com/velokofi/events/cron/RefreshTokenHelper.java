package com.velokofi.events.cron;

import com.velokofi.events.VeloKofiEventsApplication;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.model.RefreshTokenRequest;
import com.velokofi.events.model.RefreshTokenResponse;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public class RefreshTokenHelper {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshTokenHelper.class);

    public static void refreshToken(final OAuthorizedClientRepository authorizedClientRepo, final String clientId) throws Exception {
        final OAuthorizedClient client = authorizedClientRepo.findById(clientId).get();
        final OAuth2AuthorizedClient authorizedClient = OAuthorizedClient.fromBytes(client.getBytes());

        final StringBuilder builder = new StringBuilder();
        builder.append("https://www.strava.com/api/v3/oauth/token");

        final URI uri = new URI(builder.toString());

        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final RefreshTokenRequest requestObj = getRefreshTokenRequest(authorizedClientRepo, authorizedClient);
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

    public static RefreshTokenRequest getRefreshTokenRequest(final OAuthorizedClientRepository authorizedClientRepo, final OAuth2AuthorizedClient authorizedClient) {
        final RefreshTokenRequest requestObj = new RefreshTokenRequest();
        requestObj.setClient_id(authorizedClient.getClientRegistration().getClientId());
        requestObj.setClient_secret(authorizedClient.getClientRegistration().getClientSecret());
        requestObj.setGrant_type("refresh_token");
        requestObj.setRefresh_token(authorizedClient.getRefreshToken().getTokenValue());
        return requestObj;
    }

    public static String getTokenValue(final OAuthorizedClientRepository authorizedClientRepo, final String clientId) {
        final OAuthorizedClient client = authorizedClientRepo.findById(clientId).get();
        final OAuth2AuthorizedClient entry = OAuthorizedClient.fromBytes(client.getBytes());
        final String tokenValue = entry.getAccessToken().getTokenValue();
        return tokenValue;
    }

}
