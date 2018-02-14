package com.jsqlboxdemo.service;

import java.util.List;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.jsqlboxdemo.init.Initializer.Transaction;

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

	public List<Team> queryAllTeams() {
		return SqlBoxContext.gctx().netLoad(Team.class).getAllEntityList(Team.class);
	}

	@Transaction
	public List<Team> getRatingNotZero() {
		return new Team().getRatingNotZero();
	}

	@Transaction
	public List<Team> queryAbstractRatingBiggerThan(Integer rating) {
		AbstractTeam team = AbstractTeam.create(AbstractTeam.class);
		return team.queryAbstractRatingBiggerThan(rating);
	}

}
