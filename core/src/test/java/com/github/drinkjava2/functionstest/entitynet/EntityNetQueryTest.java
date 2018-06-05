package com.github.drinkjava2.functionstest.entitynet;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQuery;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.gpQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.functionstest.entitynet.entities.Address;
import com.github.drinkjava2.functionstest.entitynet.entities.Email;
import com.github.drinkjava2.functionstest.entitynet.entities.Privilege;
import com.github.drinkjava2.functionstest.entitynet.entities.Role;
import com.github.drinkjava2.functionstest.entitynet.entities.RolePrivilege;
import com.github.drinkjava2.functionstest.entitynet.entities.User;
import com.github.drinkjava2.functionstest.entitynet.entities.UserRole;
import com.github.drinkjava2.jsqlbox.entitynet.DefaultNodeValidator;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.entitynet.Node;
import com.github.drinkjava2.jsqlbox.entitynet.Path;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.MapListWrap;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;
import com.github.drinkjava2.jsqlbox.handler.SSMapListWrapHandler;

public class EntityNetQueryTest extends TestBase {
	@Before
	public void init() {
		super.init();
		createAndRegTables(User.class, Email.class, Address.class, Role.class, Privilege.class, UserRole.class,
				RolePrivilege.class);
	}

	@Test
	public void testEntityListHandler() {
		System.out.println("==============testJoinFields================ ");
		new User().put("id", "u1").put("userName", "user1").put("age", 10).insert();
		new User().put("id", "u2").put("userName", "user2").put("age", 20).insert();
		new User().put("id", "u3").put("userName", "user3").put("age", 30).insert();
		List<User> users = gpQuery(new EntityListHandler(User.class), "select * from usertb where age>?", 10);
		Assert.assertEquals(2, users.size());

		List<User> users2 = giQuery(new EntityListHandler(User.class), "select * from usertb where age>?", param(10));

		Assert.assertEquals(2, users2.size());
	}

	@Test
	public void testJoinFields() {
		System.out.println("==============testJoinFields================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		new User().put("id", "u2").put("userName", "user2").insert();
		EntityNet net = ctx.netLoadSketch(User.class);
		Assert.assertEquals(2, net.size());
		List<User> users = net.selectEntityList(User.class);
		Assert.assertNull(users.get(0).getUserName());

		List<Map<String, Object>> listMap = gpQuery(MapListHandler.class,
				"select u.id as u_id, u.userName as u_userName from usertb as u");
		net.add(listMap, new User().alias("u"));// userName joined

		Assert.assertEquals(2, net.size());
		users = net.selectEntityList(User.class);
		Assert.assertNotNull(users.get(0).getUserName());
	}

	@Test
	public void testJoinParents() {
		System.out.println("==============testJoinParents================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		new User().put("id", "u2").put("userName", "user2").insert();
		new Email().putFields("id", "emailName", "userId");
		new Email().putValues("e1", "email1", "u1").insert();
		new Email().putValues("e2", "email2", "u1").insert();

		MapListWrap wrap = gpQuery(new SSMapListWrapHandler(new Email().alias("e")),
				"select e.id as e_id from emailtb e");
		System.out.println(wrap.getMapList());

		EntityNet net = (EntityNet) ctx.netCreate(wrap);
		Assert.assertEquals(2, net.size());
		Node emailNode = net.selectOneNode(Email.class, "e1");
		Assert.assertNull(emailNode.getParentRelations());// e1 have no userId

		List<Map<String, Object>> listMap2 = gpQuery(new SSMapListHandler(Email.class), "select e.** from emailtb e");
		net.add(listMap2);// alias still is e

		Assert.assertEquals(2, net.size());
		emailNode = net.selectOneNode(Email.class, "e1");
		Assert.assertNotNull(emailNode.getParentRelations());
	}

	@Test
	public void testFindSelf() {
		System.out.println("==============testFindSelf================ ");
		int sampleSize = 500;
		int queyrTimes = 200;
		for (int i = 1; i <= sampleSize; i++) {
			new User().put("id", "u" + i).put("userName", "user" + i).insert();
		}
		EntityNet net = ctx.netLoadAll(new User(), Email.class);

		Set<User> users1 = net.runPath(new Path("S-", User.class)).selectEntitySet(User.class);
		Assert.assertEquals(0, users1.size());

		long start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			Set<User> users2 = net.runPath(new Path("S+", User.class)).selectEntitySet(User.class);
			Assert.assertTrue(users2.size() > 0);
		}
		printTimeUsed(start, "Find self");
	}

	@Test
	public void testFindChild() {// This unit test will put on user manual
		int sampleSize = 30;
		int queyrTimes = 30;
		for (int i = 0; i < sampleSize; i++) {
			new User().put("id", "usr" + i).put("userName", "user" + i).insert();
			for (int j = 0; j < sampleSize; j++)
				new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
		}
		EntityNet net = ctx.netLoadAll(new User(), Email.class);

		Map<Class<?>, Set<Object>> result = null;
		long start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			result = net.runPath(new Path("S+", User.class).nextPath("C+", Email.class, "userId")).selectEntitySetMap();
		}
		printTimeUsed(start, "Find Childs with Cache");
		System.out.println("user selected2:" + result.get(User.class).size());
		System.out.println("email selected2:" + result.get(Email.class).size());
	}

