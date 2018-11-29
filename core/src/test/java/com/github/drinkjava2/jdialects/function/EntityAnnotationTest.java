/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.jdialects.DebugUtils;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
import com.github.drinkjava2.jdialects.entity.RoleTB;
import com.github.drinkjava2.jdialects.entity.UserTB;
import com.github.drinkjava2.jdialects.function.AnnotationTest.Entity1;
import com.github.drinkjava2.jdialects.function.AnnotationTest.Entity2;

/**
 * Annotation Test
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class EntityAnnotationTest extends JdialectsTestBase {

	@Test
	public void ddlOutTest() {
		String[] dropAndCreateDDL = Dialect.H2Dialect
				.toCreateDDL(TableModelUtils.entity2Models(UserTB.class, RoleTB.class));
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);

		testCreateAndDropDatabase(TableModelUtils.entity2Models(Entity1.class, Entity2.class));
		System.out.println(DebugUtils.getTableModelDebugInfo(TableModelUtils.entity2Model(Entity2.class)));
	}
}
