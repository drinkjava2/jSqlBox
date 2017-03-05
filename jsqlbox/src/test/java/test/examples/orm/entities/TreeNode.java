package test.examples.orm.entities;

import java.util.Set;

import com.github.drinkjava2.jsqlbox.Entity;

public class TreeNode implements Entity {
	public static final String CREATE_SQL = "create table treetest (  "//
			+ "id varchar(32),  "//
			+ "comments varchar(55), "//
			+ "pid varchar(32),   "//
			+ "line integer,   "//
			+ "level integer,   "//
			+ "tempno integer, "//
			+ "temporder integer  "//
			+ ") ";

	String id;
	String comments;
	String pid;
	Integer line;
	Integer level;
	Integer tempno;
	Integer temporder;
	TreeNode parent;
	Set<TreeNode> childs;

	{
		this.box().configTable("treetest");
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

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getTempno() {
		return tempno;
	}

	public void setTempno(Integer tempno) {
		this.tempno = tempno;
	}

	public Integer getTemporder() {
		return temporder;
	}

	public void setTemporder(Integer temporder) {
		this.temporder = temporder;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public Set<TreeNode> getChilds() {
		return childs;
	}

	public void setChilds(Set<TreeNode> childs) {
		this.childs = childs;
	}

	public String ID() {
		return box().getColumnName("id");
	}

	public String PID() {
		return box().getColumnName("pid");
	}

	public String LINE() {
		return box().getColumnName("line");
	}

	public String LEVEL() {
		return box().getColumnName("level");
	}

	public String PARENT() {
		return box().getColumnName("parent");
	}

	public String CHILDS() {
		return box().getColumnName("childs");
	}

}