package com.velokofi.events.model.hungryvelos;

import com.velokofi.events.model.AthleteProfile;
import com.velokofi.events.model.AthleteSummary;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class LeaderBoard {

    private AthleteProfile athleteProfile;

    // event totals
    private Double totalDistance;
    private Double totalElevation;
    private int totalRides;
    private long movingTime;
    private Double riderAverage;
    private int riderCount;

    private String movingTimeInHumanReadableFormat;

    // team totals
    private Map<String, Double> teamDistanceMap;
    private Map<String, Double> teamElevationMap;
    private Map<String, Double> teamRidesMap;
    private Map<String, Double> teamSpeedMap;

    // team averages
    private Map<String, Double> teamAvgDistanceMap;
    private Map<String, Double> teamAvgElevationMap;
    private Map<String, Double> teamAvgRidesMap;
    private Map<String, Double> teamAvgSpeedMap;

    // individual totals
    private List<Entry<String, Double>> bettappa;
    private List<Entry<String, Double>> bettamma;
    private List<Entry<String, Double>> mrAlemaari;
    private List<Entry<String, Double>> msAlemaari;
    private List<Entry<String, Double>> minchinaOtappa;
    private List<Entry<String, Double>> minchinaOtamma;
    private List<Entry<String, Long>> mrThuliMaga;
    private List<Entry<String, Long>> msThuliMaga;

    // for tabular data
    private List<AthleteSummary> athleteSummaries;
    private List<TeamSummary> teamSummaries;

}
