package com.github.drinkjava2.jsqlbox.function.entitynet;

import static com.github.drinkjava2.jsqlbox.DB.alias;
import static com.github.drinkjava2.jsqlbox.DB.give;
import static com.github.drinkjava2.jsqlbox.DB.par;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.TreeNode;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

public class EntityNetTreeTest extends TestBase {
	@Before
	public void init() {// See "orm.png" in jSqlBox project's root folder
		super.init();
		createAndRegTables(TreeNode.class);
		TreeNode t = new TreeNode();
		t.forFields("id", "comments", "pid", "line", "lvl");
		t.putValues("A", "found a bug", null, 1, 1).insert();
		t.putValues("B", "is a worm?", "A", 2, 2).insert();
		t.putValues("E", "no", "B", 3, 3).insert();
		t.putValues("F", "is a bug", "B", 4, 3).insert();
		t.putValues("t", "oh, a bug", "A", 5, 2).insert();
		t.putValues("G", "need solve it", "t", 6, 3).insert();
		t.putValues("D", "careful it bites", "A", 7, 2).insert();
		t.putValues("H", "it does not bite", "D", 8, 3).insert();
		t.putValues("J", "found the reason", "H", 9, 4).insert();
		t.putValues("K", "solved", "H", 10, 4).insert();
		t.putValues("L", "uploaded", "H", 11, 4).insert();
		t.putValues("I", "well done!", "D", 12, 3).insert();
		t.putValues("END", "end tag", null, 13, 0).insert();
		Systemout.println();
	}

	private static final Object[] targets = new Object[] { new EntityNetHandler(), TreeNode.class, TreeNode.class,
			alias("t", "p"), give("p", "t", "parent"), give("t", "p", "childs") };

	@Test
	public void testSearchTreeChild() {
		EntityNet net = ctx.qry(targets, "select t.**, t.pid as p_id from treenodetb t");
		TreeNode node = net.pickOneEntity("t", "A");
		printTree(node, 0);

		Systemout.println("====================");
		node = net.pickOneEntity("t", "D");
		printTree(node, 0);
	}

	@Test
	public void subTreeSearch() {// see https://my.oschina.net/drinkjava2/blog/181863
		Systemout.println("==========Sub trea load==========");
		TreeNode d = new TreeNode().loadById("D");
		loadSubTreeByGivenNode(d);
	}

	/**
	 * Use one SQL to load a whole sub-tree,see
	 * https://my.oschina.net/drinkjava2/blog/1818631 (海底捞算法 )
	 */
	public void loadSubTreeByGivenNode(TreeNode d) {
		EntityNet net = ctx.qry(targets,
				"select t.**, t.pid as p_id from treenodetb t where t.line>=? and t.line< (select min(line) from treenodetb where line>? and lvl<=?) ",
				par(d.getLine(), d.getLine(), d.getLvl()));
		TreeNode node = net.pickOneEntity("t", d.getId());
		printTree(node, 0);
	}

	private static void printTree(TreeNode node, int space) {
		space++;
		for (int i = 1; i < space; i++)
			Systemout.print("  ");
		Systemout.println(node.getId() + (node.getParent() != null ? "  Parent:" + node.getParent().getId() : " "));
		if (node.getChilds() != null)
			for (TreeNode c : node.getChilds())
				printTree(c, space);
		space--;
	}

}