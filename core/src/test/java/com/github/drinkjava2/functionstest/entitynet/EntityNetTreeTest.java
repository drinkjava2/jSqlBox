package com.github.drinkjava2.functionstest.entitynet;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.alias;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.give;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.functionstest.entitynet.entities.TreeNode;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

public class EntityNetTreeTest extends TestBase {
	@Before
	public void init() {// See "orm.png" in root folder
		super.init();
		createAndRegTables(TreeNode.class);
		TreeNode t = new TreeNode();
		t.putFields("id", "comments", "pid", "line", "lvl");
		t.putValues("A", "found a bug", null, 1, 1).insert();
		t.putValues("B", "is a worm?", "A", 2, 2).insert();
		t.putValues("E", "no", "B", 3, 3).insert();
		t.putValues("F", "is a bug", "B", 4, 3).insert();
		t.putValues("C", "oh, a bug", "A", 5, 2).insert();
		t.putValues("G", "need solve it", "C", 6, 3).insert();
		t.putValues("D", "careful it bites", "A", 7, 2).insert();
		t.putValues("H", "it does not bite", "D", 8, 3).insert();
		t.putValues("J", "found the reason", "H", 9, 4).insert();
		t.putValues("K", "solved", "H", 10, 4).insert();
		t.putValues("L", "uploaded", "H", 11, 4).insert();
		t.putValues("I", "well done!", "D", 12, 3).insert();
		t.putValues("END", "end tag", null, 13, 0).insert();
		System.out.println();
	}

	private static final Object[] targets = new Object[] { new EntityNetHandler(), TreeNode.class, TreeNode.class,
			alias("a", "b"), give("b", "a", "parent"), give("a", "b", "childs") };

	@Test
	public void testSearchTreeChild() {
		EntityNet net = ctx.iQuery(targets, "select a.**, a.pid as b_id from treenodetb a");
		TreeNode node = net.pickOneEntity("a", "A");
		printTree(node, 0);

		System.out.println("====================");
		node = net.pickOneEntity("a", "D");
		printTree(node, 0);
	}

	@Test
	public void subTreeSearch() {// see https://my.oschina.net/drinkjava2/blog/181863
		System.out.println("====================");
		TreeNode d = new TreeNode().loadById("D");
		loadSubTree(d);
	}

	/**
	 * Use one SQL to load a whole child tree,see
	 * https://my.oschina.net/drinkjava2/blog/1818631
	 */
	public void loadSubTree(TreeNode d) {
		EntityNet net = ctx.pQuery(targets,
				"select a.**, a.pid as b_id from treenodetb a where a.line>=? and a.line< (select min(line) from treenodetb where line>? and lvl<=?) ",
				d.getLine(), d.getLine(), d.getLvl());
		TreeNode node = net.pickOneEntity("a", d.getId());
		printTree(node, 0);
	}

	private static void printTree(TreeNode node, int space) {
		space++;
		for (int i = 1; i < space; i++)
			System.out.print("  ");
		System.out.println(node.getId() + (node.getParent() != null ? "  Parent:" + node.getParent().getId() : " "));
		if (node.getChilds() != null)
			for (TreeNode c : node.getChilds())
				printTree(c, space);
		space--;
	}

}