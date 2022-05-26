package com.velokofi.events.controller;

import com.velokofi.events.model.ActivityStatistics;
import com.velokofi.events.model.ActivityStatisticsSummary;
import com.velokofi.events.persistence.ActivityStatisticsRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
public class PledgeController {

    private static final Logger LOG = LoggerFactory.getLogger(PledgeController.class);

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;

    @GetMapping("/pledge")
    public ModelAndView execute() throws Exception {
        final List<ActivityStatistics> activityStatistics = activityStatisticsRepo.findAll();
        final List<ActivityStatisticsSummary> activityStatisticsSummaries = activityStatistics.stream().map(ActivityStatisticsSummary::new).collect(toList());

        final ModelAndView mav = new ModelAndView("pledge");
        mav.addObject("activityStatisticsSummaries", activityStatisticsSummaries);
        return mav;
    }

}
