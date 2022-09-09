package com.velokofi.events;

import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.BeatYesterdayPhasesSummary;
import org.junit.jupiter.api.Test;

import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeatYesterdayTest {

    @Test
    public void fiveHundredInJune() {
        final List<AthleteActivity> athleteActivities = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            athleteActivities.add(new AthleteActivity()
                    .setStart_date_local("2021-06-" + (i > 9 ? "" : "0") + i + "T06:00:00Z").setDistance(50000));
        }

        final BeatYesterdayPhasesSummary summary = new BeatYesterdayPhasesSummary(
                "37177283", "Manjunath Sathyanarayana",
                athleteActivities, 2021, Month.JUNE, Month.SEPTEMBER
        );

        final Map<Month, BeatYesterdayPhasesSummary.PhaseMetric> phaseMetrics = summary.getPhaseMetrics();
        for (final Month month : phaseMetrics.keySet()) {
            System.out.println(month + ": " + phaseMetrics.get(month).print());
        }
    }

    @Test
    public void thousandSixHundredInJune() {
        final List<AthleteActivity> athleteActivities = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            athleteActivities.add(new AthleteActivity()
                    .setStart_date_local("2021-06-" + (i > 9 ? "" : "0") + i + "T06:00:00Z").setDistance(100000));
        }

        final BeatYesterdayPhasesSummary summary = new BeatYesterdayPhasesSummary(
                "37177283", "Manjunath Sathyanarayana",
                athleteActivities, 2021, Month.JUNE, Month.SEPTEMBER
        );

        final Map<Month, BeatYesterdayPhasesSummary.PhaseMetric> phaseMetrics = summary.getPhaseMetrics();
        for (final Month month : phaseMetrics.keySet()) {
            System.out.println(month + ": " + phaseMetrics.get(month).print());
        }
    }

    @Test
    public void threeHundredInJune() {
        final List<AthleteActivity> athleteActivities = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            athleteActivities.add(new AthleteActivity()
                    .setStart_date_local("2021-06-" + (i > 9 ? "" : "0") + i + "T06:00:00Z").setDistance(100000));
        }

        final BeatYesterdayPhasesSummary summary = new BeatYesterdayPhasesSummary(
                "37177283", "Manjunath Sathyanarayana",
                athleteActivities, 2021, Month.JUNE, Month.SEPTEMBER
        );

        final Map<Month, BeatYesterdayPhasesSummary.PhaseMetric> phaseMetrics = summary.getPhaseMetrics();
        for (final Month month : phaseMetrics.keySet()) {
            System.out.println(month + ": " + phaseMetrics.get(month).print());
        }
    }

    @Test
    public void sixtyInJune() {
        final List<AthleteActivity> athleteActivities = new ArrayList<>();
        for (int i = 1; i <= 1; i++) {
            athleteActivities.add(new AthleteActivity()
                    .setStart_date_local("2021-06-" + (i > 9 ? "" : "0") + i + "T06:00:00Z").setDistance(60000));
        }

        final BeatYesterdayPhasesSummary summary = new BeatYesterdayPhasesSummary(
                "37177283", "Manjunath Sathyanarayana",
                athleteActivities, 2021, Month.JUNE, Month.SEPTEMBER
        );

        final Map<Month, BeatYesterdayPhasesSummary.PhaseMetric> phaseMetrics = summary.getPhaseMetrics();
        for (final Month month : phaseMetrics.keySet()) {
            System.out.println(month + ": " + phaseMetrics.get(month).print());
        }
    }

    @Test
    public void twoHundredInJune() {
        final List<AthleteActivity> athleteActivities = new ArrayList<>();
        for (int i = 1; i <= 1; i++) {
            athleteActivities.add(new AthleteActivity()
                    .setStart_date_local("2021-06-" + (i > 9 ? "" : "0") + i + "T06:00:00Z").setDistance(200000));
        }

        final BeatYesterdayPhasesSummary summary = new BeatYesterdayPhasesSummary(
                "37177283", "Manjunath Sathyanarayana",
                athleteActivities, 2021, Month.JUNE, Month.SEPTEMBER
        );

        final Map<Month, BeatYesterdayPhasesSummary.PhaseMetric> phaseMetrics = summary.getPhaseMetrics();
        for (final Month month : phaseMetrics.keySet()) {
            System.out.println(month + ": " + phaseMetrics.get(month).print());
        }
    }

    @Test
    public void thirtyInJune() {
        final List<AthleteActivity> athleteActivities = new ArrayList<>();
        for (int i = 1; i <= 1; i++) {
            athleteActivities.add(new AthleteActivity()
                    .setStart_date_local("2021-06-" + (i > 9 ? "" : "0") + i + "T06:00:00Z").setDistance(30000));
        }

        final BeatYesterdayPhasesSummary summary = new BeatYesterdayPhasesSummary(
                "37177283", "Manjunath Sathyanarayana",
                athleteActivities, 2021, Month.JUNE, Month.SEPTEMBER
        );

        final Map<Month, BeatYesterdayPhasesSummary.PhaseMetric> phaseMetrics = summary.getPhaseMetrics();
        for (final Month month : phaseMetrics.keySet()) {
            System.out.println(month + ": " + phaseMetrics.get(month).print());
        }
    }

    @Test
    public void deleteMe() {
        final ZoneOffset IST = ZoneOffset.of("+05:30");
        final OffsetDateTime after = OffsetDateTime.of(2022, 8, 1, 0, 0, 0, 0, IST);
        System.out.println("After: " + after.toEpochSecond());

        final OffsetDateTime before = OffsetDateTime.of(2022, 8, 31, 11, 59, 59, 0, IST);
        System.out.println("Before: " + before.toEpochSecond());

    }



}
