package com.jsqlboxdemo.service;

import java.util.List;

import com.github.drinkjava2.jbeanbox.AopAround;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.jsqlboxdemo.init.Initializer.TxBox;

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

	@AopAround(TxBox.class)
	public List<Team> queryBeamsRatingBiggerThan(Integer rating) {
		return new Team().queryBeamsRatingBiggerThan(rating);
	}
}
