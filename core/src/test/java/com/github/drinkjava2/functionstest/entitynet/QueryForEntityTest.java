/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.functionstest.entitynet;

import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param0;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put0;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.functionstest.entitynet.entities.Address;
import com.github.drinkjava2.functionstest.entitynet.entities.Email;
import com.github.drinkjava2.functionstest.entitynet.entities.Privilege;
import com.github.drinkjava2.functionstest.entitynet.entities.Role;
import com.github.drinkjava2.functionstest.entitynet.entities.RolePrivilege;
import com.github.drinkjava2.functionstest.entitynet.entities.User;
import com.github.drinkjava2.functionstest.entitynet.entities.UserRole;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class QueryForEntityTest extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		TableModel[] models = TableModelUtils.entity2Models(User.class, Email.class, Address.class, Role.class,
				Privilege.class, UserRole.class, RolePrivilege.class);
		dropAndCreateDatabase(models);
	}

	@Test
	public void testJoinFields() {
		System.out.println("==============testJoinFields================ ");
		new User().put("id", "u1").put("userName", "user1").put("age", 10).insert();
		new User().put("id", "u2").put("userName", "user2").put("age", 20).insert();
		new User().put("id", "u3").put("userName", "user3").put("age", 30).insert();
		Set<User> setResult = ctx.nQueryForEntitySet(User.class, "select u.** from usertb u where u.age>?", 10);
		Assert.assertTrue(setResult.size() == 2);
		Assert.assertEquals("user2", setResult.iterator().next().getUserName());

		List<User> listResult = ctx.nQueryForEntityList(User.class, "select u.** from usertb u where u.age>?", 10);
		Assert.assertTrue(listResult.size() == 2);
		Assert.assertEquals("user2", listResult.iterator().next().getUserName());

		setResult = ctx.iQueryForEntitySet(User.class, "select u.** from usertb u where u.age>?" + param0(10));
		Assert.assertTrue(setResult.size() == 2);
		Assert.assertEquals("user2", setResult.iterator().next().getUserName());

		listResult = ctx.iQueryForEntityList(User.class, "select u.** from usertb u where u.age>?" + param0(10));
		Assert.assertTrue(listResult.size() == 2);
		Assert.assertEquals("user2", listResult.iterator().next().getUserName());

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("age", 10);
		setResult = ctx.tQueryForEntitySet(User.class, paramMap, "select u.** from usertb u where u.age>#{age}");
		Assert.assertTrue(setResult.size() == 2);
		Assert.assertEquals("user2", setResult.iterator().next().getUserName());

		Map<String, Object> paramMap2 = new HashMap<String, Object>();
		User u = new User();
		u.setAge(10);
		paramMap2.put("u", u);
		listResult = ctx.tQueryForEntityList(User.class, paramMap2, "select u.** from usertb u where u.age>:u.age");
		Assert.assertTrue(setResult.size() == 2);
		Assert.assertEquals("user2", setResult.iterator().next().getUserName());

		setResult = ctx.xQueryForEntitySet(User.class, "select u.** from usertb u where u.age>:age" + put0("age", 10));
		Assert.assertTrue(setResult.size() == 2);
		Assert.assertEquals("user2", setResult.iterator().next().getUserName());

		listResult = ctx.xQueryForEntityList(User.class, "select u.** from usertb u where u.age>:u.age" + put0("u", u));
		Assert.assertTrue(setResult.size() == 2);
		Assert.assertEquals("user2", setResult.iterator().next().getUserName());
	}

}