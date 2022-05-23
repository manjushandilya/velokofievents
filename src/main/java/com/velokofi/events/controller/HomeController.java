package com.velokofi.events.controller;

import com.velokofi.events.Application;
import com.velokofi.events.model.AthleteProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class HomeController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @GetMapping("/")
    public ModelAndView execute(@AuthenticationPrincipal final OAuth2User principal) throws Exception {
        final ModelAndView mav = new ModelAndView("index");
        if (principal != null) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication.isAuthenticated()) {

                final OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

                final OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

                final String profileResponse = getResponse(client.getAccessToken().getTokenValue(), "https://www.strava.com/api/v3/athlete");
                final AthleteProfile athleteProfile = Application.MAPPER.readValue(profileResponse, AthleteProfile.class);
                mav.addObject("athleteProfile", athleteProfile);
            }
        }
        return mav;
    }

    private String getResponse(final String tokenValue, final String url) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + tokenValue);
        HttpEntity<String> request = new HttpEntity<>(headers);

        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody();
    }

}
