package com.velokofi.events.controller;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import com.velokofi.events.persistence.TeamsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @GetMapping("/reports/activities")
    public String getActivities() throws Exception {
        final List<AthleteActivity> activities = athleteActivityRepo.findAll();
        return toCSV(activities);
    }

    @GetMapping("/reports/activities/{athleteId}")
    public String getActivities(@PathVariable("athleteId") Long athleteId) throws Exception {
        final List<AthleteActivity> athleteActivities = athleteActivityRepo.findAll();
        final List<AthleteActivity> activities = athleteActivities.stream().filter(a -> a.getAthlete().getId() == athleteId).collect(toList());
        return toCSV(activities);
    }

    private String toCSV(final List<AthleteActivity> activities) throws Exception {
        final Writer writer = new StringWriter();
        final StatefulBeanToCsv statefulBeanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        statefulBeanToCsv.write(activities);
        writer.close();
        return writer.toString();
    }

}
