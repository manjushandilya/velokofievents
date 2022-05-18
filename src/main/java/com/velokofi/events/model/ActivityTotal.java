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
public class ActivityTotal {

    private int count;

    private float distance;

    private int moving_time;

    private int elapsed_time;

    private float elevation_gain;

    private int achievement_count;

}
