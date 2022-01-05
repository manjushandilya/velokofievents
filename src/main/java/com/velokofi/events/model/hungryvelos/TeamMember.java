package com.velokofi.events.model.hungryvelos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TeamMember {

    private long id;

    private String name;

    private String alias;

    private String gender;

    private int teamId;

    private boolean captain;
}
