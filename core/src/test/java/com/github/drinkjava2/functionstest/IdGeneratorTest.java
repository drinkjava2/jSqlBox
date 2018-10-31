/**
 * Copyright (C) 2016 Original Author
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
package com.github.drinkjava2.functionstest;
 
import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jdbpro.JDBPRO.valuesQuestions;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.id.AutoIdGenerator;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.SortedUUIDGenerator;
import com.github.drinkjava2.jdialects.id.UUID25Generator;
import com.github.drinkjava2.jdialects.id.UUID32Generator;
import com.github.drinkjava2.jdialects.id.UUID36Generator;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Unit test for SortedUUIDGenerator
 */
public class IdGeneratorTest extends TestBase {

	public static class PkeyEntity {
		@Id
		private String id1;

		@PKey
		private String id2;

		public String getId2() {
			return id2;
		}

		public void setId2(String id2) {
			this.id2 = id2;
		}

		public String getId1() {
			return id1;
		}

		public void setId1(String id1) {
			this.id1 = id1;
		}
	}

	@Test
	public void testPKey() {// nextID
		TableModel t = TableModelUtils.entity2ReadOnlyModel(PkeyEntity.class);
		Assert.assertTrue(t.column("id1").getPkey());
		Assert.assertTrue(t.column("id2").getPkey());
	}

	public static class uuidTester {
		private String id1;
		private String id2;
		private String id3;
		private String id4;
		private String id5;

		public String getId1() {
			return id1;
		}

		public void setId1(String id1) {
			this.id1 = id1;
		}

		public String getId2() {
			return id2;
		}

		public void setId2(String id2) {
			this.id2 = id2;
		}

		public String getId3() {
			return id3;
		}

		public void setId3(String id3) {
			this.id3 = id3;
		}

		public String getId4() {
			return id4;
		}

		public void setId4(String id4) {
			this.id4 = id4;
		}

		public String getId5() {
			return id5;
		}

		public void setId5(String id5) {
			this.id5 = id5;
		}
	}

	@Test
	public void testUUIDs() {// nextID
		TableModel table = new TableModel("testNextIdTable");
		table.column("id1").STRING(25).pkey();
		table.column("id2").STRING(32);
		table.column("id3").STRING(36);
		createAndRegTables(table);

		for (int i = 0; i < 10; i++) {
			Object id1 = dialect.getNexID(UUID25Generator.INSTANCE, ctx, null);
			Object id2 = dialect.getNexID(UUID32Generator.INSTANCE, ctx, null);
			Object id3 = dialect.getNexID(UUID36Generator.INSTANCE, ctx, null);
			System.out.println("id1=" + id1);
			System.out.println("id2=" + id2);
			System.out.println("id3=" + id3);
			Assert.assertTrue(("" + id1).length() == 25);
			Assert.assertTrue(("" + id2).length() == 32);
			Assert.assertTrue(("" + id3).length() == 36);
			ctx.iExecute("insert into testNextIdTable (id1,id2,id3) ", param(id1, id2, id3), valuesQuestions());
		}
	}

	@Test
	public void testAutoIdGenerator() {
		TableModel table = new TableModel("testAutoIdGenerator");
		table.column("id").STRING(30).pkey().autoId();
		createAndRegTables(table);

		IdGenerator gen = table.getColumnByColName("id").getIdGenerator();
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
		table.column("id").STRING(30).pkey().idGenerator("sorteduuid");
		table.column("id2").STRING(30).pkey().idGenerator("sorteduuid2");
		createAndRegTables(table);

		IdGenerator gen1 = table.getIdGenerator("sorteduuid");
		for (int i = 0; i < 10; i++) {
			Assert.assertNotNull(gen1.getNextID(ctx, dialect, null));
			System.out.println(gen1.getNextID(ctx, dialect, null));
		}

		IdGenerator gen2 = table.getIdGenerator("sorteduuid2");
		for (int i = 0; i < 10; i++) {
			Assert.assertNotNull(gen2.getNextID(ctx, dialect, null));
			System.out.println(gen2.getNextID(ctx, dialect, null));
		}
	}

	@Test
	public void testSequenceIdGenerator() {
		System.out.println(dialect);
		if (!dialect.getDdlFeatures().supportBasicOrPooledSequence())
			return;
		TableModel table1 = new TableModel("testTableIdGenerator");
		table1.sequenceGenerator("seq1", "seq1", 1, 10);
		table1.column("id").STRING(30).pkey().idGenerator("seq1");
		table1.column("id2").STRING(30).pkey().sequenceGenerator("seq2", "seq2", 1, 20);

		TableModel table2 = new TableModel("testTableIdGenerator2");
		table2.sequenceGenerator("seq3", "seq3", 1, 10);
		table2.column("id").STRING(30).pkey().idGenerator("seq3");
		table2.column("id2").STRING(30).pkey().sequenceGenerator("seq2", "seq2", 1, 20);

		createAndRegTables(table1, table2);

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
		table1.column("id").STRING(30).pkey().idGenerator("tab1");
		table1.column("id2").STRING(30).pkey().tableGenerator("tab2", "tb1", "pkCol", "valueColname", "pkColVal", 1,
				10);

		TableModel table2 = new TableModel("testTableIdGenerator2");
		table2.tableGenerator("tab3", "tb1", "pkCol", "valueColname", "pkColVal", 1, 10);
		table2.column("id").STRING(30).pkey().idGenerator("tab3");
		table2.column("id2").STRING(30).pkey().tableGenerator("tab2", "tb1", "pkCol", "valueColname", "pkColVal", 1,
				10);

		createAndRegTables(table1, table2);

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
		table.column("id").INTEGER().identityId().id();
		table.column("name").STRING(30);
		createAndRegTables(table);

		ctx.nExecute("insert into testIdentity (name) values(?)", "Tom");
		ctx.nExecute("insert into testIdentity (name) values(?)", "Sam");
		IdGenerator idGen = table.getIdGenerator(GenerationType.IDENTITY);
		System.out.println(idGen.getNextID(ctx, dialect, Type.INTEGER));

		idGen = table.getColumnByColName("id").getIdGenerator();
		System.out.println(idGen.getNextID(ctx, dialect, Type.INTEGER));
	}

}
