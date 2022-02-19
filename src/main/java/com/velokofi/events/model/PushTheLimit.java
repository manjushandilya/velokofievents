package com.velokofi.events.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PushTheLimit implements Comparable<PushTheLimit> {

    private long phaseOneDistance;

    private long phaseTwoDistance;

    private String teamMemberName;

    private double getRatio() {
        return (double) phaseTwoDistance / phaseOneDistance;
    }

    @Override
    public String toString() {
        return teamMemberName + "," + phaseTwoDistance / 1000 + "," + phaseOneDistance / 1000 + "," + getRatio();
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
