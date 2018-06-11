package com.github.drinkjava2.functionstest.entitynet;

import java.util.HashMap;
import java.util.Map;

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
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
import com.github.drinkjava2.jsqlbox.handler.MapListWrap;

public class EntityNetBindTest extends TestBase {
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

		Role r = new Role();
		r.setId("r1");
		r.setRoleName("role1");
		r.insert();
		r.setId("r2");
		r.setRoleName("role2");
		r.insert();
		r.setId("r3");
		r.setRoleName("role3");
		r.insert();
		r.setId("r4");
		r.setRoleName("role4");
		r.insert();
		r.setId("r5");
		r.setRoleName("role5");
		r.insert();

		Privilege p = new Privilege();
		p.setId("p1");
		p.setPrivilegeName("privilege1");
		p.insert();
		p.setId("p2");
		p.setPrivilegeName("privilege2");
		p.insert();
		p.setId("p3");
		p.setPrivilegeName("privilege3");
		p.insert();
		p.setId("p4");
		p.setPrivilegeName("privilege4");
		p.insert();
		p.setId("p5");
		p.setPrivilegeName("privilege5");
		p.insert();

		UserRole ur = new UserRole();
		ur.setUserId("u1");
		ur.setRid("r1");
		ur.insert();
		ur.setUserId("u2");
		ur.setRid("r1");
		ur.insert();
		ur.setUserId("u2");
		ur.setRid("r2");
		ur.insert();
		ur.setUserId("u2");
		ur.setRid("r3");
		ur.insert();
		ur.setUserId("u3");
		ur.setRid("r4");
		ur.insert();
		ur.setUserId("u4");
		ur.setRid("r1");
		ur.insert();

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
		EntityNet net= ctx.iQuery(
				new EntityNetHandler(new User(), Role.class, Privilege.class, UserRole.class, RolePrivilege.class), //
				"select  u.**, ur.**, r.**, p.**, rp.** from usertb u ", //
				" left join userroletb ur on u.id=ur.userid ", //
				" left join roletb r on ur.rid=r.id ", //
				" left join roleprivilegetb rp on rp.rid=r.id ", //
				" left join privilegetb p on p.id=rp.pid "); 
		System.out.println(net.size());
		Assert.assertEquals(24, net.size());
		
	}
	 

}