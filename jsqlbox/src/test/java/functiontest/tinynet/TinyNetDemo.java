package functiontest.tinynet;

import static com.github.drinkjava2.jsqlbox.SqlBoxContext.netConfig;
import static com.github.drinkjava2.jsqlbox.SqlBoxContext.netProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.ModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jtinynet.DefaultNodeValidator;
import com.github.drinkjava2.jtinynet.Path;
import com.github.drinkjava2.jtinynet.TinyNet;

import config.TestBase;
import functiontest.tinynet.entities.Address;
import functiontest.tinynet.entities.Email;
import functiontest.tinynet.entities.Privilege;
import functiontest.tinynet.entities.Role;
import functiontest.tinynet.entities.RolePrivilege;
import functiontest.tinynet.entities.User;
import functiontest.tinynet.entities.UserRole;

public class TinyNetDemo extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		TableModel[] models = ModelUtils.entity2Model(User.class, Email.class, Address.class, Role.class,
				Privilege.class, UserRole.class, RolePrivilege.class);
		dropAndCreateDatabase(models);
		ctx.refreshMetaData();
	}

	protected void insertDemoData() {
		//@formatter:off 
		ctx.nBatchBegin(); //Batch insert enabled 
		new User().put("id","u1").put("userName","user1").insert();
		new User().put("id","u2").put("userName","user2").insert();
		new User().put("id","u3").put("userName","user3").insert();
		new User().put("id","u4").put("userName","user4").insert();
		new User().put("id","u5").put("userName","user5").insert(); 
		
		new Address().put("id","a1","addressName","address1","userId","u1").insert();
		new Address().put("id","a2","addressName","address2","userId","u2").insert();
		new Address().put("id","a3","addressName","address3","userId","u3").insert();
		new Address().put("id","a4","addressName","address4","userId","u4").insert();
		new Address().put("id","a5","addressName","address5","userId","u5").insert();

		new Email().putFields("id","emailName","userId");
		new Email().putValues("e1","email1","u1").insert();
		new Email().putValues("e2","email2","u1").insert();
		new Email().putValues("e3","email3","u2").insert();
		new Email().putValues("e4","email4","u2").insert();
		new Email().putValues("e5","email5","u3").insert();
		
		Role r=new Role();
		r.setId("r1");r.setRoleName("role1");r.insert();
		r.setId("r2");r.setRoleName("role2");r.insert();
		r.setId("r3");r.setRoleName("role3");r.insert();
		r.setId("r4");r.setRoleName("role4");r.insert();
		r.setId("r5");r.setRoleName("role5");r.insert();
		
		Privilege p=new Privilege();
		p.setId("p1");p.setPrivilegeName("privilege1");p.insert();
		p.setId("p2");p.setPrivilegeName("privilege2");p.insert();
		p.setId("p3");p.setPrivilegeName("privilege3");p.insert();
		p.setId("p4");p.setPrivilegeName("privilege4");p.insert();
		p.setId("p5");p.setPrivilegeName("privilege5");p.insert();

        UserRole ur=new UserRole();
        ur.setUserId("u1");ur.setRid("r1");ur.insert();
        ur.setUserId("u2");ur.setRid("r1");ur.insert();
        ur.setUserId("u2");ur.setRid("r2");ur.insert();
        ur.setUserId("u2");ur.setRid("r3");ur.insert();
        ur.setUserId("u3");ur.setRid("r4");ur.insert();
        ur.setUserId("u4");ur.setRid("r1");ur.insert();        
         
		new RolePrivilege().putFields("rid","pid"); 
		new RolePrivilege().putValues("r1","p1").insert();
		new RolePrivilege().putValues("r2","p1").insert();		
		new RolePrivilege().putValues("r2","p2").insert();		 
		new RolePrivilege().putValues("r2","p3").insert();	
		new RolePrivilege().putValues("r3","p3").insert();
		new RolePrivilege().putValues("r4","p1").insert();
		ctx.nBatchEnd(); //Batch insert end
		//@formatter:on
	}

	@Test
	public void testPathFind() {
		insertDemoData();
		TinyNet net = ctx.netLoad(new User(), new Role(), Privilege.class, UserRole.class, RolePrivilege.class);
		Set<Privilege> privileges = net.findEntitySet(Privilege.class,
				new Path("S-", User.class).where("id='u1' or id='u2'").nextPath("C-", UserRole.class, "userId")
						.nextPath("P-", Role.class, "rid").nextPath("C-", RolePrivilege.class, "rid")
						.nextPath("P+", Privilege.class, "pid"));
		for (Privilege privilege : privileges)
			System.out.print(privilege.getId()+" ");
		Assert.assertEquals(3, privileges.size());
	}

	@Test
	public void testAutoPath() {
		insertDemoData();
		TinyNet net = ctx.netLoad(new User(), new Role(), Privilege.class, UserRole.class, RolePrivilege.class);
		Set<Privilege> privileges = net.findEntitySet(Privilege.class,
				new Path(User.class).where("id='u1' or id='u2'").autoPath(Privilege.class));
		for (Privilege privilege : privileges)
			System.out.print(privilege.getId()+" ");
		Assert.assertEquals(3, privileges.size());
	}

	@Test
	public void testAutoPath2() {
		insertDemoData();
		TinyNet net = ctx.netLoad(new Email(), new User(), new Role(), Privilege.class, UserRole.class,
				RolePrivilege.class);
		Set<Privilege> privileges = net.findEntitySet(Privilege.class,
				new Path(Email.class).setValidator(new EmailValidator()).autoPath(Privilege.class));
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
		TinyNet net = ctx.netLoad(new User(), Email.class, Address.class, new Role(), Privilege.class, UserRole.class,
				RolePrivilege.class);
		Assert.assertEquals(37, net.size());
		System.out.println(net.size());

		List<User> users = net.getAllEntityList(User.class);
		Assert.assertEquals(5, users.size());
		Assert.assertEquals("user1", users.get(0).getUserName());
		System.out.println(users.get(0).getUserName());
	}

	@Test
	public void testNetLoadSketch() {
		insertDemoData();
		TinyNet net = ctx.netLoadSketch(new User(), Email.class, Address.class, new Role(), Privilege.class,
				UserRole.class, RolePrivilege.class);
		Assert.assertEquals(37, net.size());
		System.out.println(net.size());

		List<User> users = net.getAllEntityList(User.class);
		Assert.assertEquals(5, users.size());
		Assert.assertEquals(null, users.get(0).getUserName());
	}

	@Test
	public void testManualLoadAndJoin() {
		insertDemoData();
		List<Map<String, Object>> mapList1 = ctx.nQuery(new MapListHandler(netProcessor(User.class, Address.class)),
				"select u.**, a.** from usertb u, addresstb a where a.userId=u.id");
		TinyNet net = ctx.netCreate(mapList1);
		Assert.assertEquals(10, net.size());

		List<Map<String, Object>> mapList2 = ctx.nQuery(new MapListHandler(),
				netConfig(Email.class) + "select e.** from emailtb as e");
		ctx.netJoinList(net, mapList2);
		Assert.assertEquals(15, net.size());

		List<Map<String, Object>> mapList3 = ctx.nQuery(
				new MapListHandler(netProcessor(Role.class, UserRole.class, RolePrivilege.class, Privilege.class)),
				"select r.**, ur.**, rp.**, p.** from roletb r, userroletb ur, RolePrivilegetb rp, privilegetb p");
		Assert.assertEquals(900, mapList3.size());
		ctx.netJoinList(net, mapList3);
		Assert.assertEquals(37, net.size());
	}

}