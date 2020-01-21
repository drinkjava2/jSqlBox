package com.github.drinkjava2.jsqlbox.function.entitynet;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.DB.AUTO_SQL;
import static com.github.drinkjava2.jsqlbox.DB.alias;
import static com.github.drinkjava2.jsqlbox.DB.give;
import static com.github.drinkjava2.jsqlbox.DB.giveBoth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.Address;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.Email;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.Privilege;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.Role;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.RolePrivilege;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.User;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.UserRole;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

public class EntityNetTest extends TestBase {
	{
		regTables(User.class, Email.class, Address.class, Role.class, Privilege.class, UserRole.class,
				RolePrivilege.class);
	}

	protected void insertDemoData() {
		new User().putField("id", "u1").putField("userName", "user1").insert();
		new User().putField("id", "u2").putField("userName", "user2").insert();
		new User().putField("id", "u3").putField("userName", "user3").insert();
		new User().putField("id", "u4").putField("userName", "user4").insert();
		new User().putField("id", "u5").putField("userName", "user5").insert();

		new Address().putField("id", "a1", "addressName", "address1", "userId", "u1").insert();
		new Address().putField("id", "a2", "addressName", "address2", "userId", "u2").insert();
		new Address().putField("id", "a3", "addressName", "address3", "userId", "u3").insert();
		new Address().putField("id", "a4", "addressName", "address4", "userId", "u4").insert();
		new Address().putField("id", "a5", "addressName", "address5", "userId", "u5").insert();

		new Email().forFields("id", "emailName", "userId");
		new Email().putValues("e1", "email1", "u1").insert();
		new Email().putValues("e2", "email2", "u1").insert();
		new Email().putValues("e3", "email3", "u2").insert();
		new Email().putValues("e4", "email4", "u2").insert();
		new Email().putValues("e5", "email5", "u3").insert();

		new Role().forFields("id", "roleName");
		new Role().putValues("r1", "role1").insert();
		new Role().putValues("r2", "role2").insert();
		new Role().putValues("r3", "role3").insert();
		new Role().putValues("r4", "role4").insert();
		new Role().putValues("r5", "role5").insert();

		new Privilege().forFields("id", "privilegeName");
		new Privilege().putValues("p1", "privilege1").insert();
		new Privilege().putValues("p2", "privilege2").insert();
		new Privilege().putValues("p3", "privilege3").insert();
		new Privilege().putValues("p4", "privilege4").insert();
		new Privilege().putValues("p5", "privilege5").insert();

		new UserRole().forFields("userId", "rid");
		new UserRole().putValues("u1", "r1").insert();
		new UserRole().putValues("u2", "r1").insert();
		new UserRole().putValues("u2", "r2").insert();
		new UserRole().putValues("u2", "r3").insert();
		new UserRole().putValues("u3", "r4").insert();
		new UserRole().putValues("u4", "r1").insert();

		new RolePrivilege().forFields("rid", "pid");
		new RolePrivilege().putValues("r1", "p1").insert();
		new RolePrivilege().putValues("r2", "p1").insert();
		new RolePrivilege().putValues("r2", "p2").insert();
		new RolePrivilege().putValues("r2", "p3").insert();
		new RolePrivilege().putValues("r3", "p3").insert();
		new RolePrivilege().putValues("r4", "p1").insert();
	}

	private static final Object[] targets = new Object[] { new EntityNetHandler(), User.class, UserRole.class,
			Role.class, RolePrivilege.class, Privilege.class, giveBoth("r", "u"), giveBoth("p", "u") };

	@Test
	public void testAutoAlias() {
		insertDemoData();
		EntityNet net = ctx.iQuery(targets, "select u.**, ur.**, r.**, p.**, rp.** from usertb u ", //
				" left join userroletb ur on u.id=ur.userid ", //
				" left join roletb r on ur.rid=r.id ", //
				" left join roleprivilegetb rp on rp.rid=r.id ", //
				" left join privilegetb p on p.id=rp.pid ", //
				" order by u.id, ur.id, r.id, rp.id, p.id");
		List<User> userList = net.pickEntityList("u");
		for (User u : userList) {
			Systemout.println("User:" + u.getId());

			List<Role> roles = u.getRoleList();
			if (roles != null)
				for (Role r : roles)
					Systemout.println("  Roles:" + r.getId());

			Map<Integer, Role> roleMap = u.getRoleMap();
			if (roleMap != null)
				for (Role r : roleMap.values())
					Systemout.println("  Roles:" + r.getId());

			Set<Privilege> privileges = u.getPrivilegeSet();
			if (privileges != null)
				for (Privilege privilege : privileges)
					Systemout.println("  Privilege:" + privilege.getId());
		}

		Systemout.println("===========pick entity set================");
		Set<Privilege> privileges = net.pickEntitySet(Privilege.class);
		for (Privilege p : privileges) {
			Systemout.println("Privilege:" + p.getId());
			Systemout.println("  User:" + p.getUser().getId());
		}

		Systemout.println("===========pick entity Map================");
		Map<Object, Role> roleMap = net.pickEntityMap(Role.class);
		for (Entry<Object, Role> entry : roleMap.entrySet()) {
			Systemout.println("RoleId:" + entry.getKey());
			Role r = entry.getValue();
			Systemout.println("Role:" + r.getId());
			Systemout.println("  user=" + r.getUser().getId());
		}

		Systemout.println("===========pick one entity by value================");
		Role r3 = net.pickOneEntity(Role.class, "r3");
		Assert.assertEquals("r3", r3.getId());

		Systemout.println("===========pick one entity by bean ================");
		Role temp = new Role();
		temp.putField("id", "r4");
		Role r4 = net.pickOneEntity(Role.class, temp);
		Assert.assertEquals("r4", r4.getId());

		Systemout.println("===========pick one entity by map ================");
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("id", "r2");
		Role r2 = net.pickOneEntity(Role.class, mp);
		Assert.assertEquals("r2", r2.getId());
	}

