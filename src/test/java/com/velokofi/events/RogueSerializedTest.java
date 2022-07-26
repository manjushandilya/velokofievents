package com.velokofi.events;

import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.hungryvelos.RogueActivities;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RogueSerializedTest {

    @Test
    public void test() throws Exception {
        final List<AthleteActivity> activities = new ArrayList<>();
        final Field[] declaredFields = RogueActivities.class.getDeclaredFields();
        for (final Field field : declaredFields) {
            final AthleteActivity rogueActivity = VeloKofiEventsApplication.MAPPER.readValue(field.get(null).toString(), AthleteActivity.class);
            activities.add(rogueActivity);
        }

        System.out.println(activities);

    }

}
