package com.velokofi.events.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal final OAuth2User principal) {
        return Collections.singletonMap("name", principal.getName());
    }

    @GetMapping("/")
    public ModelAndView execute() throws Exception {
        final ModelAndView mav = new ModelAndView("index");
        return mav;
    }

}
