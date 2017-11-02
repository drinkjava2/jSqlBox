package functiontest.orm;

import static com.github.drinkjava2.jsqlbox.SqlBoxContext.netConfig;
import static com.github.drinkjava2.jsqlbox.SqlBoxContext.netProcessor;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.ModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.EntityNet;

import config.TestBase;
import functiontest.orm.entities.Address;
import functiontest.orm.entities.Email;
import functiontest.orm.entities.Privilege;
import functiontest.orm.entities.Role;
import functiontest.orm.entities.RolePrivilege;
import functiontest.orm.entities.User;
import functiontest.orm.entities.UserRole;

public class ORMTest extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		TableModel[] models = ModelUtils.entity2Model(User.class, Email.class, Address.class, Role.class,
				Privilege.class, UserRole.class, RolePrivilege.class);
		dropAndCreateDatabase(models);
		ctx.refreshMetaData();

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
	public void testCreateEntityNet() {
		EntityNet net = ctx.createEntityNet(new User(), Email.class, Address.class, new Role(), Privilege.class,
				UserRole.class, RolePrivilege.class);
		Assert.assertEquals(37, net.size());
		System.out.println(net.size());

		List<User> users = net.getEntityList(User.class);
		Assert.assertEquals(5, users.size());
		Assert.assertEquals("user1", users.get(0).getUserName());
		System.out.println(users.get(0).getUserName());
	}

	@Test
	public void testLazyCreateEntityNet() {
		EntityNet net = ctx.createKeyEntityNet(new User(), Email.class, Address.class, new Role(), Privilege.class,
				UserRole.class, RolePrivilege.class);
		Assert.assertEquals(37, net.size());
		System.out.println(net.size());

		List<User> users = net.getEntityList(User.class);
		Assert.assertEquals(5, users.size());
		Assert.assertEquals(null, users.get(0).getUserName());
		System.out.println(users.get(0).getUserName());
	}

	@Test
	public void testManualLoadAndJoin() {
		ctx.setAllowShowSQL(true);
		List<Map<String, Object>> mapList1 = ctx.nQuery(new MapListHandler(netProcessor(User.class, Address.class)),
				"select u.**, a.** from usertb u, addresstb a where a.userId=u.id");
		EntityNet net = ctx.createEntityNet(mapList1);
		System.out.println(net.size());
		Assert.assertEquals(10, net.size());

		List<Map<String, Object>> mapList2 = ctx.nQuery(new MapListHandler(),
				netConfig(Email.class) + "select e.** from emailtb as e");
		ctx.joinList(net, mapList2);
		System.out.println(net.size());
		Assert.assertEquals(15, net.size());

		List<Map<String, Object>> mapList3 = ctx.nQuery(
				new MapListHandler(netProcessor(Role.class, UserRole.class, RolePrivilege.class, Privilege.class)),
				"select r.**, ur.**, rp.**, p.** from roletb r, userroletb ur, RolePrivilegetb rp, privilegetb p");
		System.out.println(mapList3.size());
		Assert.assertEquals(900, mapList3.size());

		ctx.joinList(net, mapList3);
		System.out.println(net.size());
		Assert.assertEquals(37, net.size());
	}

	//@formatter:off
	 		// List<User> users = net.getList(User.class);
			// for (User user : users) {
			// List<Email> emails = user.getChildEntities(Email.class); 
			          //Email.class can omit if search path no cause confuse, below methods same
			// List<Email> emails = ctx.getChildEntities(user, Email.class); 

			// User u = email.getParentEntity(User.class);
			// User u = ctx.getParentEntity(email, User.class);  
			// List<User> u = ctx.getParentEntities(emails, User.class); 
	 
			// List<Email> emails = user.getRelatedNodes("parent",Path1.class, "child",Path1.class..., "parent", Email.class); 
			// List<Email> emails = user.getRelatedNodes(Email.class); //if path can be guessed by computer 
			// }
			
			//EntityNet net=u.entityNet();
			//EntityNet net=Net.getEntityNet(u);
			//net.remove(u);
			//u.setAddress("new Address");
			//net.update(u);
			//net.add(u);
			//net.flush();
			//ctx.flushEntityNet(net);
			//net.setReadonlyAndCache(true)
	

}