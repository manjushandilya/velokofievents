package com.velokofi.events.util;

import com.velokofi.events.VeloKofiEventsApplication;
import com.velokofi.events.model.AthleteActivity;
import com.velokofi.events.model.Team;
import com.velokofi.events.model.TeamMember;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.*;

public final class NumberCruncher {

    public static String getNameFromId(final Long id, final List<TeamMember> teamMembers) {
        final Optional<TeamMember> optional = teamMembers.stream().filter(tm -> tm.getId() == id).findFirst();
        return optional.isPresent() ? optional.get().getName() : null;
    }

    public static String getGenderFromId(final Long id, final List<TeamMember> teamMembers) {
        final Optional<TeamMember> optional = teamMembers.stream().filter(tm -> tm.getId() == id).findFirst();
        return optional.isPresent() ? optional.get().getGender() : null;
    }

    public static long getTeamMemberCount(final String teamName, final List<Team> teams) {
        final Optional<Team> optional = teams.stream().filter(t -> t.getName().equals(teamName)).findFirst();
        if (optional.isPresent()) {
            final Team team = optional.get();
            return team.getMembers().size();
        }
        return 0;
    }

    public static List<Map.Entry<String, Double>> summingAggregateDoubleNoLimit(final List<AthleteActivity> activities,
                                                                                final List<TeamMember> teamMembers,
                                                                                final String gender,
                                                                                final VeloKofiEventsApplication.MetricType metricType) {
        final Map<String, Double> aggregateMap = activities.stream()
                .filter(a -> filterBasedOnGender(a.getAthlete(), teamMembers, gender))
                .collect(groupingBy(
                        a -> getNameFromId(a.getAthlete().getId(), teamMembers),
                        summingDouble(a -> round(getValue(metricType, a))))
                );

        final Stream<Map.Entry<String, Double>> aggregateSorted = aggregateMap.entrySet().stream().sorted(comparingByValue(reverseOrder()));
        return aggregateSorted.collect(toList());
    }

    public static List<Map.Entry<String, Double>> summingAggregateDouble(final List<AthleteActivity> activities,
                                                                         final List<TeamMember> teamMembers,
                                                                         final String gender,
                                                                         final VeloKofiEventsApplication.MetricType metricType) {
        final Map<String, Double> aggregateMap = activities.stream()
                .filter(a -> filterBasedOnGender(a.getAthlete(), teamMembers, gender))
                .collect(groupingBy(
                        a -> getNameFromId(a.getAthlete().getId(), teamMembers),
                        summingDouble(a -> round(getValue(metricType, a))))
                );

        final Stream<Map.Entry<String, Double>> aggregateSorted = aggregateMap.entrySet().stream().sorted(comparingByValue(reverseOrder()));
        return aggregateSorted.limit(VeloKofiEventsApplication.LEADER_BOARD_LIMIT).collect(toList());
    }

    public static List<Map.Entry<String, Double>> averagingAggregateDoubleNoLimit(final List<AthleteActivity> activities,
                                                                                  final List<TeamMember> teamMembers,
                                                                                  final String gender,
                                                                                  final VeloKofiEventsApplication.MetricType metricType) {
        final Map<String, Double> aggregateMap = activities.stream()
                .filter(a -> filterBasedOnGender(a.getAthlete(), teamMembers, gender))
                .collect(groupingBy(
                        a -> getNameFromId(a.getAthlete().getId(), teamMembers),
                        averagingDouble(a -> round(getValue(metricType, a) * 3.6)))
                );

        final Stream<Map.Entry<String, Double>> aggregateSorted = aggregateMap.entrySet().stream().sorted(comparingByValue(reverseOrder()));
        return aggregateSorted.collect(toList());
    }

    public static List<Map.Entry<String, Double>> averagingAggregateDouble(final List<AthleteActivity> activities,
                                                                           final List<TeamMember> teamMembers,
                                                                           final String gender,
                                                                           final VeloKofiEventsApplication.MetricType metricType) {
        final Map<String, Double> aggregateMap = activities.stream()
                .filter(a -> filterBasedOnGender(a.getAthlete(), teamMembers, gender))
                .collect(groupingBy(
                        a -> getNameFromId(a.getAthlete().getId(), teamMembers),
                        averagingDouble(a -> round(getValue(metricType, a) * 3.6)))
                );

        final Stream<Map.Entry<String, Double>> aggregateSorted = aggregateMap.entrySet().stream().sorted(comparingByValue(reverseOrder()));
        return aggregateSorted.limit(VeloKofiEventsApplication.LEADER_BOARD_LIMIT).collect(toList());
    }

