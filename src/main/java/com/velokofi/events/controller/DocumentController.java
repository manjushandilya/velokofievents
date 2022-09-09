package com.velokofi.events.controller;

import com.velokofi.events.VeloKofiEventsApplication;
import com.velokofi.events.cron.ActivityUpdater;
import com.velokofi.events.cron.StatisticsUpdater;
import com.velokofi.events.model.ActivityStatistics;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.persistence.ActivityStatisticsRepository;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import com.velokofi.events.persistence.HungryVelos2022TeamsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
public class DocumentController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private HungryVelos2022TeamsRepository hungryVelos2022TeamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;

    @Autowired
    private StatisticsUpdater statisticsUpdater;

    @Autowired
    private ActivityUpdater activityUpdater;

    @GetMapping("/documents/statistics")
    public String getStatistics(@RequestParam(name = "action", required = false) final String action) throws Exception {
        if (action != null) {
            switch (action) {
                case "refresh":
                    statisticsUpdater.run();
                    break;
            }
        }
        final List<ActivityStatistics> statistics = activityStatisticsRepo.findAll();
        return VeloKofiEventsApplication.MAPPER.writeValueAsString(statistics);
    }

    @GetMapping("/documents/activities")
    public String getAthleteActivities(
            @RequestParam(name = "action", required = false) final String action,
            @RequestParam(name = "after", required = false) final String after,
            @RequestParam(name = "before", required = false) final String before
    ) throws Exception {
        if (action != null) {
            switch (action) {
                case "refresh":
                    if (after != null && before != null) {
                        activityUpdater.fetch(after, before);
                    } else {
                        activityUpdater.run();
                    }
                    break;
            }
        }
        final List<AthleteActivity> activities = athleteActivityRepo.findAll();
        return VeloKofiEventsApplication.MAPPER.writeValueAsString(activities);
    }

    @GetMapping("/documents/activities/{athleteId}")
    public String getAthleteActivities(@PathVariable("athleteId") final Long athleteId) throws Exception {
        final List<AthleteActivity> allActivities = athleteActivityRepo.findAll();
        final List<AthleteActivity> athleteActivities = allActivities.stream().filter(a -> a.getAthlete().getId() == athleteId).collect(toList());
        return VeloKofiEventsApplication.MAPPER.writeValueAsString(athleteActivities);
    }

    @GetMapping("/documents/clients")
    public String getClients() throws Exception {
        final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
        return VeloKofiEventsApplication.MAPPER.writeValueAsString(clients);
    }

    @GetMapping("/documents/clients/{clientId}")
    public String getClient(@PathVariable("clientId") final Long clientId, @RequestParam(name = "action") final String action) throws Exception {
        final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
        final List<OAuthorizedClient> filteredClients = clients.stream().filter(c -> c.getPrincipalName().equals(clientId)).collect(toList());
        final String clientAsString = VeloKofiEventsApplication.MAPPER.writeValueAsString(filteredClients);

        switch (action) {
            case "clear":
                LOG.info("Clearing clearClient with id: " + clientId);
                authorizedClientRepo.deleteById(String.valueOf(clientId));
                break;
            default:
                break;
        }

        return clientAsString;
    }

    @GetMapping("/documents")
    public String operation(@RequestParam(name = "action") final String action) throws Exception {
        switch (action) {
            case "updateAthleteNames":
                LOG.info("Updating athleteNames...");
                final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
                for (final OAuthorizedClient client: clients) {
                    if (client.getAthleteName() == null || client.getAthleteName().isBlank()) {
                        final String principalName = client.getPrincipalName();
                        final String athleteName = STRAVA_ID_VS_NAME_MAP.get(principalName);

                        LOG.error("AthleteName not found for clientId: " + principalName);
                        if (athleteName != null) {
                             LOG.error("Setting athleteName as: " + athleteName);
                            client.setAthleteName(athleteName);
                            authorizedClientRepo.save(client);
                        } else {
                            LOG.error("Ghost athlete with id: " + principalName);
                        }
                    }
                }
                break;
            case "clearStatistics":
                LOG.info("Clearing activityStatisticsRepo...");
                activityStatisticsRepo.deleteAll();
                break;
            case "clearActivities":
                LOG.info("Clearing clearActivities...");
                athleteActivityRepo.deleteAll();
                break;
            case "clearClients":
                LOG.info("Clearing clearClients...");
                authorizedClientRepo.deleteAll();
                break;
            case "clearAll":
                LOG.info("Clearing activityStatisticsRepo...");
                activityStatisticsRepo.deleteAll();
                LOG.info("Clearing clearActivities...");
                athleteActivityRepo.deleteAll();
                LOG.info("Clearing clearClients...");
                authorizedClientRepo.deleteAll();
                break;
            case "cleanup":
                LOG.info("Cleaning up...");
                final List<Team> teams = hungryVelos2022TeamsRepository.listTeams();
                final List<Long> configuredClientIds = teams.stream().flatMap(t -> t.getMembers().stream()).map(tm -> tm.getId()).collect(toList());

                LOG.debug("configuredClientIds: " + configuredClientIds);

                final List<Long> persistedClientIds = authorizedClientRepo.findAll().stream().map(ac -> Long.parseLong(ac.getPrincipalName())).collect(toList());

                LOG.debug("persistedClientIds: " + persistedClientIds);

                persistedClientIds.stream().filter(c -> !configuredClientIds.contains(c)).forEach(
                        id -> authorizedClientRepo.deleteById(String.valueOf(id))
                );
        }
        return "";
    }

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
        STRAVA_ID_VS_NAME_MAP.put("93507523", "Amarnath Vali");
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
        STRAVA_ID_VS_NAME_MAP.put("38148750", "Chandu :)");
        STRAVA_ID_VS_NAME_MAP.put("33762572", "Arun Bastin");
        STRAVA_ID_VS_NAME_MAP.put("28550167", "Sudarshan Gopinath");
        STRAVA_ID_VS_NAME_MAP.put("41081264", "Subhra Kanti Das");
        STRAVA_ID_VS_NAME_MAP.put("62775438", "Sandhya Santhosh");
        STRAVA_ID_VS_NAME_MAP.put("42776148", "Manas Chand Ryali");
        STRAVA_ID_VS_NAME_MAP.put("42777287", "Manavchand Ryali");
        STRAVA_ID_VS_NAME_MAP.put("68380635", "Latha Rani");
        STRAVA_ID_VS_NAME_MAP.put("36760093", "Manasa Bharadwaj");
        STRAVA_ID_VS_NAME_MAP.put("78551177", "samrudh kishor");
        STRAVA_ID_VS_NAME_MAP.put("88072624", "Niharikaa N");
        STRAVA_ID_VS_NAME_MAP.put("88076285", "Tanu Pradeep Gorur");
        STRAVA_ID_VS_NAME_MAP.put("88054379", "Aditya V K");
        STRAVA_ID_VS_NAME_MAP.put("87964523", "Atharva Subramanya Kashyap");
        STRAVA_ID_VS_NAME_MAP.put("81228572", "Jayashree Nandakumar");
        STRAVA_ID_VS_NAME_MAP.put("69378718", "Varun Kamath");
        STRAVA_ID_VS_NAME_MAP.put("44516714", "Amit Krishna");
        STRAVA_ID_VS_NAME_MAP.put("95295744", "Roopa Rangaswamy");
        STRAVA_ID_VS_NAME_MAP.put("85359369", "Madhusudan Bangalore Prabhakar");
    }

}
