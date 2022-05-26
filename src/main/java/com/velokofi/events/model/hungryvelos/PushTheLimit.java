package com.velokofi.events.model.hungryvelos;

import com.velokofi.events.util.NumberCruncher;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

import static com.velokofi.events.util.Formatter.humanReadableFormat;

@Getter
@Setter
public class PushTheLimit implements Comparable<PushTheLimit> {

    private String teamMemberName;

    private long phaseOneDistance;

    private long phaseTwoDistance;

    private double phaseOneElevation;

    private double phaseTwoElevation;

    private long phaseOneMovingTime;

    private long phaseTwoMovingTime;

    private double getRatio() {
        return (double) phaseTwoDistance / phaseOneDistance;
    }

    @Override
    public String toString() {
        return teamMemberName
                + "," + (double) phaseTwoDistance / 1000 + "," + (double) phaseOneDistance / 1000 + "," + getRatio()
                + "," + NumberCruncher.round(phaseOneElevation) + "," + NumberCruncher.round(phaseTwoElevation)
                + "," + humanReadableFormat(Duration.ofSeconds(phaseOneMovingTime)) + "," + humanReadableFormat(Duration.ofSeconds(phaseTwoMovingTime));
    }

    @Override
    public int compareTo(final PushTheLimit that) {
        if (this.getRatio() > that.getRatio()) {
            return -1;
        } else if (this.getRatio() > that.getRatio()) {
            return 1;
        } else {
            return 0;
        }
    }

}
