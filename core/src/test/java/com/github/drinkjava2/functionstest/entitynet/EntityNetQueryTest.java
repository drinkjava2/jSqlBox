package com.github.drinkjava2.functionstest.entitynet;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.DefaultNodeValidator;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.entitynet.Node;
import com.github.drinkjava2.jsqlbox.entitynet.Path;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.EntitySqlMapListHandler;

public class EntityNetQueryTest extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		TableModel[] models = TableModelUtils.entity2Models(User.class, Email.class, Address.class, Role.class,
				Privilege.class, UserRole.class, RolePrivilege.class);
		dropAndCreateDatabase(models);
	}

	@Test
	public void testEntityListHandler() {
		System.out.println("==============testJoinFields================ ");
		new User().put("id", "u1").put("userName", "user1").put("age", 10).insert();
		new User().put("id", "u2").put("userName", "user2").put("age", 20).insert();
		new User().put("id", "u3").put("userName", "user3").put("age", 30).insert();
		List<User> setResult = ctx.nQuery(new EntityListHandler(User.class), "select u.** from usertb u where u.age>?",
				10);
		Assert.assertTrue(setResult.size() == 2);
	}

	@Test
	public void testJoinFields() {
		System.out.println("==============testJoinFields================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		new User().put("id", "u2").put("userName", "user2").insert();
		EntityNet net = ctx.netLoadSketch(User.class);
		Assert.assertEquals(2, net.size());
		List<User> users = net.getAllEntityList(User.class);
		Assert.assertNull(users.get(0).getUserName());

		User u = new User();
		u.tableModel().setAlias("u");
		List<Map<String, Object>> listMap = ctx.nQuery(new EntitySqlMapListHandler(u),
				"select u.id as u_id, u.userName as u_userName from usertb as u");
		ctx.netJoinList(net, listMap);// not userName be joined

		Assert.assertEquals(2, net.size());
		users = net.getAllEntityList(User.class);
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
		List<Map<String, Object>> listMap = ctx.nQuery(new EntitySqlMapListHandler(new Email().alias("e")),
				"select e.id as e_id from emailtb e");
		EntityNet net = (EntityNet) ctx.netCreate(listMap);
		Assert.assertEquals(2, net.size());
		Node emailNode = net.getOneNode(Email.class, "e1");
		Assert.assertNull(emailNode.getParentRelations());// e1 have no userId
															// field

		List<Map<String, Object>> listMap2 = ctx.nQuery(new EntitySqlMapListHandler(Email.class),
				"select e.** from emailtb e");
		ctx.netJoinList(net, listMap2);

		Assert.assertEquals(2, net.size());
		emailNode = net.getOneNode(Email.class, "e1");
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
		EntityNet net = ctx.netLoad(new User(), Email.class);

		Set<User> users2 = net.findEntitySet(User.class, new Path("S-", User.class));
		Assert.assertEquals(0, users2.size());

		long start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			Set<User> users = net.findEntitySet(User.class, new Path("S+", User.class).setCacheable(false));
			Assert.assertTrue(users.size() > 0);
		}
		printTimeUsed(start, "Find self No Cache");

		start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			Set<User> users = net.findEntitySet(User.class, new Path("S+", User.class).setCacheable(true));
			Assert.assertTrue(users.size() > 0);
		}
		printTimeUsed(start, "Find self With Cache");
	}

	@Test
	public void testFindChild() {
		System.out.println("==============testFindChild================ ");
		int sampleSize = 30;
		int queyrTimes = 30;
		for (int i = 0; i < sampleSize; i++) {
			new User().put("id", "usr" + i).put("userName", "user" + i).insert();
			for (int j = 0; j < sampleSize; j++)
				new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
		}
		EntityNet net = ctx.netLoad(new User(), Email.class);

		Map<Class<?>, Set<Node>> result = null;
		long start = System.currentTimeMillis();

		start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			result = net.findNodeMapByEntities(new Path("S+", User.class).setCacheable(false)
					.nextPath("C+", Email.class, "userId").setCacheable(false));
		}
		printTimeUsed(start, "Find Childs no Cache");

		System.out.println("user selected2:" + result.get(User.class).size());
		System.out.println("email selected2:" + result.get(Email.class).size());
		net.setAllowQueryCache(true);
		start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			result = net.findNodeMapByEntities(new Path("S+", User.class).nextPath("C+", Email.class, "userId"));
		}
		printTimeUsed(start, "Find Childs with Cache");

		System.out.println("user selected2:" + result.get(User.class).size());
		System.out.println("email selected2:" + result.get(Email.class).size());
	}

	@Test
	public void testFindChild2() {//This unit test will put on user manual
		int sampleSize = 30;
		int queyrTimes = 30;
		for (int i = 0; i < sampleSize; i++) {
			new User().put("id", "usr" + i).put("userName", "user" + i).insert();
			for (int j = 0; j < sampleSize; j++)
				new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
		}
		EntityNet net = ctx.netLoad(new User(), Email.class);
		net.setAllowQueryCache(true);
		Map<Class<?>, Set<Node>> result = null;
		long start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			result = net.findNodeMapByEntities(new Path("S+", User.class).nextPath("C+", Email.class, "userId"));
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
	public void testValidateByBeanValidator() {// no cache
		System.out.println("==============testValidateByBeanValidator================ ");
		int sampleSize = 30;
		int queyrTimes = 60;
		for (int i = 0; i < sampleSize; i++) {
			new User().put("id", "usr" + i).put("userName", "user" + i).insert();
			for (int j = 0; j < sampleSize; j++)
				new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
		}
		EntityNet net = ctx.netLoad(User.class, Email.class);
		long start = System.currentTimeMillis();
		Set<Email> emails = null;
		// Set validator instance will not cache query result
		Path p = new Path("S-", User.class).nextPath("C+", Email.class, "userId").setValidator(new MyBeanValidator());
		for (int i = 0; i < queyrTimes; i++) {
			emails = net.findEntitySet(Email.class, p);
		}
		printTimeUsed(start, "Bean Validator instance (No Cache)");
		Assert.assertEquals(sampleSize * sampleSize, emails.size());

		// Set validator class will cache query result
		start = System.currentTimeMillis();
		p = new Path("S-", User.class).nextPath("C+", Email.class, "userId").setValidator(MyBeanValidator.class);
		for (int i = 0; i < queyrTimes; i++) {
			emails = net.findEntitySet(Email.class, p);
		}
		printTimeUsed(start, "Bean Validator instance (Has Cache)");
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
		EntityNet net = ctx.netLoad(User.class, Email.class);
		long start = System.currentTimeMillis();
		Set<Email> emails = null;
		// Set expression query parameters will not cache query result
		Path p = new Path("S-", User.class).where("age>?", 10).nextPath("C+", Email.class, "userId")
				.where("id startwith ? or emailName=?", "email1", "Bar");
		for (int i = 0; i < queyrTimes; i++) {
			emails = net.findEntitySet(Email.class, p);
		}
		printTimeUsed(start, "Validate by expression with parameters");
		Assert.assertEquals(180, emails.size());

		// Do not have query parameters will cause query result be cached
		start = System.currentTimeMillis();
		p = new Path("S-", User.class).where("age>10").nextPath("C+", Email.class, "userId")
				.where("id startwith 'email1' or emailName='Bar'");
		for (int i = 0; i < queyrTimes; i++) {
			emails = net.findEntitySet(Email.class, p);
		}
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
		EntityNet net = ctx.netLoad(new User(), Email.class);
		Map<Class<?>, Set<Node>> result = null;
		long start = System.currentTimeMillis();

		start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			result = net.findNodeMapByEntities(new Path("S+", Email.class).nextPath("P+", User.class, "userId"));
		}
		printTimeUsed(start, "Find parent (no cache)");
		System.out.println("user selected2:" + result.get(User.class).size());
		System.out.println("email selected2:" + result.get(Email.class).size());
	}

	@Test
	public void testAddEntity() {
		System.out.println("==============testAddEntity================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		EntityNet net = ctx.netLoad(User.class);
		Assert.assertEquals(1, net.size());

		User u2 = new User();
		u2.setId("u2");
		u2.setUserName("user2");
		u2.insert();
		ctx.netAddEntity(net, u2);

		Assert.assertEquals(2, net.size());

		User u = net.getOneEntity(User.class, "u2");
		Assert.assertEquals("user2", u.getUserName());
	}

	@Test
	public void testRemoveEntity() {
		System.out.println("==============testRemoveEntity================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		new User().put("id", "u2").put("userName", "user2").insert();
		EntityNet net = ctx.netLoad(User.class);
		Assert.assertEquals(2, net.size());
		User u2 = net.getOneEntity(User.class, "u2");
		ctx.netRemoveEntity(net, u2);
		Assert.assertEquals(1, net.size());
	}

	@Test
	public void testUpdateEntity() {
		System.out.println("==============testUpdateEntity================ ");
		new User().put("id", "u1").put("userName", "user1").insert();
		new User().put("id", "u2").put("userName", "user2").insert();
		EntityNet net = ctx.netLoad(User.class);
		Assert.assertEquals(2, net.size());
		User u2 = net.getOneEntity(User.class, "u2");
		u2.setUserName("newName");
		ctx.netUpdateEntity(net, u2);

		u2 = net.getOneEntity(User.class, "u2");
		Assert.assertEquals("newName", u2.getUserName());
	}
}