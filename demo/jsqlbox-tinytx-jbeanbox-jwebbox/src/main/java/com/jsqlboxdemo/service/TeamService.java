package com.jsqlboxdemo.service;

import java.util.List;

import org.junit.Assert;

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

	public void addOneTeam(Long oldQTY) {
		Team t = new Team();
		t.setName("bar");
		t.setRating(8);
		t.insert();
		Assert.assertTrue(oldQTY + 2 == SqlBoxContext.gctx().nQueryForLongValue("select count(*) from teams"));
		System.out.println(1 / 0);
	}

	@AopAround(TxBox.class)
	public void transactionRollBackTest() {
		Long oldQTY = SqlBoxContext.gctx().nQueryForLongValue("select count(*) from teams");
		Team t = new Team();
		t.setName("foo");
		t.setRating(8);
		t.insert();
		Assert.assertTrue(oldQTY + 1 == SqlBoxContext.gctx().nQueryForLongValue("select count(*) from teams"));
		addOneTeam(oldQTY);
	}
}
