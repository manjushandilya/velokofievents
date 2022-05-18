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

    private double biggest_ride_distance;

    private double biggest_climb_elevation_gain;

    private ActivityTotal ytd_ride_totals;

    private ActivityTotal ytd_run_totals;

    private ActivityTotal ytd_swim_totals;

    private ActivityTotal all_ride_totals;

    private ActivityTotal all_run_totals;

    private ActivityTotal all_swim_totals;

}
