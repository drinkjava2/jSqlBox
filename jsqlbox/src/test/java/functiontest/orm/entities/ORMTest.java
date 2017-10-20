package functiontest.orm.entities;

import static com.github.drinkjava2.jsqlbox.SqlBoxContext.net;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.ModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.EntityNet;

import config.TestBase;

public class ORMTest extends TestBase {
	@Before
	public void init() {
		super.init();
		TableModel[] models = ModelUtils.entity2Model(User.class, Email.class, Address.class, Role.class,
				Privilege.class, UserRole.class, RolePrivilege.class);
		dropAndCreateDatabase(models);
		ctx.refreshMetaData();
		ctx.nExecute("insert into usertb values('u1','user1')");
		ctx.nExecute("insert into usertb values('u2','user2')");
		ctx.nExecute("insert into usertb values('u3','user3')");
		ctx.nExecute("insert into usertb values('u4','user4')");
		ctx.nExecute("insert into usertb values('u5','user5')");

		ctx.nExecute("insert into address values('a1','address1','u1')");
		ctx.nExecute("insert into address values('a2','address2','u2')");
		ctx.nExecute("insert into address values('a3','address3','u3')");
		ctx.nExecute("insert into address values('a4','address4','u4')");
		ctx.nExecute("insert into address values('a5','address5','u5')");

		ctx.nExecute("insert into email values('e1','email1','u1')");
		ctx.nExecute("insert into email values('e2','email2','u1')");
		ctx.nExecute("insert into email values('e3','email3','u2')");
		ctx.nExecute("insert into email values('e4','email4','u2')");
		ctx.nExecute("insert into email values('e5','email5','u3')");

		ctx.nExecute("insert into roles values('r1','role1')");
		ctx.nExecute("insert into roles values('r2','role2')");
		ctx.nExecute("insert into roles values('r3','role3')");
		ctx.nExecute("insert into roles values('r4','role4')");
		ctx.nExecute("insert into roles values('r5','role5')");

		ctx.nExecute("insert into privilegetb values('p1','privilege1')");
		ctx.nExecute("insert into privilegetb values('p2','privilege2')");
		ctx.nExecute("insert into privilegetb values('p3','privilege3')");
		ctx.nExecute("insert into privilegetb values('p4','privilege4')");
		ctx.nExecute("insert into privilegetb values('p5','privilege5')");

		ctx.nExecute("insert into userroletb values('u1','r1')");
		ctx.nExecute("insert into userroletb values('u2','r1')");
		ctx.nExecute("insert into userroletb values('u2','r2')");
		ctx.nExecute("insert into userroletb values('u2','r3')");
		ctx.nExecute("insert into userroletb values('u3','r4')");
		ctx.nExecute("insert into userroletb values('u4','r1')");

		ctx.nExecute("insert into roleprivilege values('r1','p1')");
		ctx.nExecute("insert into roleprivilege values('r2','p1')");
		ctx.nExecute("insert into roleprivilege values('r2','p2')");
		ctx.nExecute("insert into roleprivilege values('r2','p3')");
		ctx.nExecute("insert into roleprivilege values('r3','p3')");
		ctx.nExecute("insert into roleprivilege values('r4','p1')");
	}

	@Test
	public void test() {
		List<Map<String, Object>> listMap = ctx.nQuery(new MapListHandler(),
				net(User.class, Email.class) + "select ur.**, e.** from usertb ur, email e where ur.id=e.userId");

		EntityNet net = new EntityNet(ctx, listMap);

		//@formatter:off
		// EntityNet net = new EntityNet(null, listMap, User.class, new Email()),

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

}