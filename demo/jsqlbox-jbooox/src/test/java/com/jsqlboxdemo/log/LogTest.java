package com.jsqlboxdemo.log;

import org.junit.Test;

import com.github.drinkjava2.jlogs.Log;
import com.github.drinkjava2.jlogs.LogFactory;

/**
 * This is unit test for services
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class LogTest {
	Log log = LogFactory.getLog(LogTest.class);

	@Test
	public void logTest() {
		log.info("Log test");
	}

}
