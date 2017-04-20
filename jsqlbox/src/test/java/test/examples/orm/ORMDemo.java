package test.examples.orm;

import static com.github.drinkjava2.jsqlbox.MappingHelper.bind;
import static com.github.drinkjava2.jsqlbox.MappingHelper.oneToMany;
import static com.github.drinkjava2.jsqlbox.MappingHelper.oneToOne;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.examples.orm.entities.Address;
import test.examples.orm.entities.Email;
import test.examples.orm.entities.Privilege;
import test.examples.orm.entities.Role;
import test.examples.orm.entities.RolePrivilege;
import test.examples.orm.entities.User;
import test.examples.orm.entities.UserRole;

public class ORMDemo extends TestBase {

	@Before
	public void setup() {
		super.setup();
		Dao.executeQuiet("DROP TABLE users");
		Dao.executeQuiet("DROP TABLE email");
		Dao.executeQuiet("DROP TABLE address");
		Dao.executeQuiet("DROP TABLE roles");
		Dao.executeQuiet("DROP TABLE privilegetb");
		Dao.executeQuiet("DROP TABLE userroletb");
		Dao.executeQuiet("DROP TABLE roleprivilege");
		Dao.execute(User.CREATE_SQL);
		Dao.execute(Email.CREATE_SQL);
		Dao.execute(Address.CREATE_SQL);
		Dao.execute(Role.CREATE_SQL);
		Dao.execute(Privilege.CREATE_SQL);
		Dao.execute(UserRole.CREATE_SQL);
		Dao.execute(RolePrivilege.CREATE_SQL);

		Dao.execute("insert into users values('u1','user1')");
		Dao.execute("insert into users values('u2','user2')");
		Dao.execute("insert into users values('u3','user3')");
		Dao.execute("insert into users values('u4','user4')");
		Dao.execute("insert into users values('u5','user5')");

		Dao.execute("insert into address values('a1','address1','u1')");
		Dao.execute("insert into address values('a2','address2','u2')");
		Dao.execute("insert into address values('a3','address3','u3')");
		Dao.execute("insert into address values('a4','address4','u4')");
		Dao.execute("insert into address values('a5','address5','u5')");

		Dao.execute("insert into email values('e1','email1','u1')");
		Dao.execute("insert into email values('e2','email2','u1')");
		Dao.execute("insert into email values('e3','email3','u2')");
		Dao.execute("insert into email values('e4','email4','u2')");
		Dao.execute("insert into email values('e5','email5','u3')");

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
		// Dao.getDefaultContext().setShowSql(true).setShowQueryResult(true);
	}

	/**
	 * 1 user has 1 address, 1 address has 1 user
	 */
	@Test
	public void oneToOneNoBind() {
		System.out.println("============oneToOneNoBind=========");
		User u = new User();
		Address a = new Address();
		List<User> users = Dao.queryForEntityList(User.class, select(), u.all(), ",", a.all(), from(), u.table(), ",",
				a.table(), " where ", oneToOne(), u.ID(), "=", a.UID(), bind());
		for (User user : users) {
			System.out.println(user.getUserName());
			Address address = user.getChildNode(Address.class);
			System.out.println("\t" + address.getAddressName());
			User user2 = address.getParentNode(User.class);
			Assert.assertTrue(user == user2);
		}
	}

	/**
	 * 1 user has 1 address, 1 address has 1 user
	 */
	@Test
	public void oneToOneWithBind() {
		System.out.println("============oneToOneWithBind=========");
		User u = new User();
		Address a = new Address();
		u.configMapping(oneToOne(), u.ID(), a.UID(), bind(u.ADDRESS(), a.USER()));
		List<User> users = Dao.queryForEntityList(User.class, select(), u.all(), ",", a.all(), from(), u.table(), ",",
				a.table(), " where ", u.ID(), "=", a.UID());
		for (User user : users) {
			System.out.println(user.getUserName());
			Address address = user.getAddress();
			System.out.println("\t" + address.getAddressName());
			User user2 = address.getUser();
			Assert.assertTrue(user == user2);
		}
	}

