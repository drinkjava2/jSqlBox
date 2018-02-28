package com.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.demo.model.Team;

@Service
public class TeamService {

	public void addTeam(Team team) {
		team.insert();
	}

	public void updateTeam(Team team) {
		team.update();
	}

	public Team getTeam(int id) {
		return new Team().load(id);
	}

	public void deleteTeam(int id) {
		new Team().put("id", id).delete();
	}

	public List<Team> getTeams() {
		return new Team().finaAllTeams();
	}

}
