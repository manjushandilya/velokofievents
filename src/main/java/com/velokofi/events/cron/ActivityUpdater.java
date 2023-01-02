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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.velokofi.events.VeloKofiEventsApplication.MAPPER;
import static com.velokofi.events.VeloKofiEventsApplication.MAX_ACTIVITIES_PER_PAGE;
import static java.util.stream.Collectors.toList;

@Component
@Setter
@Getter
public class ActivityUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityUpdater.class);

    private static final String HV3_START_TIMESTAMP; // 00:00:00 on 01 Jan 2023

    static {
        final OffsetDateTime dateTime = OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, VeloKofiEventsApplication.IST);
        HV3_START_TIMESTAMP = String.valueOf(dateTime.toEpochSecond());
    }

    private static final String HV3_END_TIMESTAMP; // 23:59:59 on 31 Jan 2023

    static {
        final OffsetDateTime dateTime = OffsetDateTime.of(2023, 1, 31, 23, 59, 59, 0, VeloKofiEventsApplication.IST);
        HV3_END_TIMESTAMP = String.valueOf(dateTime.toEpochSecond());
    }

    private static final List<String> SUPPORTED_RIDE_TYPES;

    static {
        SUPPORTED_RIDE_TYPES = new ArrayList<>();
        SUPPORTED_RIDE_TYPES.add("Ride");
        SUPPORTED_RIDE_TYPES.add("VirtualRide");
    }

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @Scheduled(cron = "0 0 0 * * *")
    public void run() throws Exception {
        fetch(HV3_START_TIMESTAMP, HV3_END_TIMESTAMP);
    }

    public void fetch(final String after, final String before) throws Exception {
        LOG.info("Running ActivityUpdater scheduled task at: " + LocalDateTime.now());

        final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
        final List<String> clientIds = clients.stream().map(c -> c.getPrincipalName()).collect(toList());

        for (final String clientId : clientIds) {
            LOG.info("Fetching activities for clientId: " + clientId);
            try {
                final List<AthleteActivity> activities = getActivities(clientId, after, before);
                if (!activities.isEmpty()) {
                    LOG.info("Saving " + activities.size() + " activities for clientId: " + clientId);
                    final List<AthleteActivity> filteredActivities = activities.stream()
                            .filter(a -> SUPPORTED_RIDE_TYPES.contains(a.getType()))
                            .collect(toList());
                    athleteActivityRepo.saveAll(filteredActivities);
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

    private List<AthleteActivity> getActivities(final String clientId, final String after, final String before) throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + RefreshTokenHelper.getTokenValue(authorizedClientRepo, clientId));

        final HttpEntity<String> request = new HttpEntity<>(headers);

        final List<AthleteActivity> activities = new ArrayList<>();
        int pageNumber = 1;
        while (true) {
            final ResponseEntity<String> response = restTemplate.exchange(
                    getUri(pageNumber, after, before), HttpMethod.GET, request, String.class
            );
            final AthleteActivity[] athleteActivities = MAPPER.readValue(
                    response.getBody(), AthleteActivity[].class
            );
            activities.addAll(Arrays.asList(athleteActivities));
            pageNumber++;
            if (athleteActivities.length < MAX_ACTIVITIES_PER_PAGE) {
                break;
            }
        }
        return activities;
    }

    private URI getUri(final int pageNumber, final String after, final String before) throws URISyntaxException {
        final StringBuilder builder = new StringBuilder();
        builder.append("https://www.strava.com/api/v3/athlete/activities");
        builder.append("?per_page=").append(MAX_ACTIVITIES_PER_PAGE);
        builder.append("&after=").append(after);
        builder.append("&before=").append(before);
        builder.append("&page=").append(pageNumber);

        return new URI(builder.toString());
    }

}
