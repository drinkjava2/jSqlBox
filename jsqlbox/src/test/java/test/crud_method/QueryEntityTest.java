package test.crud_method;

import org.junit.Before;

import test.config.Config;

public class QueryEntityTest {

	@Before
	public void setup() {
		Config.recreateTables();
	}

}