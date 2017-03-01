package test.examples.orm;

import static com.github.drinkjava2.jsqlbox.MappingHelper.bind;
import static com.github.drinkjava2.jsqlbox.MappingHelper.tree;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.q;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;
import static com.github.drinkjava2.jsqlbox.SqlHelper.use;

import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.examples.orm.entities.TreeNode;
import test.utils.tinyjdbc.TinyJdbc;

public class TreeTest {

	@Before
	public void setup() {
		System.out.println("===============================Testing ORMDemo===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		// Dao.getDefaultContext().setShowSql(true).setShowQueryResult(true);
		Dao.executeQuiet("drop table if exists treetest");
		Dao.execute(TreeNode.CREATE_SQL);
		Dao.refreshMetaData();
		Dao.execute("insert into treetest (id,comments,Pid) values('A','found a bug',null)");
		Dao.execute("insert into treetest (id,comments,Pid) values('B','is a worm','A')");
		Dao.execute("insert into treetest (id,comments,Pid) values('E','no','B')");
		Dao.execute("insert into treetest (id,comments,Pid) values('F','is a bug','B')");
		Dao.execute("insert into treetest (id,comments,Pid) values('C','oh, a bug','A')");
		Dao.execute("insert into treetest (id,comments,Pid) values('G','solve it','C')");
		Dao.execute("insert into treetest (id,comments,Pid) values('D','careful it bites','A')");
		Dao.execute("insert into treetest (id,comments,Pid) values('H','it does not bit','D')");
		Dao.execute("insert into treetest (id,comments,Pid) values('J','found the reason','H')");
		Dao.execute("insert into treetest (id,comments,Pid) values('K','solved','H')");
		Dao.execute("insert into treetest (id,comments,Pid) values('L','uploaded','H')");
		Dao.execute("insert into treetest (id,comments,Pid) values('I','well done!','D')");
	}

	public void sortMySqlTree() {
		executeJDBC("set @mycnt=0");
		executeJDBC("update treetest set line=0,level=0, tempno=0, temporder=(@mycnt := @mycnt + 1) order by id");
		executeJDBC("update treetest set level=1, line=1 where pid is null");
		int level = 1;
		while (Dao.queryForInteger("select line from treetest where line=0 limit 1") != null) {
			executeJDBC("update treetest set tempno=line*100000 where line>0 ");
			executeJDBC("update treetest a, treetest b set a.level=" + (level + 1)
					+ ", a.tempno=b.tempno+a.temporder where a.level=0 and a.pid=b.id and b.level=" + level);
			executeJDBC("set @mycnt=0");
			executeJDBC("update treetest set line=(@mycnt := @mycnt + 1) where level>0 order by tempno");
			level++;
		}
		int count = Dao.queryForInteger("select count(*) from treetest") + 1;
		Dao.execute("insert into treetest (id,comments,Pid,line,level) values('END','End Tag',null," + count + ",0)");
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	public void executeJDBC(String sql) {
		DataSource ds = Dao.getDefaultContext().getDataSource();
		TinyJdbc.execute(ds, TinyJdbc.TRANSACTION_READ_COMMITTED, sql, new Object[] {});
	}

	/**
	 * Detail of "Sorted-Unlimited-Depth-Tree" see https://github.com/drinkjava2/Multiple-Columns-Tree
	 */
	public void sortedUnlimitedDepthTreeTest() {
		sortMySqlTree();
		TreeNode t = Dao.load(TreeNode.class, "D");
		t.configAlias("t");
		List<TreeNode> childNodes = Dao.queryForEntityList(TreeNode.class, select(), t.all(), from(), t.table(),
				" where line>=" + t.getLine() + " and line< (select min(line) from ", t.table(), " where line>",
				q(t.getLine()), " and level<= ", q(t.getLevel()), ")");
		for (TreeNode node : childNodes) {
			System.out.println(generateSpace(node.getLevel()) + node.getId() + "\t" + node.getComments());
		}
		Assert.assertTrue(childNodes.size() == 6);
	}

	@Test
	public void buildObjectTreeTest() {
		sortMySqlTree();
		TreeNode t = new TreeNode().configAlias("t");
		List<TreeNode> childNodes = Dao.queryForEntityList(TreeNode.class, select(), t.all(), from(), t.table(),
				" where level>0 order by ", t.LINE(), tree(), use(t.PID(), t.ID()), bind());
		TreeNode root = childNodes.get(2);
		TreeNode b = (TreeNode) root.box().getEntityCache().get(TreeNode.class).get("B");
		TreeNode c=b.getParentNode(TreeNode.class);
		System.out.println(c.getId());
	}

	public static String generateSpace(int count) {
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++)
			sb.append(' ');
		return sb.toString();
	}

}