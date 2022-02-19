package com.velokofi.events.cron;

import com.velokofi.events.persistence.AthleteActivityRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Setter
@Getter
public final class ActivityPurger {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityUpdater.class);

    @Autowired
    private AthleteActivityRepository athleteActivityRepo;

    //@Scheduled(cron = "0 0 0 * * *")
    public void run() throws Exception {
        LOG.info("Running ActivityPurger scheduled task at: " + LocalDateTime.now());

        athleteActivityRepo.deleteAll();
    }

}
