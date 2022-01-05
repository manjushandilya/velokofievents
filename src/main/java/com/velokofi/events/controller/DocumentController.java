package com.velokofi.events.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.OAuthorizedClient;
import com.velokofi.events.model.hungryvelos.Team;
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

    @GetMapping("/documents/activities")
    public String getActivities() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final List<AthleteActivity> activities = athleteActivityRepo.findAll();
        return mapper.writeValueAsString(activities);
    }

    @GetMapping("/documents/activities/{athleteId}")
    public String getActivities(@PathVariable("athleteId") Long athleteId) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final List<AthleteActivity> athleteActivities = athleteActivityRepo.findAll();
        final List<AthleteActivity> activities = athleteActivities.stream().filter(a -> a.getAthlete().getId() == athleteId).collect(toList());
        return mapper.writeValueAsString(activities);
    }

    @GetMapping("/documents/clients")
    public String getClients() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final List<OAuthorizedClient> clients = authorizedClientRepo.findAll();
        return mapper.writeValueAsString(clients);
    }

    @GetMapping("/documents")
    public String operation(@RequestParam(name = "action") String action) throws Exception {
        switch (action) {
            case "clearActivities":
                athleteActivityRepo.deleteAll();
                break;
            case "clearClients":
                authorizedClientRepo.deleteAll();
                break;
            case "clearAll":
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
