package com.jsqlboxdemo.service;

import java.util.List;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.jsqlboxdemo.init.Initializer.TX;

import model.AbstractTeam;
import model.Team;

/**
 * This TeamService class should use a AOP tool to build a singleton instance to
 * make methods to controlled in transaction.
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class TeamService {

	public List<Team> listAll() {
		return SqlBoxContext.gctx().entityFindAll(Team.class);
	}

	@TX
	public List<Team> listEqual(Integer rating) {
		return new Team().queryTeamsRatingEqualTo(rating);
	}

	@TX
	public List<Team> listNotEqual(Integer rating) {
		return new Team().queryTeamsRatingNotEqual(rating);
	}

	@TX
	public List<Team> listBigger(Integer rating) {
		AbstractTeam team = SqlBoxContext.createMapper(AbstractTeam.class);
		return team.queryAbstractRatingBiggerThan(rating);
	}

}
