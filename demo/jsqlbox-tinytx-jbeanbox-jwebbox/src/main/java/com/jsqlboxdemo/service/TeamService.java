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
public class TeamService {

	/** If contexts is empty, will use globalSqlBoxContext */
	protected static SqlBoxContext getContext(SqlBoxContext... contexts) {
		SqlBoxContext ctx;
		if (contexts == null || contexts.length == 0)
			ctx = SqlBoxContext.getGlobalSqlBoxContext();
		else
			ctx = contexts[0];
		return ctx;
	}

	public List<Team> queryAllTeams(SqlBoxContext... contexts) {
		return getContext(contexts).netLoad(Team.class).getAllEntityList(Team.class);
	}

	public List<Team> queryTeamsBigger(Integer rating) {
		return new Team().getTeamsRatingBiggerThan(rating);
	}
}
