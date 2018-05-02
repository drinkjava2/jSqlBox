package com.demo.services;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.demo.TestBase;
import com.demo.model.Team;
import com.demo.service.TeamService;

/**
 * This is unit test for services
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class TeamServiceTest extends TestBase {
 
	@Test
	public void listAllTest() {
		List<Team> teams = teamServices.getTeams();
		Assert.assertEquals(5, teams.size());
	}

	@Test
	public void ListEqualTest() {
		List<Team> teams = teamServices.getTeam(id)
		Assert.assertEquals(1, teams.size());
	}
 
}
