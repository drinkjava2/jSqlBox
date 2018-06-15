package com.github.drinkjava2.functionstest.entitynet;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.functionstest.entitynet.entities.Address;
import com.github.drinkjava2.functionstest.entitynet.entities.Email;
import com.github.drinkjava2.functionstest.entitynet.entities.Privilege;
import com.github.drinkjava2.functionstest.entitynet.entities.Role;
import com.github.drinkjava2.functionstest.entitynet.entities.RolePrivilege;
import com.github.drinkjava2.functionstest.entitynet.entities.User;
import com.github.drinkjava2.functionstest.entitynet.entities.UserRole;
import com.github.drinkjava2.jsqlbox.entitynet.NewNet;

public class NewNetTest extends TestBase {
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
	public void testAutoPath() {
		insertDemoData();
		NewNet net = new NewNet(ctx)
				.config(new User(), Role.class, Privilege.class, UserRole.class, RolePrivilege.class)
				.give("r", "u", "roleList").giveBoth("p", "u")
				.query("select u.**, ur.**, r.**, p.**, rp.** from usertb u ", //
						" left join userroletb ur on u.id=ur.userid ", //
						" left join roletb r on ur.rid=r.id ", //
						" left join roleprivilegetb rp on rp.rid=r.id ", //
						" left join privilegetb p on p.id=rp.pid ", //
						" order by u.id, ur.id, r.id, rp.id, p.id");
		System.out.println(net.getDebugInfo());

		List<User> userList = net.pickAsEntityList("u");
		for (User u : userList) {
			System.out.println("User:" + u.getId());

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

		System.out.println("===========================");
		Set<Privilege> privileges = net.pickAsEntitySet("p");
		for (Privilege p : privileges) {
			System.out.println("Privilege:" + p.getId());
			User u = p.getUser();
			if (u != null)
				System.out.println("  User:" + p.getUser().getId());
		}

	}

}