package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class TreeNode implements Entity {
	public static final String CREATE_SQL = "create table treetest (  "//
			+ "id varchar(32),  "//
			+ "comments varchar(55), "//
			+ "pid varchar(32),   "//
			+ "line integer,   "//
			+ "level integer,   "//
			+ "tempno integer, "//
			+ "  temporder integer  "//
			+ ") ";

	String id;
	String comments;
	String pid;
	Integer line;
	Integer tempno;
	Integer temporder;
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

}