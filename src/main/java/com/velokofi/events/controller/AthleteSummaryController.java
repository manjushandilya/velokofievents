package com.velokofi.events.controller;

import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.TeamsRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@Getter
@Setter
public class AthleteSummaryController {

    private static final Logger LOG = LoggerFactory.getLogger(HungryVelosController.class);

    private final RestTemplate restTemplate;

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    public AthleteSummaryController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/athleteSummary/{athleteId}")
    public ModelAndView execute(@PathVariable("athleteId") Long athleteId) throws Exception {
        final List<AthleteActivity> allActivities = athleteActivityRepo.findAll();
        final List<AthleteActivity> athleteActivities = allActivities.stream().filter(a -> a.getAthlete().getId() == athleteId).collect(toList());

        final ModelAndView mav = new ModelAndView("index");
        mav.addObject("athleteActivities", athleteActivities);
        return mav;
    }

}
