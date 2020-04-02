package com.github.drinkjava2.jsqlbox.function.jdialects;
/* 
 * To test Identity annotation
 * 
 * @author Yong Zhu
 * @since 4.0.2
 */

/*- JAVA8_BEGIN */

import org.junit.Assert;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.annotation.jdia.IdentityId;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.config.TestBase;

public class IdentityIdTest extends TestBase {

	public static class EntityDemo implements ActiveEntity<EntityDemo> {

		@Id
		@IdentityId
		private Integer id;
		private String fullName;
		private Integer pid;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public Integer getPid() {
			return pid;
		}

		public void setPid(Integer pid) {
			this.pid = pid;
		}
	}

	@Test
	public void doTest() {
		if (!dialect.ddlFeatures.getSupportsIdentityColumns())
			Systemout.print("Dialect '" + dialect + "' does not support identity type, skip IdentityId unit test.");

		createAndRegTables(EntityDemo.class);

		EntityDemo e = new EntityDemo();
		e.setFullName("a");
		e.insert();

		e.setPid(e.getId());
		e.setFullName("b");
		e.setId(null);
		e.insert();

		Assert.assertEquals("a", e.loadById(e.getPid()).getFullName());
	}

}
/* JAVA8_END */