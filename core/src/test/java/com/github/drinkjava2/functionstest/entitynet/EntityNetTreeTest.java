package com.github.drinkjava2.functionstest.entitynet;

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

public class EntityNetTreeTest extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		TableModel[] models = TableModelUtils.entity2Models(TreeNode.class);
		dropAndCreateDatabase(models); 
		new TreeNode().putFields("id", "comments", "pid");
		new TreeNode().putValues("A", "found a bug", null).insert();
		new TreeNode().putValues("B", "is a worm?", "A").insert();
		new TreeNode().putValues("E", "no", "B").insert();
		new TreeNode().putValues("F", "is a bug", "B").insert();
		new TreeNode().putValues("C", "oh, a bug", "A").insert();
		new TreeNode().putValues("G", "need solve it", "C").insert();
		new TreeNode().putValues("D", "careful it bites", "A").insert();
		new TreeNode().putValues("H", "it does not bite", "D").insert();
		new TreeNode().putValues("J", "found the reason", "H").insert();
		new TreeNode().putValues("K", "solved", "H").insert();
		new TreeNode().putValues("L", "uploaded", "H").insert();
		new TreeNode().putValues("I", "well done!", "D").insert();
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

}