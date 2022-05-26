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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
public class PledgeController {

    private static final Logger LOG = LoggerFactory.getLogger(PledgeController.class);

    public static final Map<String, String> STRAVA_ID_VS_NAME_MAP;

    static {
        STRAVA_ID_VS_NAME_MAP = new HashMap<>();

        STRAVA_ID_VS_NAME_MAP.put("43378948", "Shruthi Subbanna");
        STRAVA_ID_VS_NAME_MAP.put("35940578", "Sai Srinivas");
        STRAVA_ID_VS_NAME_MAP.put("37177283", "Manjunath Sathyanarayana");
        STRAVA_ID_VS_NAME_MAP.put("28234693", "Goutham YV");
        STRAVA_ID_VS_NAME_MAP.put("74963850", "Nandini Kishor");
        STRAVA_ID_VS_NAME_MAP.put("69245380", "Siddharth Horee");
        STRAVA_ID_VS_NAME_MAP.put("14463022", "Sagar MC");
        STRAVA_ID_VS_NAME_MAP.put("3225831", "Prashanth S");
        STRAVA_ID_VS_NAME_MAP.put("73010512", "Chandrashekar K");
        STRAVA_ID_VS_NAME_MAP.put("15589851", "Sun'J Sharma");
        STRAVA_ID_VS_NAME_MAP.put("3377091", "Sunil Gopala Chari");
        STRAVA_ID_VS_NAME_MAP.put("9125629", "Deepu Fixie Beardo");
        STRAVA_ID_VS_NAME_MAP.put("37178147", "Sandeep B N");
        STRAVA_ID_VS_NAME_MAP.put("58389372", "Rupesh 🏃🚴🚵🏊");
        STRAVA_ID_VS_NAME_MAP.put("6848467", "Shreenivasa K s");
        STRAVA_ID_VS_NAME_MAP.put("62516649", "Anil Kumar");
        STRAVA_ID_VS_NAME_MAP.put("20378999", "Rav Indra");
        STRAVA_ID_VS_NAME_MAP.put("62411206", "Arun Bhaskar");
        STRAVA_ID_VS_NAME_MAP.put("75083256", "Manu Gopinath");
        STRAVA_ID_VS_NAME_MAP.put("38684955", "Premchand Ryali");
        STRAVA_ID_VS_NAME_MAP.put("83728576", "Shyam Sundar K");
        STRAVA_ID_VS_NAME_MAP.put("12244586", "Kirti Chalam");
        STRAVA_ID_VS_NAME_MAP.put("64757576", "Bharadwaja S R");
        STRAVA_ID_VS_NAME_MAP.put("78896766", "Rahul Padmanabha");
        STRAVA_ID_VS_NAME_MAP.put("69757350", "Ravi Sunderrajan");
        STRAVA_ID_VS_NAME_MAP.put("51400681", "Roopa Rupesh");
        STRAVA_ID_VS_NAME_MAP.put("17249418", "Chandan Gaddehosur");
        STRAVA_ID_VS_NAME_MAP.put("75500724", "Revathi M P");
        STRAVA_ID_VS_NAME_MAP.put("65820756", "Nanda Kishor G D");
        STRAVA_ID_VS_NAME_MAP.put("65392239", "Vipin Devis");
        STRAVA_ID_VS_NAME_MAP.put("64907699", "Amarnath Vali");
        STRAVA_ID_VS_NAME_MAP.put("63014939", "Srinidhi Bharadwaj");
        STRAVA_ID_VS_NAME_MAP.put("61999014", "Vidyaprasanna Kambalur");
        STRAVA_ID_VS_NAME_MAP.put("61335755", "Sampige Santhosh");
        STRAVA_ID_VS_NAME_MAP.put("68355712", "Sandeep Vishwanath");
        STRAVA_ID_VS_NAME_MAP.put("33590187", "Faraz Umar");
        STRAVA_ID_VS_NAME_MAP.put("25409226", "Lakshmi Narasu");
        STRAVA_ID_VS_NAME_MAP.put("16222927", "Mythri Sunil");
        STRAVA_ID_VS_NAME_MAP.put("12920635", "Sukumar Sundaram");
        STRAVA_ID_VS_NAME_MAP.put("19218746", "Raghu B V");
        STRAVA_ID_VS_NAME_MAP.put("85387376", "Nischal Kumar");
        STRAVA_ID_VS_NAME_MAP.put("74710582", "Zainab Shoaib");
        STRAVA_ID_VS_NAME_MAP.put("74590313", "Venkat");
        STRAVA_ID_VS_NAME_MAP.put("73629852", "Bharathi Malavalli");
        STRAVA_ID_VS_NAME_MAP.put("73492491", "Savitha Naik");
        STRAVA_ID_VS_NAME_MAP.put("71154685", "Ashwini Nischal (Ashu)");
        STRAVA_ID_VS_NAME_MAP.put("70101578", "Chidanand Kawri Lokesh");
        STRAVA_ID_VS_NAME_MAP.put("67288162", "Harsha Gandhi");
        STRAVA_ID_VS_NAME_MAP.put("62471947", "Girish Jain Jain");
        STRAVA_ID_VS_NAME_MAP.put("38148750", "Chandrakanth");
        STRAVA_ID_VS_NAME_MAP.put("33762572", "Arun Bastin");
        STRAVA_ID_VS_NAME_MAP.put("28550167", "Sudarshan Gopinath");
        STRAVA_ID_VS_NAME_MAP.put("41081264", "Subhra Kanti Das");
        STRAVA_ID_VS_NAME_MAP.put("62775438", "Sandhya Santhosh");
        STRAVA_ID_VS_NAME_MAP.put("42776148", "Manas Chand Ryali");
        STRAVA_ID_VS_NAME_MAP.put("42777287", "Manavchand Ryali");
    }

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;
    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;

    @GetMapping("/pledge")
    public ModelAndView execute() throws Exception {
        final List<ActivityStatistics> activityStatistics = activityStatisticsRepo.findAll();
        final List<ActivityStatisticsSummary> activityStatisticsSummaries = activityStatistics.stream().map(this::fetch).collect(toList());

        final ModelAndView mav = new ModelAndView("pledge");
        mav.addObject("activityStatisticsSummaries", activityStatisticsSummaries);
        return mav;
    }

    private ActivityStatisticsSummary fetch(final ActivityStatistics activityStatistics) {
        if (activityStatistics.getAthleteName() == null) {
            activityStatistics.setAthleteName(STRAVA_ID_VS_NAME_MAP.get(activityStatistics.getAthleteId()));
        }
        return new ActivityStatisticsSummary(activityStatistics);
    }

}
