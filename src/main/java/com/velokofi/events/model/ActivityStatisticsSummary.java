package com.velokofi.events.model;

import com.velokofi.events.model.hungryvelos.TeamMember;
import com.velokofi.events.util.NumberCruncher;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

import static com.velokofi.events.Application.MetricType.DISTANCE;
import static com.velokofi.events.util.Formatter.convertMetersToKilometers;

@Getter
@ToString
public class ActivityStatisticsSummary {

    final String athleteName;
    final BigDecimal ytdDistance;

    final BigDecimal allTimeDistance;

    public ActivityStatisticsSummary(final ActivityStatistics activityStatistics, final List<TeamMember> teamMembers) {
        this.athleteName = NumberCruncher.getNameFromId(Long.parseLong(activityStatistics.getAthleteId()), teamMembers);

        double totalYtdDistance = NumberCruncher.getValue(DISTANCE, activityStatistics.getYtd_ride_totals().getDistance());
        this.ytdDistance = new BigDecimal(convertMetersToKilometers(totalYtdDistance));

        double totalAllTimeDistance = NumberCruncher.getValue(DISTANCE, activityStatistics.getAll_ride_totals().getDistance());
        this.allTimeDistance = new BigDecimal(convertMetersToKilometers(totalAllTimeDistance));
    }

}
