package com.github.drinkjava2.functionstest.entitynet;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.alias;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.give;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.functionstest.entitynet.entities.TreeNode;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

public class EntityNetTreeTest extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		createAndRegTables(TreeNode.class);
		new TreeNode().putFields("id", "comments", "pid", "line", "lvl");
		new TreeNode().putValues("A", "found a bug", null, 1, 1).insert();
		new TreeNode().putValues("B", "is a worm?", "A", 2, 2).insert();
		new TreeNode().putValues("E", "no", "B", 3, 3).insert();
		new TreeNode().putValues("F", "is a bug", "B", 4, 3).insert();
		new TreeNode().putValues("C", "oh, a bug", "A", 5, 2).insert();
		new TreeNode().putValues("G", "need solve it", "C", 6, 3).insert();
		new TreeNode().putValues("D", "careful it bites", "A", 7, 2).insert();
		new TreeNode().putValues("H", "it does not bite", "D", 8, 3).insert();
		new TreeNode().putValues("J", "found the reason", "H", 9, 4).insert();
		new TreeNode().putValues("K", "solved", "H", 10, 4).insert();
		new TreeNode().putValues("L", "uploaded", "H", 11, 4).insert();
		new TreeNode().putValues("I", "well done!", "D", 12, 3).insert();
		new TreeNode().putValues("END", "end tag", null, 13, 0).insert();
		System.out.println();
	}

	@Test
	public void testSearchTreeChild() {
		EntityNet net = ctx.iQuery(new EntityNetHandler(), TreeNode.class, TreeNode.class, alias("a", "b"),
				give("b", "a", "parent"), give("a", "b", "childs"),
				"select a.**, a.pid as b_id from treenodetb a,  treenodetb b");
		List<TreeNode> nodes = net.pickEntityList("a");
		System.out.println(nodes);
	}

}