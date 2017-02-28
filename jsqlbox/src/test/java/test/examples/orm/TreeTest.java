package test.examples.orm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.AopAround;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.JBeanBoxConfig.SpringTxInterceptorBox;
import test.config.PrepareTestContext;
import test.examples.orm.entities.TreeNode;

public class TreeTest {

	@Before
	public void setup() {
		System.out.println("===============================Testing ORMDemo===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Dao.getDefaultContext().setShowSql(true).setShowQueryResult(true);
		Dao.executeQuiet("DROP TABLE treetest");
		Dao.execute(TreeNode.CREATE_SQL);
		Dao.refreshMetaData();
		Dao.execute("insert into treetest (id,comments,pid) values('A','I found a bug',null) ");
		Dao.execute("insert into treetest (id,comments,pid) values('B','is a worm','A') ");
		Dao.execute("insert into treetest (id,comments,pid) values('C','no','A')");
		Dao.execute("insert into treetest (id,comments,pid) values('D','is a bug','A')");
		Dao.execute("insert into treetest (id,comments,pid) values('E','Oh, a bug','B')");
		Dao.execute("insert into treetest (id,comments,pid) values('F','Need solve it','B')");
		Dao.execute("insert into treetest (id,comments,pid) values('G','Careful it bites','C')");
		Dao.execute("insert into treetest (id,comments,pid) values('H','It does not bite','D')");
		Dao.execute("insert into treetest (id,comments,pid) values('I','Found the reason','D')");
		Dao.execute("insert into treetest (id,comments,pid) values('J','Solved','H')");
		Dao.execute("insert into treetest (id,comments,pid) values('K','Updated','H')");
		Dao.execute("insert into treetest (id,comments,pid) values('L','Well done!','H')");

		doSortTree();
	}

	@AopAround(SpringTxInterceptorBox.class)
	public void sortTree() {
		Dao.execute("set @mycnt=0");
		Dao.execute("update treetest set  line=0,level=0, tempno=0, temporder=(@mycnt := @mycnt + 1) order by id");
		Dao.execute("update treetest set level=1, line=1 where pid is null");

		int level = 1;
		while (Dao.queryForInteger("select count(*) from treetest where line=0 ") > 0) {
			Dao.execute("update tb3 set tempno=line*100000 where line>0");
			Dao.execute("update tb3 a, tb3 b set a.level=" + (level + 1)
					+ ", a.tempno=b.tempno+a.temporder where a.level=0 and a.pid=b.id and b.level=" + level);
			Dao.execute("set @mycnt=0");
			Dao.execute("update tb3 set line=(@mycnt := @mycnt + 1) where level>0 order by tempno");
			level++; 
		}
	}

	public static void doSortTree() {
		TreeTest t = BeanBox.getBean(TreeTest.class);
		t.sortTree();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	@Test
	public void treeTest() {
	}

}