package com.velokofi.events.model;

import com.velokofi.events.Application;
import com.velokofi.events.util.NumberCruncher;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

import static com.velokofi.events.Application.MetricType.DISTANCE;
import static com.velokofi.events.util.Formatter.convertMetersToKilometers;

@Getter
@ToString
public class ActivityStatisticsSummary {

    final String athleteName;

    final BigDecimal ytdDistance;

    final BigDecimal percentComplete;

    final BigDecimal allTimeDistance;

    public ActivityStatisticsSummary(final ActivityStatistics activityStatistics) {
        this.athleteName = activityStatistics.getAthleteName();

        double totalYtdDistance = NumberCruncher.getValue(DISTANCE, activityStatistics.getYtd_ride_totals().getDistance());
        this.ytdDistance = new BigDecimal(convertMetersToKilometers(totalYtdDistance));

        this.percentComplete = new BigDecimal(totalYtdDistance / Application.PLEDGE_DISTANCE);

        double totalAllTimeDistance = NumberCruncher.getValue(DISTANCE, activityStatistics.getAll_ride_totals().getDistance());
        this.allTimeDistance = new BigDecimal(convertMetersToKilometers(totalAllTimeDistance));
    }

}
