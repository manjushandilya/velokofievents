package com.velokofi.events.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class PledgeController {

    @GetMapping("/pledge")
    public ModelAndView execute() throws Exception {
        final ModelAndView mav = new ModelAndView("pledge");
        return mav;
    }

}
