package com.github.drinkjava2.functionstest.entitynet;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.give;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giveBoth;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.modelAutoAlias;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

public class EntityNetTest extends TestBase {
	{
		regTables(User.class, Email.class, Address.class, Role.class, Privilege.class, UserRole.class,
				RolePrivilege.class);
	}

	protected void insertDemoData() {
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
	}

	@Test
	public void testAutoAlias() {
		insertDemoData();
		EntityNet net = ctx.iQuery(new EntityNetHandler(),
				modelAutoAlias(new User(), Role.class, Privilege.class, UserRole.class, RolePrivilege.class),
				give("r", "u"), give("u", "r"), giveBoth("p", "u"),
				"select u.**, ur.**, r.**, p.**, rp.** from usertb u ", //
				" left join userroletb ur on u.id=ur.userid ", //
				" left join roletb r on ur.rid=r.id ", //
				" left join roleprivilegetb rp on rp.rid=r.id ", //
				" left join privilegetb p on p.id=rp.pid ", //
				" order by u.id, ur.id, r.id, rp.id, p.id");
		System.out.println("===========pick entity list================");
		List<User> userList = net.pickEntityList("u");
		for (User u : userList) {
			System.out.println("User:" + u.getId());

			List<Role> roles = u.getRoleList();
			if (roles != null)
				for (Role r : roles) {
					System.out.println("  Roles List:" + r.getId());
				}

			Map<Integer, Role> roleMap = u.getRoleMap();
			if (roleMap != null)
				for (Role r : roleMap.values()) {
					System.out.println("  Roles Map:" + r.getId());
				}

			Set<Privilege> privileges = u.getPrivilegeSet();
			if (privileges != null)
				for (Privilege privilege : privileges) {
					System.out.println("  Privilege:" + privilege.getId());
				}
		}

		System.out.println("===========pick entity set================");
		Set<Privilege> privileges = net.pickEntitySet("p");
		for (Privilege p : privileges) {
			System.out.println("Privilege:" + p.getId());
			System.out.println("  User:" + p.getUser().getId());
		}

		System.out.println("===========pick entity Map================");
		Map<Object, Object> roleMap = net.pickEntityMap("r");
		for (Entry<Object, Object> entry : roleMap.entrySet()) {
			System.out.println("RoleId:" + entry.getKey());
			Role r = (Role) entry.getValue();
			System.out.println("Role:" + r.getId());
			System.out.println("  user=" + r.getUser().getId());
		}

		System.out.println("===========pick one entity by value================");
		Role r3 = net.pickOneEntity("r", "r3");
		Assert.assertEquals("r3", r3.getId());

		System.out.println("===========pick one entity by bean ================");
		Role temp = new Role();
		temp.put("id", "r4");
		Role r4 = net.pickOneEntity("r", temp);
		Assert.assertEquals("r4", r4.getId());

		System.out.println("===========pick one entity by map ================");
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("id", "r2");
		Role r2 = net.pickOneEntity("r", mp);
		Assert.assertEquals("r2", r2.getId());
	}

	@Test
	public void testManualLoad() {
		insertDemoData();
		TableModel model = new User().tableModel();
		model.column("userName").setTransientable(true);
		User u2 = ctx.loadById(User.class, "u1", model);
		System.out.println(u2.getUserName());

		List<User> userList = ctx.loadAll(User.class);

		System.out.println(userList.size());
		for (User u : userList) {
			System.out.println("User:" + u.getId());

			// ctx.fillField(u, Role.class);
			List<Role> roles = u.getRoleList();
			if (roles != null)
				for (Role r : roles) {
					System.out.println("  Roles:" + r.getId());
				}

			Set<Privilege> privileges = u.getPrivilegeSet();
			if (privileges != null)
				for (Privilege privilege : privileges) {
					System.out.println("  Privilege:" + privilege.getId());
				}
		}
	}
}