package com.velokofi.events.model;

import com.velokofi.events.Application;
import com.velokofi.events.util.NumberCruncher;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static com.velokofi.events.Application.MetricType.DISTANCE;
import static com.velokofi.events.util.Formatter.convertMetersToKilometers;

@Getter
@ToString
public class ActivityStatisticsSummary {

    final String athleteName;

    final BigDecimal ytdDistance;

    final BigDecimal percentComplete;

    final String percentCompleteString;

    final BigDecimal allTimeDistance;

    public ActivityStatisticsSummary(final ActivityStatistics activityStatistics) {
        this.athleteName = activityStatistics.getAthleteName();

        double totalYtdDistance = NumberCruncher.getValue(DISTANCE, activityStatistics.getYtd_ride_totals().getDistance());
        this.ytdDistance = new BigDecimal(convertMetersToKilometers(totalYtdDistance));

        this.percentComplete = ytdDistance.divide(Application.PLEDGE_DISTANCE, new MathContext(
                ytdDistance.toBigInteger().toString().length(),
                RoundingMode.HALF_UP
        ));

        this.percentCompleteString = Double.valueOf(NumberCruncher.round(percentComplete.floatValue() * 100)).toString();

        double totalAllTimeDistance = NumberCruncher.getValue(DISTANCE, activityStatistics.getAll_ride_totals().getDistance());
        this.allTimeDistance = new BigDecimal(convertMetersToKilometers(totalAllTimeDistance));
    }

}
