package com.velokofi.events.model;

import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvRecurse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Objects;

@Document
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AthleteActivity implements Serializable {

    /*
    "resource_state": 2,
        "athlete": {
            "id": 37177283,
            "resource_state": 1
        },
        "name": "Morning Ride",
        "distance": 254.4,
        "moving_time": 99,
        "elapsed_time": 184,
        "average_speed": 6.679,
        "total_elevation_gain": 0.0,
        "type": "Ride",
        "workout_type": null,
        "id": 4564044221,
        "external_id": "shealthc0902a36-8a0a-4a46-a2db-0a478dc7e75a.tcx",
        "upload_id": 4876036333,
        "start_date": "2021-01-04T05:09:48Z",
        "start_date_local": "2021-01-04T10:39:48Z",
        "timezone": "(GMT+05:30) Asia/Kolkata",
        "utc_offset": 19800.0,
     */

    private int resource_state;

    private Athlete athlete;

    private String name;

    private long distance;

    private long moving_time;

    private long elapsed_time;

    private double average_speed;

    private double total_elevation_gain;

    private String type;

    @Id
    private long id;

    private String start_date;

    private String start_date_local;

    private String timezone;

    private double utc_offset;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AthleteActivity activity = (AthleteActivity) o;
        return this.getId() == activity.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(includeFieldNames = false)
    public static final class Athlete implements Serializable {
        private long id;

        @CsvIgnore
        private int resource_state;
    }


}
