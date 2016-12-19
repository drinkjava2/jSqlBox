package test.config.po;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;

/**
 * This entity class should automatically created by a code generator tool
 * 
 */
public class EntityBase {
	// ==== have to copy below methods in each entity class:=====
	private Dao dao;

	public void putDao(Dao dao) {
		this.dao = dao;
	}

	public Dao dao() {
		return Dao.getDao(this, dao);
	}

	public String table() {
		return box().getRealTable();
	}

	public SqlBox box() {
		return dao().getBox();
	}

	public void insert() {
		this.dao().insert();
	}

	public void update() {
		this.dao().update();
	}

	public void delete() {
		this.dao().update();
	}

}