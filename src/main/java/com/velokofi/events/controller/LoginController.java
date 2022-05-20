package com.velokofi.events.controller;

import com.velokofi.events.Application;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.OAuthorizedClient;
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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RestController
@Getter
@Setter
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    private static final String COOKIE_ID = "velokofieventscookie";

    private final RestTemplate restTemplate;

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    public LoginController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/login")
    public RedirectView execute(@RegisteredOAuth2AuthorizedClient final OAuth2AuthorizedClient client) throws Exception {
        final List<Team> teams = teamsRepository.listTeams();
        final List<TeamMember> teamMembers = teams.stream().flatMap(t -> t.getMembers().stream()).collect(toList());
        final Optional<TeamMember> teamMemberLogin = teamMembers.stream()
                .filter(tm -> String.valueOf(tm.getId()).equals(client.getPrincipalName())).findFirst();

        LOG.debug("Team member logged in? " + teamMemberLogin.isPresent() + ", strava id: " + client.getPrincipalName());

        final String tokenValue = client.getAccessToken().getTokenValue();
        if (teamMemberLogin.isPresent()) {
            final OAuthorizedClient OAuthorizedClient = new OAuthorizedClient();
            OAuthorizedClient.setPrincipalName(client.getPrincipalName());
            OAuthorizedClient.setBytes(com.velokofi.events.model.OAuthorizedClient.toBytes(client));
            authorizedClientRepo.save(OAuthorizedClient);

            for (int pageNumber = 1; ; pageNumber++) {
                final StringBuilder url = new StringBuilder();
                url.append("https://www.strava.com/api/v3/athlete/activities");
                url.append("?per_page=").append(Application.ACTIVITIES_PER_PAGE);
                url.append("&after=").append(Application.START_TIMESTAMP);
                url.append("&before=").append(Application.END_TIMESTAMP);
                url.append("&page=").append(pageNumber);

                LOG.debug("Hitting url: " + url);

                final String response = getResponse(tokenValue, url.toString());

                final AthleteActivity[] activitiesArray = Application.MAPPER.readValue(response, AthleteActivity[].class);
                Stream.of(activitiesArray)
                        .filter(a -> Application.SUPPORTED_RIDE_TYPES.contains(a.getType()))
                        .forEach(activity -> athleteActivityRepo.save(activity));

                if (activitiesArray.length < 200) {
                    break;
                }
            }
        }
        final RedirectView redirectView = new RedirectView("/setCookie");
        redirectView.addStaticAttribute(COOKIE_ID, client.getPrincipalName());

        return redirectView;
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
