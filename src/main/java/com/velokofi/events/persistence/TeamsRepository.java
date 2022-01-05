package com.velokofi.events.persistence;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.velokofi.events.model.hungryvelos.Team;
import com.velokofi.events.model.hungryvelos.TeamConstants;
import com.velokofi.events.model.hungryvelos.TeamMember;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeamsRepository implements TeamConstants {

    public List<Team> listTeams() throws Exception {
        final StringReader reader = new StringReader(TEAMS_CSV);
        final CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
        final List<String[]> allData = csvReader.readAll();

        final List<Team> teams = new ArrayList<>();
        final List<TeamMember> teamMembers = listTeamMembers();
        for (final String[] row : allData) {
            final Team team = new Team();
            final int teamId = Integer.parseInt(row[0].trim());
            team.setId(teamId);
            team.setName(row[1].trim());
            team.setCaptainId(Long.parseLong(row[2].trim()));
            team.setMembers(teamMembers.stream().filter(teamMember -> teamMember.getTeamId() == teamId).collect(Collectors.toList()));

            teams.add(team);
        }
        return teams;
    }

    public List<TeamMember> listTeamMembers() throws Exception {
        final StringReader reader = new StringReader(TEAM_MEMBERS_CSV);
        final CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
        final List<String[]> allData = csvReader.readAll();

        final List<TeamMember> teamMembers = new ArrayList<>();
        for (final String[] row : allData) {
            final TeamMember teamMember = new TeamMember();
            int columnIndex = 0;
            teamMember.setId(Long.parseLong(row[columnIndex++].trim()));
            teamMember.setName(row[columnIndex++].trim());
            teamMember.setAlias(row[columnIndex++].trim());
            teamMember.setGender(row[columnIndex++].trim());
            teamMember.setTeamId(Integer.parseInt(row[columnIndex++].trim()));
            teamMember.setCaptain(Boolean.parseBoolean(row[columnIndex++].trim()));

            teamMembers.add(teamMember);
        }
        return teamMembers;
    }

    public String getNameForId(final long id) {
        try {
            final StringReader reader = new StringReader(TEAM_MEMBERS_CSV);
            final CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
            final List<String[]> allData = csvReader.readAll();

            for (final String[] row : allData) {
                if (id == Long.parseLong(row[0].trim())) {
                    return row[1].trim();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