    public static List<Map.Entry<String, Long>> summingAggregateLongNoLimit(final List<AthleteActivity> activities,
                                                                            final List<TeamMember> teamMembers,
                                                                            final String gender) {

        final Map<String, Long> map = activities.stream()
                .filter(a -> getGenderFromId(a.getAthlete().getId(), teamMembers).equalsIgnoreCase(gender))
                .collect(groupingBy(a -> a.getAthlete().getId(), counting()))
                .entrySet().stream().collect(
                        toMap(e -> getNameFromId(e.getKey(), teamMembers), e -> e.getValue())
                );

        final Stream<Map.Entry<String, Long>> aggregateSorted = map.entrySet().stream().sorted(comparingByValue(reverseOrder()));
        return aggregateSorted.collect(toList());
    }

    public static List<Map.Entry<String, Long>> summingAggregateLong(final List<AthleteActivity> activities,
                                                                     final List<TeamMember> teamMembers,
                                                                     final String gender) {

        final Map<String, Long> map = activities.stream()
                .filter(a -> getGenderFromId(a.getAthlete().getId(), teamMembers).equalsIgnoreCase(gender))
                .collect(groupingBy(a -> a.getAthlete().getId(), counting()))
                .entrySet().stream().collect(
                        toMap(e -> getNameFromId(e.getKey(), teamMembers), e -> e.getValue())
                );

        final Stream<Map.Entry<String, Long>> aggregateSorted = map.entrySet().stream().sorted(comparingByValue(reverseOrder()));
        return aggregateSorted.limit(VeloKofiEventsApplication.LEADER_BOARD_LIMIT).collect(toList());
    }

    public static double getValue(final VeloKofiEventsApplication.MetricType metricType, double a) {
        if (metricType == VeloKofiEventsApplication.MetricType.DISTANCE) {
            return round(a);
        } else if (metricType == VeloKofiEventsApplication.MetricType.ELEVATION) {
            return a;
        } else {
            return a;
        }
    }

    public static double getValue(final VeloKofiEventsApplication.MetricType metricType, final AthleteActivity a) {
        if (metricType == VeloKofiEventsApplication.MetricType.DISTANCE) {
            return getValue(metricType, a.getDistance() / 1000D);
        } else if (metricType == VeloKofiEventsApplication.MetricType.ELEVATION) {
            return getValue(metricType, a.getTotal_elevation_gain());
        } else {
            return getValue(metricType, a.getAverage_speed());
        }
    }

    public static boolean filterBasedOnGender(final AthleteActivity.Athlete athlete, final List<TeamMember> teamMembers, final String gender) {
        final Optional<TeamMember> optional = teamMembers.stream().filter(
                teamMember -> teamMember.getId() == athlete.getId() && gender.equals(teamMember.getGender())).findFirst();
        return optional.isPresent();
    }

    public static double getAthleteAggregateDouble(final Team team, final Map<Long, Double> map) {
        final List<TeamMember> members = team.getMembers();
        double total = 0;
        for (final TeamMember member : members) {
            if (map.containsKey(member.getId())) {
                total += map.get(member.getId());
            }
        }
        return total;
    }

    public static double getAthleteAverageDouble(final Team team, final Map<Long, Double> map) {
        final List<TeamMember> members = team.getMembers();
        double total = 0;
        int count = 0;
        for (final TeamMember member : members) {
            if (map.containsKey(member.getId())) {
                total += map.get(member.getId());
                count++;
            }
        }
        return count > 0 ? total / count : 0;
    }

    public static long getAthleteAggregate(final Team team, final Map<Long, Long> map) {
        final List<TeamMember> members = team.getMembers();
        long total = 0;
        for (final TeamMember member : members) {
            if (map.containsKey(member.getId())) {
                total += map.get(member.getId());
            }
        }
        return total;
    }

    public static double round(final double val) {
        return new BigDecimal(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static BigDecimal round(final BigDecimal input) {
        return input.round(
                new MathContext(
                        input.toBigInteger().toString().length(),
                        RoundingMode.HALF_UP
                )
        );
    }

}
