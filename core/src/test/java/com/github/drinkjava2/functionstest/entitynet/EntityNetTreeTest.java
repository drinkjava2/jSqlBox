package com.github.drinkjava2.functionstest.entitynet;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.functionstest.entitynet.entities.TreeNode;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.entitynet.Path;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

public class EntityNetTreeTest extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		TableModel[] models = TableModelUtils.entity2Models(TreeNode.class);
		createAndRegTables(models);
		new TreeNode().putFields("id", "comments", "pid", "line", "lvl");
		new TreeNode().putValues("A", "found a bug", null, 1, 1).insert();
		new TreeNode().putValues("B", "is a worm?", "A", 2,2).insert();
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
		EntityNet net = ctx.netLoad(TreeNode.class);
		Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class,
				new Path("S+", TreeNode.class).where("id=? or id=?", "B", "D").nextPath("C*", TreeNode.class, "pid"));
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		Assert.assertEquals(9, TreeNodes.size());
	}

	@Test
	public void testSearchTreeChild2() {
		EntityNet net = ctx.netLoad(TreeNode.class);
		Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class, new Path("C*", TreeNode.class, "pid"),
				new TreeNode("B"), new TreeNode("D"));
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		Assert.assertEquals(7, TreeNodes.size());
	}

	@Test
	public void testSearchTreeParent() {
		EntityNet net = ctx.netLoad(TreeNode.class);
		Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class,
				new Path("S-", TreeNode.class).where("id='F' or id='K'").nextPath("P*", TreeNode.class, "pid"));
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		Assert.assertEquals(4, TreeNodes.size());
	}
	
	@Test
	public void subTreeSearch() {//see https://my.oschina.net/drinkjava2/blog/1818631 
		EntityNet net = ctx.pQuery(new EntityNetHandler(TreeNode.class),
				"select t.** from treenodetb t where t.line>=? and t.line< (select min(line) from treenodetb where line>? and lvl<=?)",7,7,2);
		List<TreeNode> TreeNodes = net.getAllEntityList(TreeNode.class); 
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		Assert.assertEquals(6, TreeNodes.size());
	}

}