	@Test
	public void testLeftJoinSQL() {
		insertDemoData();
		EntityNet net = ctx.iQuery(targets, AUTO_SQL);
		List<User> userList = net.pickEntityList("u");
		for (User u : userList) {
			Systemout.println("User:" + u.getId());
			Set<Privilege> privileges = u.getPrivilegeSet();
			if (privileges != null)
				for (Privilege privilege : privileges)
					Systemout.println("  Privilege:" + privilege.getId());
		}
	}

	@Test
	public void testJoinQuary() {
		insertDemoData();
		EntityNet net = ctx.iQuery(targets, AUTO_SQL);
		ctx.iQuery(net, User.class, Email.class, give("e", "u"), AUTO_SQL);
		ctx.iQuery(net, User.class, Address.class, giveBoth("a", "u"), AUTO_SQL);
		User u = net.pickOneEntity(User.class, "u2");
		Systemout.println(u);

		Systemout.println("User:" + u.getId());
		List<Role> roles = u.getRoleList();
		if (roles != null)
			for (Role r : roles)
				Systemout.println("  Roles:" + r.getId());

		Set<Privilege> privileges = u.getPrivilegeSet();
		if (privileges != null)
			for (Privilege privilege : privileges)
				Systemout.println("  Privilege:" + privilege.getId());

		if (u.getEmailList() != null)
			for (Email e : u.getEmailList())
				Systemout.println("  Email:" + e.getId());

		if (u.getAddress() != null) {
			Systemout.println("  Address:" + u.getAddress().getId());
			Assert.assertTrue(u == u.getAddress().getUser());
		}

	}

	@Test
	public void testGiveAlias() {// Assign alias name "t" to User, "tr" to UserRole, "r" to Role
		insertDemoData();
		EntityNet net = ctx.iQuery(new EntityNet(), User.class, UserRole.class, Role.class, alias("t", "tr", "r"),
				RolePrivilege.class, Privilege.class, giveBoth("r", "t"), giveBoth("p", "t"), AUTO_SQL, //
				" order by t.id, tr.id, r.id, rp.id, p.id");
		List<User> userList = net.pickEntityList(User.class);
		for (User u : userList) {
			Systemout.println("User:" + u.getId());
			List<Role> roles = u.getRoleList();
			if (roles != null)
				for (Role r : roles)
					Systemout.println("  Roles:" + r.getId());

			Set<Privilege> privileges = u.getPrivilegeSet();
			if (privileges != null)
				for (Privilege privilege : privileges)
					Systemout.println("  Privilege:" + privilege.getId());
		}
	}

	@Test
	public void testManualLoad() {
		insertDemoData();
		List<User> users = ctx.eFindAll(User.class);

		for (User u : users) {
			Systemout.println("User:" + u.getId());

			Address addr = u.findRelatedOne(Address.class, " or u.id like ?", param("abcd%"));
			Systemout.println("  Address:" + addr.getId());

			List<UserRole> userRoles = u.findRelatedList(UserRole.class);
			if (userRoles != null)
				for (UserRole ur : userRoles) {
					Systemout.println("  UserRole:" + ur.getUserId() + "," + ur.getRid());
				}

			List<Role> roles = u.findRelatedList(UserRole.class, Role.class);
			if (roles != null)
				for (Role r : roles) {
					Systemout.println("  Roles:" + r.getId());
				}

			Object path = new Object[] { UserRole.class, Role.class, RolePrivilege.class, Privilege.class };
			Set<Privilege> privileges = u.findRelatedSet(path);
			if (privileges != null)
				for (Privilege privilege : privileges) {
					Systemout.println("  Privilege:" + privilege.getId());
				}

			Map<Object, Privilege> privilegeMap = u.findRelatedMap(path);
			if (privilegeMap != null)
				for (Entry<Object, Privilege> entry : privilegeMap.entrySet()) {
					Systemout.println("  PrivilegeMap " + entry.getKey() + "=" + entry.getValue().getId());
				}
		}
	}

