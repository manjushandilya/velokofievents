package com.velokofi.events.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Team {

    private int id;

    private String name;

    private long captainId;

    private List<TeamMember> members;

}