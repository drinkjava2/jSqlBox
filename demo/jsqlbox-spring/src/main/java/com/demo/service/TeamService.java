package com.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.model.Team;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.JSQLBOX;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

@Service
@Transactional
public class TeamService {

	public void addTeam(Team team) {
		team.insert();
	}

	public void updateTeam(Team team) {
		team.update();
	}

	public Team getTeam(Integer id) {
		return new Team().loadById(id);
	}

	public void deleteTeam(int id) {
		new Team().putField("id", id).delete();
	}

	public List<Team> getTeams() {
		return new Team().finaAllTeams();
	}

	public List<Team> getTeamByName(String name) {
		return JSQLBOX.qry(new EntityListHandler(), Team.class, "select t.* from teams t where t.name=?", DB.par(name));
	}

}
