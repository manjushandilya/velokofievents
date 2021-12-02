package com.velokofi.events.controller;

import com.velokofi.events.persistence.AthleteActivityRepository;
import com.velokofi.events.persistence.OAuthorizedClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class SevenCoffeeHundredMilesController {

    private final RestTemplate restTemplate;

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    @Autowired
    private OAuthorizedClientRepository authorizedClientRepo;

    public SevenCoffeeHundredMilesController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/7c100m")
    public ModelAndView build(@RegisteredOAuth2AuthorizedClient final OAuth2AuthorizedClient client,
                              @RequestParam(required = false, defaultValue = "false") boolean debug) throws Exception {
        final ModelAndView mav = new ModelAndView("7c100m");
        mav.addObject("principalName", client.getPrincipalName());
        return mav;
    }

}
