package test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test template
 */
/**
 * @author Yong Zhu
 * @since 2016-08-30
 *
 */
public class AppTest {
	public AppTest() {
		System.out.println("Constructor");
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		System.out.println("BeforeClass");
	}

	@AfterClass
	public static void tearDownAfterClass() {
		System.out.println("AfterClass");
	}

	@Before
	public void setUp() {
		System.out.println("Before");
	}

	@After
	public void tearDown() {
		System.out.println("After");
	}

	@Test
	public void test1() {
		System.out.println("test1");
	}

	@Test
	public void test2() {
		System.out.println("test2");
	}

	@Ignore
	public void test3() {
		System.out.println("test3");
	}
}
