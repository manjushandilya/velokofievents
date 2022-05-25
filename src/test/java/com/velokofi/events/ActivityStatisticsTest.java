package com.velokofi.events;

import com.velokofi.events.model.ActivityStatistics;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ActivityStatisticsTest {

    @Test
    public void testDeserialization() throws Exception {
        final Path path = Paths.get("src", "test", "resources", "activities", "activityStats.json");
        final byte[] bytes = Files.readAllBytes(path);
        final ActivityStatistics activityStatistics = Application.MAPPER.readValue(bytes, ActivityStatistics.class);
        System.out.println(activityStatistics);
    }

}
