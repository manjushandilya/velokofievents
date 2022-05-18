package com.velokofi.events.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ActivityStats {

    private String biggest_ride_distance;

    private String biggest_climb_elevation_gain;

    private String ytd_ride_totals;

    private String ytd_run_totals;

    private String ytd_swim_totals;

    private String all_ride_totals;

    private String all_run_totals;

    private String all_swim_totals;

}