	/**
	 * 1 user has many Email
	 */
	@Test
	public void oneToManyNoBind() {
		System.out.println("============oneToManyNoBind1=========");
		User u = new User();
		Email e = new Email();
		List<User> users = Dao.queryForEntityList(User.class, select(), u.all(), ",", e.all(), from(), u.table(), ",",
				e.table(), " where ", oneToMany(), u.ID(), "=", e.UID(), bind());
		for (User user : users) {
			System.out.println(user.getUserName());
			Set<Email> emails = user.getChildNodeSet(Email.class);
			for (Email email : emails) {
				System.out.println("\t" + email.getEmailName());
				Assert.assertTrue(user == email.getParentNode(User.class));
			}
		}
	}

	/**
	 * many Email have 1 user
	 */
	@Test
	public void oneToManyNoBind2() {
		System.out.println("============oneToManyNoBind2=========");
		User u = new User();
		Email e = new Email();
		List<Email> emails = Dao.queryForEntityList(Email.class, select(), u.all(), ",", e.all(), from(), u.table(),
				",", e.table(), " where ", oneToMany(), u.ID(), "=", e.UID(), bind());
		for (Email email : emails) {
			System.out.println(email.getEmailName());
			User user = email.getParentNode(User.class);
			System.out.println("\t" + user.getUserName());
		}
	}

	@Test
	public void oneToManyWithBind() {
		System.out.println("============oneToManyWithBind=========");
		User u = new User();
		Email e = new Email();
		List<User> users = Dao.queryForEntityList(User.class, select(), u.all(), ",", e.all(), from(), u.table(), ",",
				e.table(), " where ", oneToMany(), u.ID(), "=", e.UID(), bind(u.EMAILS(), e.USER()));
		for (User user : users) {
			System.out.println(user.getUserName());
			Set<Email> emails = user.getEmails();
			for (Email email : emails) {
				System.out.println("\t" + email.getEmailName());
				Assert.assertTrue(email.getUser() == user);
			}
		}
	}

	/**
	 * Use 2 oneToMany() to simulate 1 manyToMany <br/>
	 * u has many UserRole, r has many UserRole, r has many RolePrivilege,
	 * privilege has many RolPrivilege <br/>
	 */
	@Test
	public void manyToManyTest() {
		System.out.println("============manyToManyTest=========");
		User u = new User();
		Role r = new Role();
		Privilege p = new Privilege();
		UserRole ur = new UserRole();
		RolePrivilege rp = new RolePrivilege();
		Dao.getDefaultContext().setShowSql(true);
		List<User> users = Dao.queryForEntityList(User.class,
				u.pagination(1, 10, //
						select(), u.all(), ",", ur.all(), ",", r.all(), ",", rp.all(), ",", p.all(), from(), u.table(), //
						" left join ", ur.table(), " on ", oneToMany(), u.ID(), "=", ur.UID(), bind(), //
						" left join ", r.table(), " on ", oneToMany(), r.ID(), "=", ur.RID(), bind(), //
						" left join ", rp.table(), " on ", oneToMany(), r.ID(), "=", rp.RID(), bind(), //
						" left join ", p.table(), " on ", oneToMany(), p.ID(), "=", rp.PID(), bind(), //
						" order by ", u.ID(), ",", r.ID(), ",", p.ID()));
		for (User user : users) {
			System.out.println(user.getUserName());
			Set<Role> roles = user.getUniqueNodeSet(Role.class);
			for (Role role : roles)
				System.out.println("\t" + role.getRoleName());
			Set<Privilege> privs = user.getUniqueNodeSet(Privilege.class);
			for (Privilege priv : privs)
				System.out.println("\t" + priv.getPrivilegeName());
		}
	}

}