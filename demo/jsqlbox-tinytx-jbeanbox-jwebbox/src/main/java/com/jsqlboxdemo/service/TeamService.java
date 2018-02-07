package com.jsqlboxdemo.service;

import java.util.List;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import model.Team;

/**
 * This TeamService class should use a AOP tool to build a singleton instance to
 * make methods to controlled in transaction.
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class TeamService extends PublicService {

	public List<Team> getTeams(SqlBoxContext... contexts) {
		return getContext(contexts).netLoad(Team.class).getAllEntityList(Team.class);
	}

}
