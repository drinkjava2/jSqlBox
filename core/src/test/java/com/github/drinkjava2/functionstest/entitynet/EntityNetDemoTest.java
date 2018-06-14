package com.github.drinkjava2.functionstest.entitynet;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.gpQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
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
import com.github.drinkjava2.jsqlbox.entitynet.Path;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
import com.github.drinkjava2.jsqlbox.handler.MapListWrap;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;
import com.github.drinkjava2.jsqlbox.handler.SSMapListWrapHandler;

public class EntityNetDemoTest extends TestBase {
	{
		regTables(User.class, Email.class, Address.class, Role.class, Privilege.class, UserRole.class,
				RolePrivilege.class);
	}

	protected void insertDemoData() {
		// @formatter:off
		try {
			ctx.nBatchBegin(); // Batch insert enabled
			new User().put("id", "u1").put("userName", "user1").insert();
			new User().put("id", "u2").put("userName", "user2").insert();
			new User().put("id", "u3").put("userName", "user3").insert();
			new User().put("id", "u4").put("userName", "user4").insert();
			new User().put("id", "u5").put("userName", "user5").insert();

			new Address().put("id", "a1", "addressName", "address1", "userId", "u1").insert();
			new Address().put("id", "a2", "addressName", "address2", "userId", "u2").insert();
			new Address().put("id", "a3", "addressName", "address3", "userId", "u3").insert();
			new Address().put("id", "a4", "addressName", "address4", "userId", "u4").insert();
			new Address().put("id", "a5", "addressName", "address5", "userId", "u5").insert();

			new Email().putFields("id", "emailName", "userId");
			new Email().putValues("e1", "email1", "u1").insert();
			new Email().putValues("e2", "email2", "u1").insert();
			new Email().putValues("e3", "email3", "u2").insert();
			new Email().putValues("e4", "email4", "u2").insert();
			new Email().putValues("e5", "email5", "u3").insert();

			new Role().putFields("id", "roleName");
			new Role().putValues("r1", "role1").insert();
			new Role().putValues("r2", "role2").insert();
			new Role().putValues("r3", "role3").insert();
			new Role().putValues("r4", "role4").insert();
			new Role().putValues("r5", "role5").insert();

			new Privilege().putFields("id", "privilegeName");
			new Privilege().putValues("p1", "privilege1").insert();
			new Privilege().putValues("p2", "privilege2").insert();
			new Privilege().putValues("p3", "privilege3").insert();
			new Privilege().putValues("p4", "privilege4").insert();
			new Privilege().putValues("p5", "privilege5").insert();

			new UserRole().putFields("userId", "rid");
			new UserRole().putValues("u1", "r1").insert();
			new UserRole().putValues("u2", "r1").insert();
			new UserRole().putValues("u2", "r2").insert();
			new UserRole().putValues("u2", "r3").insert();
			new UserRole().putValues("u3", "r4").insert();
			new UserRole().putValues("u4", "r1").insert();

			new RolePrivilege().putFields("rid", "pid");
			new RolePrivilege().putValues("r1", "p1").insert();
			new RolePrivilege().putValues("r2", "p1").insert();
			new RolePrivilege().putValues("r2", "p2").insert();
			new RolePrivilege().putValues("r2", "p3").insert();
			new RolePrivilege().putValues("r3", "p3").insert();
			new RolePrivilege().putValues("r4", "p1").insert();
		} finally {
			ctx.nBatchEnd(); // Batch insert end
		}
		// @formatter:on
	}

	/**
	 * Test User class with Transient Annotated fields Address and Role
	 */
	@Test
	public void testEntityCrudWithTransientAnnotation() {
		// System.out.println(DebugUtils.getTableModelDebugInfo(TableModelUtils.entity2Model(User.class)));

		new User().put("id", "u1").put("userName", "user1").insert();
		Assert.assertEquals(1, ctx.nQueryForLongValue("select count(*) from usertb"));

		User u = new User().loadById("u1");
		Assert.assertEquals("user1", u.getUserName());

		u.setUserName("user2");
		u.update();
		Assert.assertEquals("user2", ((User) (new User().loadById("u1"))).getUserName());

		u.delete();
		Assert.assertEquals(0, ctx.nQueryForLongValue("select count(*) from usertb"));
	}

	@Test
	public void testAutoPath() {
		insertDemoData();
		EntityNet net = ctx.netLoadAll(new User(), new Role(), Privilege.class, UserRole.class, RolePrivilege.class);
		Set<Privilege> privileges = net
				.runPath(new Path(User.class).where("id='u1' or id='u2'").autoPath(Privilege.class))
				.pickEntitySet(Privilege.class);
		;
		for (Privilege privilege : privileges)
			System.out.print(privilege.getId() + " ");
		Assert.assertEquals(3, privileges.size());
	}

