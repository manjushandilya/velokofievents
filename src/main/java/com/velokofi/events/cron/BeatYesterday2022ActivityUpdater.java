package com.velokofi.events.cron;

import com.velokofi.events.VeloKofiEventsApplication;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.persistence.AthleteActivityRepository;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
@Setter
@Getter
public class BeatYesterday2022ActivityUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(BeatYesterday2022ActivityUpdater.class);

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    //@Scheduled(fixedDelay = 1 * 60 * 1000 * 60, initialDelay = 60 * 1000 * 60)
    @Scheduled(cron = "0 0 0 * * *")
    public void run() throws Exception {
        LOG.info("Running BeatYesterday2022ActivityUpdater scheduled task at: " + LocalDateTime.now());

        final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
        final List<String> clientIds = clients.stream().map(c -> c.getPrincipalName()).collect(toList());

        for (final String clientId : clientIds) {
            LOG.info("Fetching activities for clientId: " + clientId);
            try {
                final AthleteActivity[] activities = getActivities(clientId);
                if (activities.length > 0) {
                    LOG.info("Saving " + activities.length + " activities for clientId: " + clientId);
                    Stream.of(activities)
                            .filter(a -> VeloKofiEventsApplication.SUPPORTED_RIDE_TYPES.contains(a.getType()))
                            .forEach(activity -> athleteActivityRepo.save(activity));
                }
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
                    LOG.error("Exception while fetching activities for clientId " + clientId + ": " + e.getMessage());
                }
            }
        }
    }

    private AthleteActivity[] getActivities(final String clientId) throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + RefreshTokenHelper.getTokenValue(authorizedClientRepo, clientId));

        final HttpEntity<String> request = new HttpEntity<>(headers);
        final ResponseEntity<String> response = restTemplate.exchange(getUri(1),
                HttpMethod.GET, request, String.class);
        return VeloKofiEventsApplication.MAPPER.readValue(response.getBody(), AthleteActivity[].class);
    }

    private URI getUri(final int pageNumber) throws URISyntaxException {
        final StringBuilder builder = new StringBuilder();
        builder.append("https://www.strava.com/api/v3/athlete/activities");
        builder.append("?per_page=").append(VeloKofiEventsApplication.ACTIVITIES_PER_PAGE);
        //builder.append("&after=").append(VeloKofiEventsApplication.BY_2021_START_TIMESTAMP);
        final OffsetDateTime after = OffsetDateTime.of(2022, 5, 1, 0, 0, 0, 0, ZoneOffset.of("+05:30"));
        builder.append("&after=").append(after.toEpochSecond());
        //builder.append("&before=").append(VeloKofiEventsApplication.BY_2021_END_TIMESTAMP);
        final OffsetDateTime before = OffsetDateTime.of(2022, 7, 31, 23, 59, 59, 999999999, ZoneOffset.of("+05:30"));
        builder.append("&before=").append(before.toEpochSecond());
        builder.append("&page=").append(pageNumber);

        return new URI(builder.toString());
    }

}