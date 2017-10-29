package functiontest.orm.entities;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "treenodetb")
public class TreeNode extends ActiveRecord {
	@Id
	String id;
	String comments;
	String pid;
	Integer line;
	Integer level;
	Integer tempno;
	Integer temporder;

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
}