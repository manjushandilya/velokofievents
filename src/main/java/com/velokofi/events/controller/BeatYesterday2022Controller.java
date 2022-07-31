package com.velokofi.events.controller;

import com.velokofi.events.model.*;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.IntStream.rangeClosed;

@RestController
public class BeatYesterday2022Controller {

    private static final Logger LOG = LoggerFactory.getLogger(BeatYesterday2022Controller.class);

    @Autowired
    private AthleteActivityRepository athleteActivityRepository;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @GetMapping("/beatYesterday")
    public ModelAndView execute() throws Exception {
        final Month startMonth = Month.MAY, endMonth = Month.JULY;

        final List<Month> months = rangeClosed(startMonth.getValue(), endMonth.getValue())
                .mapToObj(m -> Month.of(m)).collect(toUnmodifiableList());

        final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
        final Map<String, String> clientIdVsAthleteNameMap = clients.stream().collect(
                Collectors.toMap(OAuthorizedClient::getPrincipalName, BeatYesterday2022Controller::apply)
        );

        final List<AthleteActivity> activities = athleteActivityRepository.findAll();
        final Map<String, List<AthleteActivity>> athleteActivities = activities.stream().collect(
                Collectors.groupingBy(a -> String.valueOf(a.getAthlete().getId()))
        );

        final List<BeatYesterdayPhasesSummary> beatYesterdayPhasesSummaries = new ArrayList<>();
        for (final String clientId : clientIdVsAthleteNameMap.keySet()) {
            final String athleteName = clientIdVsAthleteNameMap.get(clientId);
            LOG.info("ClientId: " + clientId + ", AthleteName: " + athleteName);

            final List<AthleteActivity> athleteActivitiesList;
            if (athleteActivities.get(clientId) != null) {
                athleteActivitiesList = athleteActivities.get(clientId);
            } else {
                athleteActivitiesList = Collections.EMPTY_LIST;
            }
            LOG.info("Activity Count: " + athleteActivitiesList.size());

            final BeatYesterdayPhasesSummary beatYesterdayPhasesSummary =
                    new BeatYesterdayPhasesSummary(
                            clientId, athleteName,
                            athleteActivitiesList, 2022, startMonth, endMonth
                    );
            beatYesterdayPhasesSummaries.add(beatYesterdayPhasesSummary);
        }

        /*
        final List<AthleteActivity> athleteActivities = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            athleteActivities.add(new AthleteActivity()
                    .setStart_date_local("2021-06-" + (i > 9 ? "" : "0") + i + "T06:00:00Z").setDistance(i * 1000));
        }

        final BeatYesterdayPhasesSummary summary = new BeatYesterdayPhasesSummary(
                37177283L, "Manjunath Sathyanarayana",
                athleteActivities, 2021, startMonth, endMonth
        );
        beatYesterdayPhasesSummaries.add(summary);
        */

        final ModelAndView mav = new ModelAndView("beatYesterday");
        mav.addObject("months", months);
        mav.addObject("beatYesterdayPhasesSummaries", beatYesterdayPhasesSummaries);
        return mav;
    }

    private ActivityStatisticsSummary fetch(final ActivityStatistics activityStatistics) {
        return new ActivityStatisticsSummary(activityStatistics);
    }

    private static String apply(final OAuthorizedClient client) {
        LOG.error("client.getAthleteName() is null for: " + client.getPrincipalName());
        return client.getAthleteName() != null ? client.getAthleteName() : client.getPrincipalName();
    }

}
