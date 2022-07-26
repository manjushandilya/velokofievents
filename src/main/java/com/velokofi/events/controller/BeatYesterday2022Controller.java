package com.velokofi.events.controller;

import com.velokofi.events.persistence.ActivityStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BeatYesterday2022Controller {

    private static final Logger LOG = LoggerFactory.getLogger(BeatYesterday2022Controller.class);

    @Autowired
    private ActivityStatisticsRepository activityStatisticsRepo;
}
