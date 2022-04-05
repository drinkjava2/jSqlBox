/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class DDLTest extends JdialectsTestBase {

	@Test
	public void testANormalModel() {// A normal setting
		TableModel t = new TableModel("testTable");
		t.column("b1").BOOLEAN();
		t.column("d2").DOUBLE();
		t.column("f3").FLOAT(5);
		t.column("i4").INTEGER().pkey();
		t.column("l5").LONG();
		t.column("s6").SHORT();
		t.column("b7").BIGDECIMAL(10, 2);
		t.column("s8").STRING(20);
		t.column("d9").DATE();
		t.column("t10").TIME();
		t.column("t11").TIMESTAMP();
		t.column("v12").VARCHAR(300);
		printAllDialectsDDLs(t);
		testOnCurrentRealDatabase(t);
	}

	@Test
	public void testNoPkey() {// Test no Prime Key
		// append() is a linked method
		TableModel t = new TableModel("aa");
		t.addColumn(new ColumnModel("aaaa"));
		TableModel table = new TableModel("tb").addColumn(new ColumnModel("field1").INTEGER())
				.addColumn(new ColumnModel("field2").LONG());
		printAllDialectsDDLs(table);
		testOnCurrentRealDatabase(table);
	}

	@Test
	public void testCompondPkey() {// Compound PKEY
		TableModel t = new TableModel("testTable");
		t.column("i4").INTEGER().pkey().notNull().defaultValue("1");
		t.column("l5").LONG().pkey();
		t.column("s6").SHORT();
		printAllDialectsDDLs(t);
		testOnCurrentRealDatabase(t);
	}

	private static TableModel testNotNullModel() {// Not Null
		TableModel t = new TableModel("testTable");
		t.column("b1").BOOLEAN().notNull();
		t.column("d2").DOUBLE().notNull();
		t.column("f3").FLOAT(5).notNull();
		t.column("i4").INTEGER().notNull();
		t.column("l5").LONG().notNull();
		t.column("s6").SHORT().notNull();
		t.column("b7").BIGDECIMAL(10, 2).notNull();
		t.column("s8").STRING(20).notNull();
		t.column("d9").DATE().notNull();
		t.column("t10").TIME().notNull();
		t.column("t11").TIMESTAMP().notNull();
		t.column("v12").VARCHAR(300).notNull();
		return t;
	}

	@Test
	public void testNotNull() {
		printAllDialectsDDLs(testNotNullModel());
		testOnCurrentRealDatabase(testNotNullModel());
	}

	private static TableModel allowNullModel() {// Allow Null
		TableModel t = new TableModel("testTable");
		t.column("b1").BOOLEAN();
		t.column("d2").DOUBLE();
		t.column("f3").FLOAT(5);
		t.column("i4").INTEGER();
		t.column("l5").LONG();
		t.column("s6").SHORT();
		t.column("b7").BIGDECIMAL(10, 2);
		t.column("s8").STRING(20);
		t.column("d9").DATE();
		t.column("t10").TIME();
		t.column("t11").TIMESTAMP();
		t.column("v12").VARCHAR(300);
		return t;
	}

	@Test
	public void testAllowNull() {
		printAllDialectsDDLs(allowNullModel());
		testOnCurrentRealDatabase(allowNullModel());
	}

	private static TableModel uniqueModel() {// unique constraint
		TableModel t = new TableModel("testTable");
		t.column("s1").STRING(20).singleUnique();
		t.column("s2").STRING(20).notNull().singleUnique();

		t.column("s3").STRING(20).singleUnique("uname1");
		t.column("s4").STRING(20).notNull().singleUnique("uname2");

		t.column("s5").STRING(20).singleUnique("A");
		t.column("s6").STRING(20).singleUnique("A");
		t.column("s7").STRING(20).notNull().singleUnique("B");
		t.column("s8").STRING(20).notNull().singleUnique("B");

		t.column("s9").STRING(20).singleUnique("C");
		t.column("s10").STRING(20).singleUnique("D");
		t.column("s11").STRING(20).notNull().singleUnique("E");
		t.column("s12").STRING(20).notNull().singleUnique("F");
		t.unique().columns("S9", "S10");
		t.unique("uk1").columns("s11", "s12");
		t.unique().columns("s5");
		return t;
	}

	@Test
	public void testUnique() {
		printAllDialectsDDLs(uniqueModel());
		testOnCurrentRealDatabase(allowNullModel());
		printOneDialectsDDLs(Dialect.DB2Dialect, uniqueModel());
		printOneDialectsDDLs(Dialect.InformixDialect, uniqueModel());
	}

	private static TableModel checkModel() {// column check
		TableModel t = new TableModel("testTable");
		t.column("s1").STRING(20).notNull().check("s1>5");
		t.column("s2").STRING(20).check("s2>5");
		t.column("s3").STRING(20).notNull().check("s3>5");
		t.column("s4").STRING(20).check("s4>5");
		return t;
	}

	@Test
	public void testCheck() {
		printAllDialectsDDLs(checkModel());
		testOnCurrentRealDatabase(checkModel());
	}

	private static TableModel tableCheckModel() {// table check
		TableModel t = new TableModel("testTable");
		t.check("s2>10");
		t.column("s1").STRING(20).notNull();
		t.column("s2").STRING(20);
		return t;
	}

	@Test
	public void testTableCheck() {
		printAllDialectsDDLs(tableCheckModel());
		testOnCurrentRealDatabase(tableCheckModel());
	}

	private static TableModel IdentityModel() {// Identity
		TableModel t = new TableModel("testTable");
		t.check("s2>10");
		t.column("s1").INTEGER().notNull().identityId().pkey();
		t.column("s2").LONG().check("s2>10");
		t.column("s3").BIGINT();
		return t;
	}

	@Test
	public void testIdentity() {
		if (!guessedDialect.ddlFeatures.getSupportsIdentityColumns())
			return;
		printAllDialectsDDLs(IdentityModel());
		testOnCurrentRealDatabase(IdentityModel());
	}

	@Test
	public void testIdentity2() {
		if (!guessedDialect.ddlFeatures.getSupportsIdentityColumns())
			return;
		printOneDialectsDDLs(Dialect.SybaseASE15Dialect, IdentityModel());
		printOneDialectsDDLs(Dialect.MySQL55Dialect, IdentityModel());
		printOneDialectsDDLs(Dialect.InformixDialect, IdentityModel());
	}

	private static TableModel CommentModel() {// Comment
		TableModel t = new TableModel("testTable").comment("table_comment");
		t.column("s1").INTEGER().notNull().pkey();
		t.column("s2").LONG().comment("column_comment1");
		t.column("s3").BIGINT().comment("column_comment2");
		return t;
	}

	@Test
	public void testComment() {
		printAllDialectsDDLs(CommentModel());
		testOnCurrentRealDatabase(CommentModel());
	}

	@Test
	public void testComment2() {
		printOneDialectsDDLs(Dialect.Ingres10Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.DB2Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.MariaDBDialect, CommentModel());
		printOneDialectsDDLs(Dialect.SQLServer2012Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.MySQL55Dialect, CommentModel());
	}

	private static TableModel SequenceModel() {// SequenceGen
		TableModel t = new TableModel("testTable");
		t.sequenceGenerator("seq1", "seq_1", 1, 1);
		t.sequenceGenerator("seq2", "seq_2", 1, 2);
		t.column("i1").INTEGER().pkey().idGenerator("seq1");
		t.column("i2").INTEGER().pkey().idGenerator("seq2");
		return t;
	}

	@Test
	public void testSequence() {
		printAllDialectsDDLs(SequenceModel());
		if (guessedDialect.ddlFeatures.supportBasicOrPooledSequence())
			testOnCurrentRealDatabase(SequenceModel());
	}

	private static TableModel tableGeneratorModel() {// tableGenerator
		TableModel t = new TableModel("testTable");
		t.tableGenerator("tbgen1", "tb1", "pkcol", "valcol", "pkval", 1, 10);
		t.tableGenerator("tbgen2", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t.column("i1").INTEGER().pkey().idGenerator("tbgen1");
		t.column("i2").INTEGER().pkey().idGenerator("tbgen2");
		return t;
	}

	private static TableModel tableGeneratorModel2() {// tableGenerator
		TableModel t = new TableModel("testTableGeneratorModel2");
		t.tableGenerator("tbgen1", "tb1", "pkcol", "valcol", "pkval", 1, 10);
		t.tableGenerator("tbgen2", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t.tableGenerator("tbgen3", "tb1", "pkcol3", "valcol", "pkval", 1, 10);
		t.tableGenerator("tbgen4", "tb1", "pkcol3", "valcol", "pkval2", 1, 10);
		t.tableGenerator("tbgen5", "tb1", "pkcol4", "valcol", "pkval3", 1, 10);
		t.tableGenerator("tbgen6", "tb1", "pkcol4", "valcol", "pkval4", 1, 10);
		t.column("i1").INTEGER().pkey().idGenerator("tbgen1");
		t.column("i2").INTEGER().pkey().idGenerator("tbgen2");
		return t;
	}

	@Test
	public void testTableGeneratorModel() {
		printAllDialectsDDLs(tableGeneratorModel(), tableGeneratorModel2());
		testOnCurrentRealDatabase(tableGeneratorModel(), tableGeneratorModel2());
	}

	private static TableModel autoGeneratorModel() {// autoGenerator
		TableModel t = new TableModel("testTable1");
		t.column("i1").INTEGER().pkey().autoId();
		t.column("i2").INTEGER().autoId();
		return t;
	}

	private static TableModel autoGeneratorModel2() {// autoGenerator
		TableModel t = new TableModel("testTable2");
		t.tableGenerator("tbgen7", "tb1", "pkcol4", "valcol", "pkval5", 1, 10);
		t.column("i1").INTEGER().pkey().autoId();
		t.column("i2").INTEGER().autoId();
		t.column("i3").INTEGER().autoId();
		return t;
	}

	@Test
	public void testAutoGeneratorModel() {
		printAllDialectsDDLs(autoGeneratorModel(), autoGeneratorModel2(), tableGeneratorModel2());
		printOneDialectsDDLs(Dialect.MySQL5Dialect, autoGeneratorModel(), autoGeneratorModel2(),
				tableGeneratorModel2());
		testOnCurrentRealDatabase(autoGeneratorModel(), autoGeneratorModel2(), tableGeneratorModel2());
	}

	@Test
	public void testFKEY() {// FKEY
	    TableModel t0 = new TableModel("master0");
	    t0.column("address").VARCHAR(20).pkey();
	        
		TableModel t1 = new TableModel("master1");
		t1.column("id").INTEGER().pkey();
		 
		TableModel t2 = new TableModel("master2");
		t2.column("name").VARCHAR(20).pkey();
		t2.column("address").VARCHAR(20).pkey();
		t2.column("fid").INTEGER().singleFKey("master1", "id");
		 
		TableModel t3 = new TableModel("child");
		t3.column("id").INTEGER().pkey();
		t3.column("masterid1").INTEGER().singleFKey("master1", "id").fkeyTail("ON DELETE CASCADE ON UPDATE CASCADE")
				.fkeyName("fknm");
		t3.column("myname").VARCHAR(20).singleFKey("master2", "name").fkeyTail("ON DELETE CASCADE ON UPDATE CASCADE");
		t3.column("myaddress").VARCHAR(20).singleFKey("master0", "address");
		t3.fkey().columns("masterid1").refs("master1", "id").fkeyTail("ON DELETE CASCADE ON UPDATE CASCADE");
		 
		t3.fkey("FKNAME1").columns("myname", "myaddress").refs("master2", "name", "address"); 
		
		TableModel t4 = new TableModel("child2");
		t4.column("id").INTEGER().pkey();
		t4.column("masterid2").INTEGER();
		t4.column("myname2").VARCHAR(20);
		t4.column("myaddress2").VARCHAR(20);
		t4.fkey().columns("masterid2").refs("master1", "id");
		t4.fkey().columns("myname2", "myaddress2").refs("master2", "name", "address");
		System.out.println("================");
		printAllDialectsDDLs(t0, t1, t2, t3);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t0, t1, t2, t3, t4);
		testOnCurrentRealDatabase(t0, t1, t2, t3, t4);
	}

	@Test
	public void testIndex() {// index
		TableModel t = new TableModel("indexTable");
		t.column("s1").STRING(20).singleIndex("aa").singleUnique("bb");
		t.column("s2").STRING(20).singleIndex().singleUnique();
		t.column("s3").STRING(20).singleIndex().singleUnique("cc");
		t.column("s4").STRING(20).singleIndex("dd").singleUnique();
		t.column("s5").STRING(20).singleIndex();
		t.column("s6").STRING(20).singleIndex("ee");
		t.index().columns("s1", "s2");
		t.index("idx1").columns("s5", "s1");
		printAllDialectsDDLs(t);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t);
		testOnCurrentRealDatabase(t);
	}

	@Test
	public void testEngineTailTableTailColumnTail() {// engineTail and tableTail and columnTail test
		TableModel t = new TableModel("tailsTestTable");
		t.engineTail(" DEFAULT CHARSET=utf8");
		t.column("id").STRING(20).pkey();
		t.column("name").STRING(20).tail(" default 'hahaha'");
		printOneDialectsDDLs(Dialect.Oracle10gDialect, t);
		printOneDialectsDDLs(Dialect.H2Dialect, t);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t);
		printOneDialectsDDLs(Dialect.MariaDB53Dialect, t);
		testOnCurrentRealDatabase(t);
		
		Systemout.println("\n\n Set tableTail with empty String to block engine and engineTail");
		t.tableTail("");
		printOneDialectsDDLs(Dialect.Oracle10gDialect, t);
        printOneDialectsDDLs(Dialect.H2Dialect, t);
        printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t);
        printOneDialectsDDLs(Dialect.MariaDB53Dialect, t);
        testOnCurrentRealDatabase(t);
        
        Systemout.println("\n\n Set tableTail to override engine and engineTail and export on all dialect");
        t.tableTail(" engine=innoDB DEFAULT CHARSET=utf8");
        printOneDialectsDDLs(Dialect.Oracle10gDialect, t);
        printOneDialectsDDLs(Dialect.H2Dialect, t);
        printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t);
        printOneDialectsDDLs(Dialect.MariaDB53Dialect, t);
        testOnCurrentRealDatabase(t); 
	}
	 

	@Test
	public void singleXxxMethodTest() {// Test singleXxx methods
		TableModel t1 = new TableModel("customers");
		t1.column("name").STRING(20).singleUnique();
		t1.column("email").VARCHAR(50).defaultValue("'Beijing'").comment("address comment").singleIndex("IDX1");
		TableModel t2 = new TableModel("orders");
		t2.column("item").STRING(20).singleUnique("A");
		t2.column("name").STRING(20).singleFKey("customers", "name");

		String[] dropAndCreateDDL = Dialect.H2Dialect.toDropAndCreateDDL(t1, t2);
		for (String ddl : dropAndCreateDDL)
			Systemout.println(ddl);
		testOnCurrentRealDatabase(t1, t2);
	}

	@Test
	public void sampleTest() {// An example used to put on README.md
		TableModel t1 = new TableModel("customers");
		t1.column("name").STRING(20).pkey();
		t1.column("email").STRING(20).pkey().entityField("email").updatable(true).insertable(false);
		t1.column("address").VARCHAR(50).defaultValue("'Beijing'").comment("address comment");
		t1.column("phoneNumber").VARCHAR(50).singleIndex("IDX2");
		t1.column("age").INTEGER().notNull().check("'>0'");
		t1.index("idx3").columns("address", "phoneNumber").unique();
		t1.column("aaaaaaaaaaaaa").NUMERIC(5,2);
		t1.column("bbbbbbbbbbb").DECIMAL(5, 2);

		TableModel t2 = new TableModel("orders").comment("order comment");
		t2.column("id").LONG().autoId().pkey();
		t2.column("name").STRING(20);
		t2.column("email").STRING(20);
		t2.column("name2").STRING(20).pkey().tail(" default 'Sam'");
		t2.column("email2").STRING(20);
		t2.fkey().columns("name2", "email2").refs("customers", "name", "email");
		t2.fkey("fk1").columns("name", "email").refs("customers", "name", "email");
		t2.unique("uk1").columns("name2", "email2");

		TableModel t3 = new TableModel("sampletable");
		t3.column("id").LONG().pkey();
		if (guessedDialect.ddlFeatures.getSupportsIdentityColumns())
			t3.column("id").identityId();
		t3.tableGenerator("table_gen1", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t3.column("id1").INTEGER().idGenerator("table_gen1");
		if (guessedDialect.ddlFeatures.supportBasicOrPooledSequence())
			t3.sequenceGenerator("seq1", "seq_1", 1, 1);
		t3.column("id2").INTEGER().idGenerator("seq1");
		t3.engineTail(" DEFAULT CHARSET=utf8");

		String[] dropAndCreateDDL =guessedDialect.toDropAndCreateDDL(t1, t2, t3);
		for (String ddl : dropAndCreateDDL)
			Systemout.println(ddl);

		testOnCurrentRealDatabase(t1, t2, t3);
	}
	
	@Test
    public void testNumericAndDecimal() {
        DbContext db = new DbContext(ds);
        db.setAllowShowSQL(true);
        TableModel t1 = new TableModel("test");
        t1.column("name").STRING(20).pkey();
        t1.column("a").NUMERIC(4, 2);
        t1.column("b").DECIMAL(4, 2);
        db.quiteExecute(db.toDropAndCreateDDL(t1));
        db.exe("insert into test (name,a, b) values('a', 12.345, 12.345)");
        Systemout.println("a=" + db.qryObject("select a from test"));
        Systemout.println("b=" + db.qryObject("select b from test"));
    }
	
    @Test
    public void testAddDropColumnsAtRuntime() {//动态添加删除列
        DbContext db = new DbContext(ds);
        Dialect dialect = db.getDialect();
        db.setAllowShowSQL(true);
        TableModel t1 = new TableModel("tb_test");
        t1.column("id").INTEGER().pkey();
        db.quiteExecute(db.toDropAndCreateDDL(t1));

        db.exe("insert into tb_test (id) values(?)", DB.par(1));
        Systemout.println("此时只有1列=" + db.qryMapList("select * from tb_test")); 

        
        ColumnModel name = new ColumnModel("name").STRING(10).comment("新增的列");//column可以单独创建，再设定它的tableModel
        name.setTableModel(t1); 
        String[] add = dialect.toAddColumnDDL(name, t1.column("age").INTEGER(), t1.column("price").DOUBLE());
        db.executeDDL(add); //新增三个列
        db.exe("insert into tb_test (id, name, age, price)", DB.par(2, "tom", 5, 100.0), DB.VQ);
        //注意DDL动态改表后，某些数据池可能会出错，要关闭cachePrepStmts，或使用不在缓存中的SQL，如下在每个SQL中加了空格以避免与缓存中的SQL相同  
        Systemout.println("此时有4列 =" + db.qryMapList("select * from tb_test ") );  
        
        db.executeDDL(dialect.toDropColumnDDL(t1.column("name"), t1.column("age"))); //删除两个列
        Systemout.println("此时有2列=" + db.qryMapList("select * from tb_test  ")); //此时有2列
        
        //改名和改类型目前不能直接做到，只能先新新一列，再用SQL拷贝，然后再删除旧列
        db.executeDDL(dialect.toAddColumnDDL(t1.column("new_price").DECIMAL(5, 2)));
        db.exe("update tb_test set new_price=price");
        db.executeDDL(dialect.toDropColumnDDL(t1.column("price")));
        Systemout.println("此时price被改名成了new_price=" + db.qryMapList("select * from tb_test   ")); //此时price被改名成了new_price
        
    }
	   
}