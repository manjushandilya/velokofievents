package com.velokofi.events.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AthleteSummary {

    private long id;

    private String name;

    private double distance;

    private double elevation;

    private double avgSpeed;

    private boolean captain;

    private String gender;

    private long rides;
}
