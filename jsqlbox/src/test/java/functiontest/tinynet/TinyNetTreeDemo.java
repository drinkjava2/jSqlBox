package functiontest.tinynet;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.ModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jtinynet.Path;
import com.github.drinkjava2.jtinynet.TinyNet;

import config.TestBase;
import functiontest.tinynet.entities.TreeNode;

public class TinyNetTreeDemo extends TestBase {
	@Before
	public void init() {
		super.init();
		// ctx.setAllowShowSQL(true);
		TableModel[] models = ModelUtils.entity2Model(TreeNode.class);
		dropAndCreateDatabase(models);
		ctx.refreshMetaData();
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
		TinyNet net = ctx.netLoad(TreeNode.class);
		Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class,
				new Path("S+", TreeNode.class).where("id=? or id=?", "B", "D").nextPath("C*", TreeNode.class, "pid"));
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		Assert.assertEquals(9, TreeNodes.size());
	}

	@Test
	public void testSearchTreeChild2() {
		TinyNet net = ctx.netLoad(TreeNode.class);
		Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class, new Path("C*", TreeNode.class, "pid"),
				new TreeNode("B"), new TreeNode("D"));
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		Assert.assertEquals(7, TreeNodes.size());
	}

	@Test
	public void testSearchTreeParent() {
		TinyNet net = ctx.netLoad(TreeNode.class);
		Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class,
				new Path("S-", TreeNode.class).where("id='F' or id='K'").nextPath("P*", TreeNode.class, "pid"));
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		Assert.assertEquals(4, TreeNodes.size());
	}

}