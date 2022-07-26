package com.velokofi.events.controller;

import com.velokofi.events.model.ActivityStatistics;
import com.velokofi.events.model.ActivityStatisticsSummary;
import com.velokofi.events.persistence.ActivityStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
public class PledgeController {

    private static final Logger LOG = LoggerFactory.getLogger(PledgeController.class);

    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;

    @GetMapping("/pledge")
    public ModelAndView execute() throws Exception {
        final List<ActivityStatistics> activityStatistics = activityStatisticsRepo.findAll();
        final List<ActivityStatisticsSummary> activityStatisticsSummaries = activityStatistics.stream()
                .filter(as -> as.getBiggest_ride_distance() > 0)
                .map(this::fetch).collect(toList());

        Collections.sort(activityStatisticsSummaries, (ass1, ass2) -> ass2.getYtdDistance().compareTo(ass1.getYtdDistance()));

        final ModelAndView mav = new ModelAndView("pledge");
        mav.addObject("activityStatisticsSummaries", activityStatisticsSummaries);
        return mav;
    }

    private ActivityStatisticsSummary fetch(final ActivityStatistics activityStatistics) {
        return new ActivityStatisticsSummary(activityStatistics);
    }

}
