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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.examples.orm.entities.TreeNode;
import test.utils.tinyjdbc.TinyJdbc;

/**
 * To Test Tree ORM, currently only support MySQL because sortMySqlTree() method
 * used some special function of MySQL, need in jBeanBoxConfig.java change below
 * line: <br/>
 * public static class DataSourceBox extends H2DataSourceBox <br/>
 * to: <br/>
 * public static class DataSourceBox extends MySqlDataSourceBox <br/>
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TreeORMTest extends TestBase {

	@Before
	public void setup() {
		super.setup();
		System.out.println(
				" !!!Note: Only run on MySql, need set  DataSourceBox extends MySqlDataSourceBox in jBeanBoxConfig.java ");
		if (!Dao.getDialect().isMySqlFamily())
			return;
		// Dao.getDefaultContext().setShowSql(true).setShowQueryResult(true);
		Dao.executeQuiet("drop table if exists treetest");
		Dao.execute(TreeNode.CREATE_SQL + " ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		Dao.refreshMetaData();
		Dao.execute("insert into treetest (id,comments,Pid) values('A','found a bug',null)");
		Dao.execute("insert into treetest (id,comments,Pid) values('B','is a worm?','A')");
		Dao.execute("insert into treetest (id,comments,Pid) values('E','no','B')");
		Dao.execute("insert into treetest (id,comments,Pid) values('F','is a bug','B')");
		Dao.execute("insert into treetest (id,comments,Pid) values('C','oh, a bug','A')");
		Dao.execute("insert into treetest (id,comments,Pid) values('G','need solve it','C')");
		Dao.execute("insert into treetest (id,comments,Pid) values('D','careful it bites','A')");
		Dao.execute("insert into treetest (id,comments,Pid) values('H','it does not bite','D')");
		Dao.execute("insert into treetest (id,comments,Pid) values('J','found the reason','H')");
		Dao.execute("insert into treetest (id,comments,Pid) values('K','solved','H')");
		Dao.execute("insert into treetest (id,comments,Pid) values('L','uploaded','H')");
		Dao.execute("insert into treetest (id,comments,Pid) values('I','well done!','D')");
	}

	/**
	 * A common method to sort "Adjacency List" tree to
	 * "Sorted-Unlimited-Depth-Tree", only for MySql
	 */
	public void sortMySqlTree() {
		Dao.execute("delete from treetest where id='END'");
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
		int count = Dao.queryForInteger("select count(*) from treetest");
		Dao.execute("insert into treetest (id,comments,Pid,line,level) values('END','End Tag',null," + ++count + ",0)");
	}

	public void executeJDBC(String sql) {
		DataSource ds = Dao.getDefaultContext().getDataSource();
		TinyJdbc.execute(ds, TinyJdbc.TRANSACTION_READ_COMMITTED, sql, new Object[] {});
	}

	/**
	 * In this test move whole "D" child tree to under "C" node and before "G"
	 * Node <br/>
	 * More detail of "Sorted-Unlimited-Depth-Tree" can see
	 * https://github.com/drinkjava2/Multiple-Columns-Tree <br/>
	 * "Sorted-Unlimited-Depth-Tree" has better query performance than
	 * "Adjacency List" mode
	 */
	@Test
	public void moveNodeTest() {
		if (!Dao.getDialect().isMySqlFamily())
			return;
		System.out.println("============moveNodeTest=========");
		TreeNode d = Dao.load(TreeNode.class, "D");
		d.setPid("C");// move whole "D" sub-Tree to "C"
		d.update();
		sortMySqlTree(); // Important!, transfer Adjacency List to
							// Sorted-Unlimited-Depth-Tree

		TreeNode c = Dao.load(TreeNode.class, "C");
		Assert.assertEquals("C", c.getId());
		c.configAlias("c");
		c.configMapping(tree(), use(c.ID(), c.PID()), bind());

		List<TreeNode> c_childtree = loadChildTree(c);
		TreeNode croot = c_childtree.get(0);
		Assert.assertEquals("C", croot.getId());
		printUnbindedChildNode(croot);
	}

	/**
	 * Return child tree include the node itself
	 */
	private List<TreeNode> loadChildTree(TreeNode n) {
		List<TreeNode> childtree = Dao.queryForEntityList(TreeNode.class, select(), n.all(), from(), n.table(),
				" where line>=" + n.getLine() + " and line< (select min(line) from ", n.table(), " where line>",
				q(n.getLine()), " and level<= ", q(n.getLevel()), ") order by ", n.LINE());
		return childtree;
	}

	/**
	 * This test show the tree mapping configuration, there are 2 configuration
	 * ways <br/>
	 * 1. use entity.configMapping() method <br/>
	 * 2. write config mapping direct in SQL <br/>
	 * Here show the 1st way
	 */
	@Test
	public void treeNoBindTest() {
		if (!Dao.getDialect().isMySqlFamily())
			return;
		System.out.println("============treeNoBindTest=========");
		sortMySqlTree();
		TreeNode t = new TreeNode().configAlias("t");
		t.configMapping(tree(), use(t.ID(), t.PID()), bind());
		List<TreeNode> childNodes = Dao.queryForEntityList(TreeNode.class, select(), t.all(), from(), t.table(),
				" where level>0 order by ", t.LINE());
		TreeNode root = childNodes.get(0);
		Assert.assertEquals("A", root.getId());
		Set<TreeNode> childs = root.getChildNodeSet(TreeNode.class);
		Assert.assertEquals(root, childs.iterator().next().getParentNode(TreeNode.class));
		printUnbindedChildNode(root);
	}

	private static int spaceCount = 0;

	public static void printUnbindedChildNode(TreeNode node) {
		spaceCount++;
		if (node != null) {
			System.out.println(generateSpace(spaceCount) + node.getId() + "=" + node.getComments());
			Set<TreeNode> nodes = node.getChildNodeSet(TreeNode.class);
			if (nodes != null)
				for (TreeNode n : nodes)
					printUnbindedChildNode(n);
		}
		spaceCount--;
	}

	/**
	 * This test show how to bind the CHILDS and PARENT property and write
	 * mapping configuration in SQL
	 */
	@Test
	public void treeWithBindTest() {
		if (!Dao.getDialect().isMySqlFamily())
			return;
		System.out.println("============treeWithBindTest=========");
		sortMySqlTree();
		TreeNode t = new TreeNode().configAlias("t");
		t.configMapping(tree(), use(t.ID(), t.PID()), bind(t.CHILDS(), t.PARENT()));
		List<TreeNode> childNodes = Dao.queryForEntityList(TreeNode.class, select(), t.all(), from(), t.table(),
				" where level>0 order by ", t.LINE());
		TreeNode root = childNodes.get(0);
		Assert.assertEquals("A", root.getId());
		Assert.assertEquals(root, root.getChilds().iterator().next().getParent());
		printBindedChildNode(root);
	}

	/**
	 * Print the tree
	 */
	public static void printBindedChildNode(TreeNode node) {
		spaceCount++;
		if (node != null) {
			System.out.println(generateSpace(spaceCount) + node.getId() + "=" + node.getComments());
			Set<TreeNode> nodes = node.getChilds();
			if (nodes != null)
				for (TreeNode n : nodes)
					printBindedChildNode(n);
		}
		spaceCount--;
	}

	/**
	 * Build spaces
	 */
	public static String generateSpace(int count) {
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++)
			sb.append(' ');
		return sb.toString();
	}
}