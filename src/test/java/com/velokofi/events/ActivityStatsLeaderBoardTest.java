package com.velokofi.events;

import com.velokofi.events.model.ActivityStats;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ActivityStatsLeaderBoardTest {

    @Test
    public void test() throws Exception {
        final Path path = Paths.get("src", "test", "resources", "activities", "activityStats.json");
        final byte[] bytes = Files.readAllBytes(path);
        final ActivityStats[] activityStats = Application.MAPPER.readValue(bytes, ActivityStats[].class);
    }

}
