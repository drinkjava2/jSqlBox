package com.jsqlboxdemo.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.jsqlboxdemo.TestBase;

import model.Team;

/**
 * This is unit test for services
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class TeamServiceTest extends TestBase {

	@Test
	public void listAllTest() {
		List<Team> teams = teamServices.listAll();
		Assert.assertEquals(5, teams.size());
	}

	@Test
	public void ListEqualTest() {
		List<Team> teams = teamServices.listEqual(10);
		Assert.assertEquals(1, teams.size());
	}
 
}
