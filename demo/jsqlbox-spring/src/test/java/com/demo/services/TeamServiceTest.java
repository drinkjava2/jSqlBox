package com.demo.services;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.demo.init.BaseTestConfig;
import com.demo.model.Team;
import com.demo.service.TeamService;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.JSQLBOX;

/**
 * This is unit test for services
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = BaseTestConfig.class)
public class TeamServiceTest {
	@Autowired
	public TeamService teamService;

	@Autowired
	public DbContext ctx;

	@Before
	public void createDB() {
		String[] ddls = ctx.toCreateDDL(Team.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);
		for (int i = 0; i < 5; i++)
			new Team().putField("name", "Team" + i, "rating", i * 10).insert(new PrintSqlHandler());
		System.out.println("========== TeamServiceTest initialized=====");
	}

	@After
	public void cleanUp() {
		String[] ddls = JSQLBOX.gctx().toDropDDL(Team.class);
		for (String ddl : ddls)
			JSQLBOX.gctx().nExecute(ddl);
		System.out.println("========== TeamServiceTest clean up done=====");
	}

	@Test
	public void listAllTest() {
		List<Team> teams = teamService.getTeams();
		Assert.assertEquals(5, teams.size());
	}

	@Test
	public void ListEqualTest() {
		List<Team> teams = teamService.getTeamByName("Team1");
		Assert.assertEquals(1, teams.size());
	}

}
