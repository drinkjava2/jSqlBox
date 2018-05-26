package com.github.drinkjava2.functionstest.entitynet.entities;

import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "treenodetb")
public class TreeNode extends ActiveRecord {
	@Id
	String id;

	@SingleFKey(ddl = false, refs = { "treenodetb", "id" })
	String pid;

	String comments;
	Integer line;
	Integer lvl;

	public TreeNode() {
		// default public constructor
	}

	public TreeNode(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getLvl() {
		return lvl;
	}

	public void setLvl(Integer lvl) {
		this.lvl = lvl;
	}

}