	@Test
	public void testNoSqlQuery() {
		insertDemoData();
		EntityNet net = ctx.iQuery(new EntityNet(), User.class, AUTO_SQL);
		ctx.iQuery(net, UserRole.class, AUTO_SQL);
		ctx.iQuery(net, Role.class, AUTO_SQL);
		ctx.iQuery(net, RolePrivilege.class, AUTO_SQL);
		ctx.iQuery(net, Privilege.class, AUTO_SQL);
		ctx.iQuery(net, Address.class, AUTO_SQL);
		ctx.iQuery(net, Email.class, AUTO_SQL);
		User u = net.pickOneEntity(User.class, "u2");
		Systemout.println("User:" + u.getId());

		Address addr = u.findRelatedOne(net, Address.class);
		Systemout.println("  Address:" + addr.getId());

		List<Email> emails = u.findRelatedList(net, Email.class);
		if (emails != null)
			for (Email e : emails)
				Systemout.println("  Email:" + e.getId());

		Set<Role> roles = u.findRelatedSet(net, UserRole.class, Role.class);
		if (roles != null)
			for (Role r : roles)
				Systemout.println("  Roles:" + r.getId());

		Object path = new Object[] { UserRole.class, Role.class, RolePrivilege.class, Privilege.class };
		List<Privilege> privileges = u.findRelatedList(net, path);
		if (privileges != null)
			for (Privilege privilege : privileges)
				Systemout.println("  Privilege:" + privilege.getId());
	}

	@Test
	public void testAutoEntityNet() {
		insertDemoData();
		List<User> users = ctx.autoNet(User.class, UserRole.class, Role.class, RolePrivilege.class, Privilege.class)
				.pickEntityList(User.class);

		for (User u : users) {
			Systemout.println("User:" + u.getId());

			List<Role> roles = u.getRoleList();
			if (roles != null)
				for (Role r : roles)
					Systemout.println("  Roles:" + r.getId());

			Set<Privilege> privileges = u.getPrivilegeSet();
			if (privileges != null)
				for (Privilege privilege : privileges)
					Systemout.println("  Privilege:" + privilege.getId());
		}
	}

	/**
	 * Test u.##, it means only load P-Key and F-Key columns, other fields will not
	 * load (is null), 一旦加载了某个属性，就不会再变回null了。
	 */
	@Test
	public void testSharpSharp() {
		insertDemoData();
		EntityNet net = ctx.iQuery(new EntityNet(), User.class, Address.class, giveBoth("u", "a"),
				"select u.##, a.** from usertb u left join addresstb a on u.id=a.userId");
		List<User> userList = net.pickEntityList("u");
		Assert.assertTrue(null == userList.get(0).getUserName());// userName is null!

		ctx.iQuery(net, User.class, Email.class, giveBoth("u", "e"), AUTO_SQL);
		userList = net.pickEntityList("u");// userName is not null!

		ctx.iQuery(net, User.class, UserRole.class, giveBoth("u", "ur"),
				"select u.##, ur.** from usertb u left join userroletb ur on u.id=ur.userId");
		userList = net.pickEntityList("u");// userName is still not null!
		Assert.assertTrue(null != userList.get(0).getUserName());

		for (User u : userList) {
			Systemout.println("User:" + u.getUserName());
			Systemout.println("  Addr:" + u.getAddress().getAddressName());
			if (u.getEmailList() != null)
				for (Email e : u.getEmailList())
					Systemout.println("  Email:" + e.getId());
		}
	}

	@Test
	public void testTail() {
		insertDemoData();
		EntityNet net = ctx.iQuery(new EntityNetHandler(), User.class, UserRole.class, Role.class, giveBoth("r", "u"), //
				"select u.**, u.username as u_name2, ur.**, r.**, r.id as r_id2 from usertb u ", //
				" left join userroletb ur on u.id=ur.userid ", //
				" left join roletb r on ur.rid=r.id ");
		Set<User> userList = net.pickEntitySet("u");
		for (User u : userList) {
			Systemout.print("User name:" + u.getId());
			Systemout.println(", name2:" + u.getTail("name2"));
			Assert.assertEquals(u.getUserName(), (String) u.getTail("name2"));
			List<Role> roles = u.getRoleList();
			if (roles != null)
				for (Role r : roles)
					Systemout.println("  Role id:" + r.getId() + ", id2:" + r.getTail("id2"));
		}
	}

}