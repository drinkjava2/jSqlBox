/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package functiontest.idgenerator;

import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.valuesQuesions;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.GenerationType;
import com.github.drinkjava2.jdialects.id.AutoIdGenerator;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.SortedUUIDGenerator;
import com.github.drinkjava2.jdialects.id.UUID25Generator;
import com.github.drinkjava2.jdialects.id.UUID32Generator;
import com.github.drinkjava2.jdialects.id.UUID36Generator;
import com.github.drinkjava2.jdialects.model.TableModel;

import config.TestBase;

/**
 * Unit test for SortedUUIDGenerator
 */
public class IdgeneratorTest extends TestBase {
	
	public static class uuidTester{
		private String id1;
	} 
	
	@Test
	public void testUUIDs() {// nextID
		TableModel table = new TableModel("testNextIdTable");
		table.addColumn("id1").STRING(25).pkey();
		table.addColumn("id2").STRING(32);
		table.addColumn("id3").STRING(36);
		dropAndCreateDatabase(table);

		for (int i = 0; i < 10; i++) {
			Object id1 = dialect.getNexID(UUID25Generator.INSTANCE, ctx, null);
			Object id2 = dialect.getNexID(UUID32Generator.INSTANCE, ctx, null);
			Object id3 = dialect.getNexID(UUID36Generator.INSTANCE, ctx, null);
			System.out.println(id1);
			System.out.println(id2);
			System.out.println(id3);
			Assert.assertTrue(("" + id1).length() == 25);
			Assert.assertTrue(("" + id2).length() == 32);
			Assert.assertTrue(("" + id3).length() == 36);
			ctx.iExecute("insert into testNextIdTable (id1,id2,id3) ", param0(id1), param(id2), param(id3),
					valuesQuesions());
		}
	}

	@Test
	public void testAutoIdGenerator() {
		TableModel table = new TableModel("testAutoIdGenerator");
		table.addColumn("id").STRING(30).pkey().autoID();
		dropAndCreateDatabase(table);

		IdGenerator gen = table.getColumn("id").getIdGenerator();
		for (int i = 0; i < 5; i++) {
			Assert.assertNotNull(gen.getNextID(ctx, dialect, null));
			System.out.println(gen.getNextID(ctx, dialect, null));
		}

		gen = AutoIdGenerator.INSTANCE;
		for (int i = 0; i < 5; i++) {
			Assert.assertNotNull(gen.getNextID(ctx, dialect, null));
			System.out.println(gen.getNextID(ctx, dialect, null));
		}
	}

	@Test
	public void testSortedUUIDGenerator() {
		TableModel table = new TableModel("testSortedUUIDGenerator");
		table.sortedUUIDGenerator("sorteduuid", 8, 8);
		table.addGenerator(new SortedUUIDGenerator("sorteduuid2", 10, 10));
		table.addColumn("id").STRING(30).pkey().idGenerator("sorteduuid");
		table.addColumn("id2").STRING(30).pkey().idGenerator("sorteduuid2");
		dropAndCreateDatabase(table);

		IdGenerator gen1 = table.getIdGenerator("sorteduuid");
		for (int i = 0; i < 10; i++){
			Assert.assertNotNull(gen1.getNextID(ctx, dialect, null));
			System.out.println(gen1.getNextID(ctx, dialect, null));
		}

		IdGenerator gen2 = table.getIdGenerator("sorteduuid2");
		for (int i = 0; i < 10; i++){
			Assert.assertNotNull(gen2.getNextID(ctx, dialect, null));
			System.out.println(gen2.getNextID(ctx, dialect, null));
		}
	}

	@Test
	public void testSequenceIdGenerator() {
		if (!dialect.getDdlFeatures().supportBasicOrPooledSequence())
			return;
		TableModel table1 = new TableModel("testTableIdGenerator");
		table1.sequenceGenerator("seq1", "seq1", 1, 10);
		table1.addColumn("id").STRING(30).pkey().idGenerator("seq1");
		table1.addColumn("id2").STRING(30).pkey().sequenceGenerator("seq2", "seq2", 1, 20);

		TableModel table2 = new TableModel("testTableIdGenerator2");
		table2.sequenceGenerator("seq3", "seq3", 1, 10);
		table2.addColumn("id").STRING(30).pkey().idGenerator("seq3");
		table2.addColumn("id2").STRING(30).pkey().sequenceGenerator("seq2", "seq2", 1, 20);

		dropAndCreateDatabase(table1, table2);

		IdGenerator gen1 = table1.getIdGenerator("seq1");
		IdGenerator gen2 = table1.getIdGenerator("seq2");
		for (int i = 0; i < 3; i++) {
			System.out.println(gen1.getNextID(ctx, dialect, null));
			System.out.println(gen2.getNextID(ctx, dialect, null));
		}

		IdGenerator gen3 = table2.getIdGenerator("seq3");
		IdGenerator gen4 = table2.getIdGenerator("seq2");
		for (int i = 0; i < 3; i++) {
			System.out.println(gen3.getNextID(ctx, dialect, null));
			System.out.println(gen4.getNextID(ctx, dialect, null));
		}
	}

	@Test
	public void testTableIdGenerator() {
		TableModel table1 = new TableModel("testTableIdGenerator");
		table1.tableGenerator("tab1", "tb1", "pkCol", "valueColname", "pkColVal", 1, 10);
		table1.addColumn("id").STRING(30).pkey().idGenerator("tab1");
		table1.addColumn("id2").STRING(30).pkey().tableGenerator("tab2", "tb1", "pkCol", "valueColname", "pkColVal", 1,
				10);

		TableModel table2 = new TableModel("testTableIdGenerator2");
		table2.tableGenerator("tab3", "tb1", "pkCol", "valueColname", "pkColVal", 1, 10);
		table2.addColumn("id").STRING(30).pkey().idGenerator("tab3");
		table2.addColumn("id2").STRING(30).pkey().tableGenerator("tab2", "tb1", "pkCol", "valueColname", "pkColVal", 1,
				10);

		dropAndCreateDatabase(table1, table2);

		IdGenerator gen1 = table1.getIdGenerator("tab1");
		IdGenerator gen2 = table1.getIdGenerator("tab2");
		for (int i = 0; i < 3; i++) {
			System.out.println(gen1.getNextID(ctx, dialect, null));
			System.out.println(gen2.getNextID(ctx, dialect, null));
		}

		IdGenerator gen3 = table2.getIdGenerator("tab3");
		IdGenerator gen4 = table2.getIdGenerator("tab2");
		for (int i = 0; i < 3; i++) {
			System.out.println(gen3.getNextID(ctx, dialect, null));
			System.out.println(gen4.getNextID(ctx, dialect, null));
		}
	}

	@Test
	public void testIdentityGenerator() {
		TableModel table = new TableModel("testIdentity");
		table.addColumn("id").INTEGER().identity();
		table.addColumn("name").STRING(30);
		dropAndCreateDatabase(table);

		ctx.nExecute("insert into testIdentity (name) values(?)", "Tom");
		ctx.nExecute("insert into testIdentity (name) values(?)", "Sam");
		IdGenerator idGen = table.getIdGenerator(GenerationType.IDENTITY);
		System.out.println(idGen.getNextID(ctx, dialect, Type.INTEGER));

		idGen = table.getColumn("id").getIdGenerator();
		System.out.println(idGen.getNextID(ctx, dialect, Type.INTEGER));
	}

}
