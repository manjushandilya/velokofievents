package com.velokofi.events.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.velokofi.events.util.Formatter.isBetween;
import static com.velokofi.events.util.Formatter.parse;
import static com.velokofi.events.util.NumberCruncher.round;
import static java.util.stream.Collectors.toList;

@Getter
@Setter
@ToString
public class BeatYesterdayPhasesSummary {

    private static final Logger LOG = LoggerFactory.getLogger(BeatYesterdayPhasesSummary.class);

    private static final Double MIN_LONGEST_RIDE = 60.0;
    private static final Double MAX_LONGEST_RIDE = 200.0;
    private static final Double MIN_TOTAL_DISTANCE = 500.0;
    private static final Double MAX_TOTAL_DISTANCE = 1200.0;

    @Getter
    @Setter
    @ToString
    public static class PhaseMetric {
        private Double actualLongestRideDistance = 0.0;
        private Double targetLongestRideDistance = 0.0;

        private boolean longestRideDistanceTargetAchieved;

        private Double actualTotalDistance = 0.0;
        private Double targetTotalDistance = 0.0;

        private boolean totalDistanceTargetAchieved;

        public String print() {
            final StringBuilder builder = new StringBuilder();
            builder.append("LongestRide=").append(round(actualLongestRideDistance)).append("/").append(round(targetLongestRideDistance));
            builder.append("(").append(longestRideDistanceTargetAchieved);
            builder.append("), Distance=").append(round(actualTotalDistance)).append("/").append(round(targetTotalDistance));
            builder.append("(").append(totalDistanceTargetAchieved).append(")");
            return builder.toString();
        }
    }

    private final Long athleteId;
    private final String athleteName;
    private final Map<Month, PhaseMetric> phaseMetrics;

    public BeatYesterdayPhasesSummary(
            final Long athleteId, final String athleteName,
            final List<AthleteActivity> athleteActivities,
            final int year, final Month start, final Month end) {
        this.athleteId = athleteId;
        this.athleteName = athleteName;
        this.phaseMetrics = new LinkedHashMap<>();

        Double baseLineLongestRideDistance = MIN_LONGEST_RIDE, baseLineTotalDistance = MIN_TOTAL_DISTANCE;
        for (int month = start.getValue(); month <= end.getValue(); month++) {
            final YearMonth yearMonth = YearMonth.of(year, month);
            final LocalDateTime phaseStart = yearMonth.atDay(1).atStartOfDay();
            final LocalDateTime phaseEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            final List<AthleteActivity> phaseActivities = filter(athleteActivities, phaseStart, phaseEnd);

            //LOG.debug("Filtering activities from: " + phaseStart + " till " + phaseEnd + " and found "
                    //+ phaseActivities.size() + " activities");

            final PhaseMetric metric = crunch(phaseActivities, baseLineLongestRideDistance, baseLineTotalDistance);
            phaseMetrics.put(Month.of(month), metric);

            if (metric.isLongestRideDistanceTargetAchieved()) {
                baseLineLongestRideDistance = Math.min(MAX_LONGEST_RIDE, metric.getTargetLongestRideDistance() + 20);
            } else {
                baseLineLongestRideDistance = MIN_LONGEST_RIDE;
            }

            if (metric.isTotalDistanceTargetAchieved()) {
                baseLineTotalDistance = Math.min(MAX_TOTAL_DISTANCE, metric.getTargetTotalDistance() * 1.1);
            } else {
                baseLineTotalDistance = MIN_TOTAL_DISTANCE;
            }
        }
    }

    private PhaseMetric crunch(final List<AthleteActivity> phaseActivities,
                               final Double targetLongestRideMinDistance,
                               final Double targetMinTotalDistance) {
        final PhaseMetric metric = new PhaseMetric();
        metric.setTargetLongestRideDistance(targetLongestRideMinDistance);
        metric.setTargetTotalDistance(targetMinTotalDistance);
        if (phaseActivities == null || phaseActivities.isEmpty()) {
            return metric;
        }

        final List<Long> phaseDistances = phaseActivities.stream().map(a -> a.getDistance()).toList();
        final Double actualLongestRideDistance = Collections.max(phaseDistances) / 1000.0;
        final Double targetLongestRideDistance;
        if (actualLongestRideDistance < targetLongestRideMinDistance) {
            targetLongestRideDistance = targetLongestRideMinDistance;
        } else if (actualLongestRideDistance > MAX_LONGEST_RIDE) {
            targetLongestRideDistance = MAX_LONGEST_RIDE;
        } else {
            targetLongestRideDistance = actualLongestRideDistance;
        }
        metric.setActualLongestRideDistance(actualLongestRideDistance);
        metric.setTargetLongestRideDistance(targetLongestRideDistance);
        metric.setLongestRideDistanceTargetAchieved(actualLongestRideDistance >= targetLongestRideDistance);

        final Double actualTotalDistance = phaseDistances.stream().map(Double::valueOf).reduce(0.0D, Double::sum) / 1000.0;
        final Double targetTotalDistance;
        if (actualTotalDistance < targetMinTotalDistance) {
            targetTotalDistance = targetMinTotalDistance;
        } else if (actualTotalDistance > MAX_TOTAL_DISTANCE) {
            targetTotalDistance = MAX_TOTAL_DISTANCE;
        } else {
            targetTotalDistance = actualTotalDistance;
        }
        metric.setActualTotalDistance(actualTotalDistance);
        metric.setTargetTotalDistance(targetTotalDistance);
        metric.setTotalDistanceTargetAchieved(actualTotalDistance >= targetTotalDistance);
        return metric;
    }

    private List<AthleteActivity> filter(final List<AthleteActivity> athleteActivities,
                                         final LocalDateTime after,
                                         final LocalDateTime before) {
        return athleteActivities.stream()
                .filter(a -> isBetween(after, before, parse(a.getStart_date_local())))
                .collect(toList());
    }

}