	@Test
	public void testPathFind() {
		insertDemoData();
		EntityNet net = ctx.netLoadAll(new User(), new Role(), Privilege.class, UserRole.class, RolePrivilege.class);
		Set<Privilege> privileges = net.runPath(new Path("S-", User.class).where("id='u1' or id='u2'")
				.nextPath("C-", UserRole.class, "userId").nextPath("P-", Role.class, "rid")
				.nextPath("C-", RolePrivilege.class, "rid").nextPath("P+", Privilege.class, "pid"))
				.pickEntitySet(Privilege.class);
		for (Privilege privilege : privileges)
			System.out.print(privilege.getId() + " ");
		Assert.assertEquals(3, privileges.size());
	}

	@Test
	public void testAutoPathAndBind() {
		insertDemoData();
		EntityNet net = ctx.netLoadAll(new User(), new Role(), Privilege.class, UserRole.class, RolePrivilege.class);
		Set<Privilege> privileges = net
				.runPath(new Path(User.class).where("id='u1' or id='u2'").autoPath(Privilege.class))
				.pickEntitySet(Privilege.class);
		for (Privilege privilege : privileges)
			System.out.print(privilege.getId() + " ");
		Assert.assertEquals(3, privileges.size());
	}

	@Test
	public void testReturnDifferentType() {
		insertDemoData();
		EntityNet net = ctx.netLoadAll(new User(), new Email());
		Map<Class<?>, Set<Object>> result = net
				.runPath(new Path("S+", Email.class).nextPath("P+", User.class, "userId")).pickEntitySetMap();
		Assert.assertEquals(3, result.get(User.class).size());
		Assert.assertEquals(5, result.get(Email.class).size());
	}

	@Test
	public void testAutoPath2() {
		insertDemoData();
		EntityNet net = ctx.netLoadAll(new Email(), new User(), new Role(), Privilege.class, UserRole.class,
				RolePrivilege.class);
		Set<Privilege> privileges = net
				.runPath(new Path(Email.class).setValidator(new EmailValidator()).autoPath(Privilege.class))
				.pickEntitySet(Privilege.class);
		;
		for (Privilege privilege : privileges)
			System.out.println(privilege.getId());
		Assert.assertEquals(1, privileges.size());
	}

	public static class EmailValidator extends DefaultNodeValidator {
		@Override
		public boolean validateBean(Object entity) {
			Email e = (Email) entity;
			return ("e1".equals(e.getId()) || "e5".equals(e.getId())) ? true : false;
		}
	}

	@Test
	public void testloadEntityNet() {
		insertDemoData();
		EntityNet net = ctx.netLoadAll(new User(), Email.class, Address.class, new Role(), Privilege.class,
				UserRole.class, RolePrivilege.class);
		Assert.assertEquals(37, net.size());
		System.out.println(net.size());

		List<User> users = net.pickEntityList(User.class);
		Assert.assertEquals(5, users.size());
		Assert.assertEquals("user1", users.get(0).getUserName());
		System.out.println(users.get(0).getUserName());
	}

	@Test
	public void testNetLoadSketch() {
		insertDemoData();
		EntityNet net = ctx.netLoadSketch(new User(), Email.class, Address.class, new Role(), Privilege.class,
				UserRole.class, RolePrivilege.class);
		Assert.assertEquals(37, net.size());
		System.out.println(net.size());

		List<User> users = net.pickEntityList(User.class);
		Assert.assertEquals(5, users.size());
		Assert.assertEquals(null, users.get(0).getUserName());
	}

	@Test
	public void testEntityNetQuery() {
		insertDemoData();
		EntityNet net = gpQuery(new EntityNetHandler(User.class, Email.class),
				"select u.**, e.** from usertb u, emailtb e where u.id=e.userId");
		Assert.assertEquals(8, net.size());
		EntityNet newNet = net.runPath(new Path(User.class).where("id='u1' or id='u2'").autoPath(Email.class));
		Set<Email> emails = newNet.pickEntitySet(Email.class);
		Assert.assertEquals(4, emails.size());
	}

	@Test
	public void testManualLoadAndJoin() {
		insertDemoData();
		EntityNet net = gpQuery(new EntityNetHandler(User.class, Address.class),
				"select u.**, a.** from usertb u, addresstb a where a.userId=u.id");
		Assert.assertEquals(10, net.size());

		List<Map<String, Object>> mapList2 = gpQuery(new SSMapListHandler(Email.class),
				"select e.id as e_id from emailtb as e");
		net.add(mapList2, new Email().alias("e"));
		Assert.assertEquals(15, net.size());

		MapListWrap wrap = gpQuery(
				new SSMapListWrapHandler(Role.class, UserRole.class, RolePrivilege.class, Privilege.class),
				"select r.**, ur.**, rp.**, p.** from roletb r, userroletb ur, RolePrivilegetb rp, privilegetb p");
		Assert.assertEquals(900, wrap.getMapList().size());
		net.add(wrap);
		Assert.assertEquals(37, net.size());
	}

}