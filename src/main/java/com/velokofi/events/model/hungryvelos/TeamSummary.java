package com.velokofi.events.model.hungryvelos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TeamSummary {

    private long id;

    private String name;

    private double distance;

    private double avgDistance;

    private double elevation;

    private double avgElevation;

    private double avgSpeed;

    private int rides;

    private double avgRides;

    private int memberCount;

}