	public static class MyBeanValidator extends DefaultNodeValidator {
		@Override
		public boolean validateBean(Object entity) {
			if (!((Email) entity).getId().equals("NotExist"))
				return true;
			return false;
		}
	}

	@Test
	public void testValidateByBeanValidator() {
		System.out.println("==============testValidateByBeanValidator================ ");
		int sampleSize = 30;
		int queyrTimes = 60;
		for (int i = 0; i < sampleSize; i++) {
			new User().put("id", "usr" + i).put("userName", "user" + i).insert();
			for (int j = 0; j < sampleSize; j++)
				new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
		}
		EntityNet net = ctx.netLoadAll(User.class, Email.class);
		long start = System.currentTimeMillis();
		Set<Email> emails = null;
		Path p = new Path("S-", User.class).nextPath("C+", Email.class, "userId").setValidator(new MyBeanValidator());
		for (int i = 0; i < queyrTimes; i++)
			emails = net.runPath(p).selectEntitySet(Email.class);
		printTimeUsed(start, "Bean Validator instance ");
		Assert.assertEquals(sampleSize * sampleSize, emails.size());
	}

	@Test
	public void testValidateByExpression() {
		System.out.println("==============testValidateByExpression================ ");
		// no cache
		int sampleSize = 20;
		int queyrTimes = 20;
		for (int i = 0; i < sampleSize; i++) {
			new User().put("id", "usr" + i).put("userName", "user" + i).put("age", i).insert();
			for (int j = 0; j < sampleSize; j++)
				new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
		}
		EntityNet net = ctx.netLoadAll(User.class, Email.class);
		long start = System.currentTimeMillis();
		Set<Email> emails = null;
		// Set expression query parameters will not cache query result
		Path p = new Path("S-", User.class).where("age>?", 10).nextPath("C+", Email.class, "userId")
				.where("id startwith ? or emailName=?", "email1", "Bar");
		for (int i = 0; i < queyrTimes; i++)
			emails = net.runPath(p).selectEntitySet(Email.class);
		printTimeUsed(start, "Validate by expression with parameters");
		Assert.assertEquals(180, emails.size());

		// Do not have query parameters
		start = System.currentTimeMillis();
		p = new Path("S-", User.class).where("age>10").nextPath("C+", Email.class, "userId")
				.where("id startwith 'email1' or emailName='Bar'");
		for (int i = 0; i < queyrTimes; i++)
			emails = net.runPath(p).selectEntitySet(Email.class);
		printTimeUsed(start, "Validate by expression no parameters");
		Assert.assertEquals(180, emails.size());
	}

	@Test
	public void testFindParent() {
		System.out.println("==============testFindParent================ ");
		int sampleSize = 20;
		int queyrTimes = 10;
		for (int i = 0; i < sampleSize; i++) {
			new User().put("id", "usr" + i).put("userName", "user" + i).insert();
			for (int j = 0; j < sampleSize; j++)
				new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
		}
		EntityNet net = ctx.netLoadAll(new User(), Email.class);
		Map<Class<?>, Set<Object>> result = null;
		long start = System.currentTimeMillis();

		start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			result = net.runPath(new Path("S+", Email.class).nextPath("P+", User.class, "userId")).selectEntitySetMap();
		}
		printTimeUsed(start, "Find parent (no cache)");
		System.out.println("user selected2:" + result.get(User.class).size());
		System.out.println("email selected2:" + result.get(Email.class).size());
	}

	@Test
	public void testAddEntity() {
		System.out.println("==============testAddEntity================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		EntityNet net = ctx.netLoadAll(User.class);
		Assert.assertEquals(1, net.size());
		User u2 = new User();
		u2.setId("u2");
		u2.setUserName("user2");
		u2.insert();
		net.addEntity(u2);

		Assert.assertEquals(2, net.size());
		User u = net.selectOneEntity(User.class, "u2");
		Assert.assertEquals("user2", u.getUserName());
	}

	@Test
	public void testRemoveEntity() {
		System.out.println("==============testRemoveEntity================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		new User().put("id", "u2").put("userName", "user2").insert();
		EntityNet net = ctx.netLoadAll(User.class);
		Assert.assertEquals(2, net.size());
		User u2 = net.selectOneEntity(User.class, "u2");
		net.removeEntity(u2);
		Assert.assertEquals(1, net.size());
	}

	@Test
	public void testUpdateEntity() {
		System.out.println("==============testUpdateEntity================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		new User().put("id", "u2").put("userName", "user2").insert();
		EntityNet net = ctx.netLoadAll(User.class);
		Assert.assertEquals(2, net.size());
		User u2 = net.selectOneEntity(User.class, "u2");
		u2.setUserName("newName");
		net.updateEntity(u2);

		u2 = net.selectOneEntity(User.class, "u2");
		Assert.assertEquals("newName", u2.getUserName());
	}
}