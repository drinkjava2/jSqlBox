/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.GeneratedValue;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
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
public class IdgeneratorTest extends JdialectsTestBase {

	@Test
	public void testPKey() {// nextID
		TableModel t = new TableModel("testPKey");
		t.column("id1").STRING(25).pkey();
		Assert.assertTrue(t.column("id1").getPkey());
	}

	@Test
	public void testUUIDs() {// nextID
		TableModel t = new TableModel("testNextIdTable");
		t.column("id1").STRING(25).pkey();
		t.column("id2").STRING(32);
		t.column("id3").STRING(36);
		String[] ddls = guessedDialect.toDropDDL(t);
		quietExecuteDDLs(ddls);

		ddls = guessedDialect.toCreateDDL(t);
		executeDDLs(ddls);
		for (int i = 0; i < 10; i++) {
			Object id1 = guessedDialect.getNexID(UUID25Generator.INSTANCE, dbPro, null);
			Object id2 = guessedDialect.getNexID(UUID32Generator.INSTANCE, dbPro, null);
			Object id3 = guessedDialect.getNexID(UUID36Generator.INSTANCE, dbPro, null);
			Systemout.println(id1);
			Systemout.println(id2);
			Systemout.println(id3);
			Assert.assertTrue(("" + id1).length() == 25);
			Assert.assertTrue(("" + id2).length() == 32);
			Assert.assertTrue(("" + id3).length() == 36);
			dbPro.nExecute("insert into testNextIdTable (id1,id2,id3) values(?,?,?) ", id1, id2, id3);
		}
		dropDB(t);
	}

	@Test
	public void testAutoIdGenerator() {
		TableModel table = new TableModel("testAutoIdGenerator");
		table.column("id").STRING(30).pkey().autoId();
		reBuildDB(table);

		IdGenerator gen = table.getColumn("id").getIdGenerator();
		for (int i = 0; i < 5; i++)
			Systemout.println(gen.getNextID(dbPro, guessedDialect, null));

		gen = AutoIdGenerator.INSTANCE;
		for (int i = 0; i < 5; i++)
			Systemout.println(gen.getNextID(dbPro, guessedDialect, null));
		dropDB(table);
	}

	@Test
	public void testSortedUUIDGenerator() {
		TableModel table = new TableModel("testSortedUUIDGenerator");
		table.sortedUUIDGenerator("sorteduuid", 8, 8);
		table.addGenerator(new SortedUUIDGenerator("sorteduuid2", 10, 10));
		table.column("id").STRING(30).pkey().idGenerator("sorteduuid");
		table.column("id2").STRING(30).pkey().idGenerator("sorteduuid2");
		reBuildDB(table);

		IdGenerator gen1 = table.getIdGenerator("sorteduuid");
		for (int i = 0; i < 10; i++)
			Systemout.println(gen1.getNextID(dbPro, guessedDialect, null));

		IdGenerator gen2 = table.getIdGenerator("sorteduuid2");
		for (int i = 0; i < 10; i++)
			Systemout.println(gen2.getNextID(dbPro, guessedDialect, null));
		dropDB(table);
	}

	@Test
	public void testSequenceIdGenerator() {
		if (!guessedDialect.getDdlFeatures().supportBasicOrPooledSequence())
			return;
		TableModel table1 = new TableModel("testTableIdGenerator");
		table1.sequenceGenerator("seq1", "seq1", 1, 10);
		table1.column("id").STRING(30).pkey().idGenerator("seq1");
		table1.column("id2").STRING(30).pkey().sequenceGenerator("seq2", "seq2", 1, 20);

		TableModel table2 = new TableModel("testTableIdGenerator2");
		table2.sequenceGenerator("seq3", "seq3", 1, 10);
		table2.column("id").STRING(30).pkey().idGenerator("seq3");
		table2.column("id2").STRING(30).pkey().sequenceGenerator("seq2", "seq2", 1, 20);

		reBuildDB(table1, table2);

		IdGenerator gen1 = table1.getIdGenerator("seq1");
		IdGenerator gen2 = table1.getIdGenerator("seq2");
		for (int i = 0; i < 3; i++) {
			Systemout.println(gen1.getNextID(dbPro, guessedDialect, null));
			Systemout.println(gen2.getNextID(dbPro, guessedDialect, null));
		}

		IdGenerator gen3 = table2.getIdGenerator("seq3");
		IdGenerator gen4 = table2.getIdGenerator("seq2");
		for (int i = 0; i < 3; i++) {
			Systemout.println(gen3.getNextID(dbPro, guessedDialect, null));
			Systemout.println(gen4.getNextID(dbPro, guessedDialect, null));
		}
		dropDB(table1, table2);
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

		reBuildDB(table1, table2);

		IdGenerator gen1 = table1.getIdGenerator("tab1");
		IdGenerator gen2 = table1.getIdGenerator("tab2");
		for (int i = 0; i < 3; i++) {
			Systemout.println(gen1.getNextID(dbPro, guessedDialect, null));
			Systemout.println(gen2.getNextID(dbPro, guessedDialect, null));
		}

		IdGenerator gen3 = table2.getIdGenerator("tab3");
		IdGenerator gen4 = table2.getIdGenerator("tab2");
		for (int i = 0; i < 3; i++) {
			Systemout.println(gen3.getNextID(dbPro, guessedDialect, null));
			Systemout.println(gen4.getNextID(dbPro, guessedDialect, null));
		}
		dropDB(table1, table2);
	}

	@Test
	public void testIdentityGenerator() {
		TableModel table = new TableModel("testIdentity");
		table.column("id").INTEGER().identityId();
		table.column("name").STRING(30);
		reBuildDB(table);

		dbPro.nExecute("insert into testIdentity (name) values(?)", "Tom");
		dbPro.nExecute("insert into testIdentity (name) values(?)", "Sam");
		IdGenerator idGen = table.getIdGenerator(GenerationType.IDENTITY);
		Systemout.println(idGen.getNextID(dbPro, guessedDialect, Type.INTEGER));

		idGen = table.getColumn("id").getIdGenerator();
		Systemout.println(idGen.getNextID(dbPro, guessedDialect, Type.INTEGER));
		dropDB(table);
	}

	public static class pkeyEntity {
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
	public void testPKey2() {// nextID
		TableModel t = TableModelUtils.entity2Model(pkeyEntity.class);
		Assert.assertTrue(t.column("id1").getPkey());
		Assert.assertTrue(t.column("id2").getPkey());
	}

	public static class uuid25Entity {
		@GeneratedValue(strategy = GenerationType.UUID25)
		private String id1;
		@UUID25
		private String id2;

		private String id3;

		public static void config(TableModel t) {
			t.getColumn("id3").uuid25();
		}

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

	}

	@Test
	public void testUUID25() {
		reBuildDB(TableModelUtils.entity2Models(uuid25Entity.class));
		testOnCurrentRealDatabase(TableModelUtils.entity2Models(uuid25Entity.class));
	}
}
