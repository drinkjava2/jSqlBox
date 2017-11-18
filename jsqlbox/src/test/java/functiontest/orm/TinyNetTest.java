package functiontest.orm;

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
import com.github.drinkjava2.jtinynet.Node;
import com.github.drinkjava2.jtinynet.Path;
import com.github.drinkjava2.jtinynet.TinyNet;

import config.TestBase;
import functiontest.orm.entities.Address;
import functiontest.orm.entities.Email;
import functiontest.orm.entities.Privilege;
import functiontest.orm.entities.Role;
import functiontest.orm.entities.RolePrivilege;
import functiontest.orm.entities.User;
import functiontest.orm.entities.UserRole;

public class TinyNetTest extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		TableModel[] models = ModelUtils.entity2Model(User.class, Email.class, Address.class, Role.class,
				Privilege.class, UserRole.class, RolePrivilege.class);
		dropAndCreateDatabase(models);
		ctx.refreshMetaData();
	}

	private void insertDemoData() {
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
	public void testJoinFields() {
		insertDemoData();
		TinyNet net = ctx.loadKeyEntityNet(User.class);
		Assert.assertEquals(5, net.size());
		List<User> users = net.getAllEntityList(User.class);
		Assert.assertNull(users.get(0).getUserName());

		User u = new User();
		u.tableModel().setAlias("u");
		List<Map<String, Object>> listMap = ctx.nQuery(new MapListHandler(),
				netConfig(u) + "select u.id as u_id, u.userName as u_userName from usertb as u");
		ctx.joinList(net, listMap);

		Assert.assertEquals(5, net.size());
		users = net.getAllEntityList(User.class);
		Assert.assertNotNull(users.get(0).getUserName());
	}

	@Test
	public void testJoinParents() {
		insertDemoData();
		List<Map<String, Object>> listMap = ctx.nQuery(new MapListHandler(),
				netConfig(new Email().alias("e")) + "select e.id as e_id from emailtb e");
		TinyNet net = (TinyNet) ctx.createEntityNet(listMap);
		Assert.assertEquals(5, net.size());
		Node e1 = net.getBody().get(Email.class).get("e1");
		Assert.assertNull(e1.getParentRelations());

		List<Map<String, Object>> listMap2 = ctx.nQuery(new MapListHandler(),
				netConfig(Email.class) + "select e.** from emailtb e");
		ctx.joinList(net, listMap2);
		Assert.assertEquals(5, net.size());
		e1 = net.getBody().get(Email.class).get("e1");
		Assert.assertNotNull(e1.getParentRelations());
	}

	@Test
	public void testloadEntityNet() {
		insertDemoData();
		TinyNet net = ctx.loadEntityNet(new User(), Email.class, Address.class, new Role(), Privilege.class,
				UserRole.class, RolePrivilege.class);
		Assert.assertEquals(37, net.size());
		System.out.println(net.size());

		List<User> users = net.getAllEntityList(User.class);
		Assert.assertEquals(5, users.size());
		Assert.assertEquals("user1", users.get(0).getUserName());
		System.out.println(users.get(0).getUserName());
	}

	@Test
	public void testloadKeyEntityNet() {
		insertDemoData();
		TinyNet net = ctx.loadKeyEntityNet(new User(), Email.class, Address.class, new Role(), Privilege.class,
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
		TinyNet net = ctx.createEntityNet(mapList1);
		Assert.assertEquals(10, net.size());

		List<Map<String, Object>> mapList2 = ctx.nQuery(new MapListHandler(),
				netConfig(Email.class) + "select e.** from emailtb as e");
		ctx.joinList(net, mapList2);
		Assert.assertEquals(15, net.size());

		List<Map<String, Object>> mapList3 = ctx.nQuery(
				new MapListHandler(netProcessor(Role.class, UserRole.class, RolePrivilege.class, Privilege.class)),
				"select r.**, ur.**, rp.**, p.** from roletb r, userroletb ur, RolePrivilegetb rp, privilegetb p");
		Assert.assertEquals(900, mapList3.size());
		ctx.joinList(net, mapList3);
		Assert.assertEquals(37, net.size());
	}

	@Test
	public void testFindSelf() {
		int sampleSize = 500;
		int queyrTimes = 100;
		for (int i = 1; i <= sampleSize; i++) {
			new User().put("id", "u" + i).put("userName", "user" + i).insert();
		}
		TinyNet net = ctx.loadEntityNet(new User(), Email.class);

		Set<User> users2 = net.findEntitySet(User.class, new Path("S-", User.class));
		Assert.assertEquals(0, users2.size());

		long start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			Set<User> users = net.findEntitySet(User.class, new Path("S+", User.class).setCacheable(false));
			Assert.assertTrue(users.size() > 0);
		}
		System.out
				.println(String.format("%28s: %6s s", "No Cache", "" + (System.currentTimeMillis() - start) / 1000.0));

		start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			Set<User> users = net.findEntitySet(User.class, new Path("S+", User.class).setCacheable(true));
			Assert.assertTrue(users.size() > 0);
		}
		System.out.println(
				String.format("%28s: %6s s", "With Cache", "" + (System.currentTimeMillis() - start) / 1000.0));
	}

	@Test
	public void testFindChild() {
		int sampleSize = 70;
		int queyrTimes = 70;
		for (int i = 0; i < sampleSize; i++) {
			new User().put("id", "usr" + i).put("userName", "user" + i).insert();
			for (int j = 0; j < sampleSize; j++)
				new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
		}
		TinyNet net = ctx.loadEntityNet(new User(), Email.class);

		Map<Class<?>, Set<Node>> result = null;
		long start = System.currentTimeMillis();

		start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			result = net.findNodeSetForEntities(new Path("S+", User.class).setCacheable(false)
					.nextPath("C+", Email.class, "userId").setCacheable(false));
		}
		System.out
				.println(String.format("%28s: %6s s", "No Cache", "" + (System.currentTimeMillis() - start) / 1000.0));
		System.out.println("user selected2:" + result.get(User.class).size());
		System.out.println("email selected2:" + result.get(Email.class).size());

		start = System.currentTimeMillis();
		for (int i = 0; i < queyrTimes; i++) {
			result = net.findNodeSetForEntities(
					new Path("S+", User.class).setCacheable(true).nextPath("C+", Email.class, "userId"));
		}
		System.out.println(
				String.format("%28s: %6s s", "With Cache", "" + (System.currentTimeMillis() - start) / 1000.0));

		System.out.println("user selected2:" + result.get(User.class).size());
		System.out.println("email selected2:" + result.get(Email.class).size());
	}

	//@formatter:off  
			//net.remove(u);  
			//net.add(u); 
	        //net.update(u); //user new u to replace old u 
	 
}