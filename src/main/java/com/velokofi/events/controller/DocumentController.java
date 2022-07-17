package com.velokofi.events.controller;

import com.velokofi.events.VeloKofiEventsApplication;
import com.velokofi.events.cron.StatisticsUpdater;
import com.velokofi.events.model.ActivityStatistics;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.persistence.ActivityStatisticsRepository;
import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import com.velokofi.events.persistence.TeamsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
public class DocumentController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;

    @Autowired
    private StatisticsUpdater statisticsUpdater;

    @GetMapping("/documents/statistics")
    public String getStatistics(@RequestParam(name = "action") final String action) throws Exception {
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
    public String getActivities() throws Exception {
        final List<AthleteActivity> activities = athleteActivityRepo.findAll();
        return VeloKofiEventsApplication.MAPPER.writeValueAsString(activities);
    }

    @GetMapping("/documents/activities/{athleteId}")
    public String getActivities(@PathVariable("athleteId") final Long athleteId) throws Exception {
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
            case "clearStatistics":
                activityStatisticsRepo.deleteAll();
                break;
            case "clearActivities":
                athleteActivityRepo.deleteAll();
                break;
            case "clearClients":
                authorizedClientRepo.deleteAll();
                break;
            case "clearAll":
                activityStatisticsRepo.deleteAll();
                athleteActivityRepo.deleteAll();
                authorizedClientRepo.deleteAll();
                break;
            case "cleanup":
                final List<Team> teams = teamsRepository.listTeams();
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

}
