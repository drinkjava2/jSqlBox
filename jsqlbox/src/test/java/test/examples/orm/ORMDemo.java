package test.examples.orm;

import static com.github.drinkjava2.jsqlbox.MappingHelper.oneToMany;
import static com.github.drinkjava2.jsqlbox.MappingHelper.to;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.examples.orm.entities.Privilege;
import test.examples.orm.entities.Role;
import test.examples.orm.entities.RolePrivilege;
import test.examples.orm.entities.User;
import test.examples.orm.entities.UserRole;

public class ORMDemo {

	@Before
	public void setup() {
		System.out.println("===============================Testing ORMDemo===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Dao.getDefaultContext().setShowSql(true);
		Dao.executeQuiet("DROP TABLE users");
		Dao.executeQuiet("DROP TABLE roles");
		Dao.executeQuiet("DROP TABLE privilegetb");
		Dao.executeQuiet("DROP TABLE userroletb");
		Dao.executeQuiet("DROP TABLE roleprivilege");
		Dao.execute(User.CREATE_SQL);
		Dao.execute(Role.CREATE_SQL);
		Dao.execute(Privilege.CREATE_SQL);
		Dao.execute(UserRole.CREATE_SQL);
		Dao.execute(RolePrivilege.CREATE_SQL);

		Dao.execute("insert into users values('u1','user1')");
		Dao.execute("insert into users values('u2','user2')");
		Dao.execute("insert into users values('u3','user3')");
		Dao.execute("insert into users values('u4','user4')");
		Dao.execute("insert into users values('u5','user5')");

		Dao.execute("insert into roles values('r1','role1')");
		Dao.execute("insert into roles values('r2','role2')");
		Dao.execute("insert into roles values('r3','role3')");
		Dao.execute("insert into roles values('r4','role4')");
		Dao.execute("insert into roles values('r5','role5')");

		Dao.execute("insert into privilegetb values('p1','privilege1')");
		Dao.execute("insert into privilegetb values('p2','privilege2')");
		Dao.execute("insert into privilegetb values('p3','privilege3')");
		Dao.execute("insert into privilegetb values('p4','privilege4')");
		Dao.execute("insert into privilegetb values('p5','privilege5')");

		Dao.execute("insert into userroletb values('u1','r1')");
		Dao.execute("insert into userroletb values('u2','r1')");
		Dao.execute("insert into userroletb values('u2','r2')");
		Dao.execute("insert into userroletb values('u2','r3')");
		Dao.execute("insert into userroletb values('u3','r4')");
		Dao.execute("insert into userroletb values('u4','r1')");

		Dao.execute("insert into roleprivilege values('r1','p1')");
		Dao.execute("insert into roleprivilege values('r2','p1')");
		Dao.execute("insert into roleprivilege values('r2','p2')");
		Dao.execute("insert into roleprivilege values('r2','p3')");
		Dao.execute("insert into roleprivilege values('r3','p3')");
		Dao.execute("insert into roleprivilege values('r4','p1')");
		Dao.refreshMetaData();
		Dao.getDefaultContext().setShowSql(true).setShowQueryResult(true);
	}

	@Test
	public void doTest() {
		User u = new User();
		Role r = new Role();
		Privilege p = new Privilege();
		UserRole ur = new UserRole();
		RolePrivilege rp = new RolePrivilege();
		List<User> users = Dao.queryForEntityList(User.class, select(), Dao.pagination(1, 10), u.all(), ",", ur.all(),
				",", r.all(), ",", rp.all(), ",", p.all(), from(), u.table(), //
				" left join ", ur.table(), " on ", oneToMany(), u.ID(), "=", ur.UID(), to(), //
				" left join ", r.table(), " on ", oneToMany(), r.ID(), "=", ur.RID(), to(), //
				" left join ", rp.table(), " on ", oneToMany(), r.ID(), "=", rp.RID(), to(), //
				" left join ", p.table(), " on ", oneToMany(), p.ID(), "=", rp.PID(), to(), //
				" order by ", u.ID());

		for (User user : users) {
			System.out.println(user.getUserName());
			Set<Privilege> privs = user.getUniqueNodeList(Privilege.class);
			for (Privilege priv : privs) {
				System.out.println("\t" + priv.getPrivilegeName());
			}
		}

		List<Privilege> privs = Dao.queryForEntityList(Privilege.class, select(), Dao.pagination(1, 10), u.all(), ",",
				ur.all(), ",", r.all(), ",", rp.all(), ",", p.all(), from(), u.table(), //
				" left join ", ur.table(), " on ", oneToMany(), u.ID(), "=", ur.UID(), to(), //
				" left join ", r.table(), " on ", oneToMany(), r.ID(), "=", ur.RID(), to(), //
				" left join ", rp.table(), " on ", oneToMany(), r.ID(), "=", rp.RID(), to(), //
				" left join ", p.table(), " on ", oneToMany(), p.ID(), "=", rp.PID(), to(), //
				" order by ", u.ID());

		for (Privilege prv : privs) {
			System.out.println(prv.getPrivilegeName());
			Set<User> userList = prv.getUniqueNodeList(User.class);
			for (User usr : userList) {
				System.out.println("\t" + usr.getUserName());
			}
		}

	}

}