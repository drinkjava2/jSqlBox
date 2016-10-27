package test.test3_order.po;

import com.github.drinkjava2.jsqlbox.Dao;

public class Order {
	// ==== have to copy below Dao methods in each entity class:=====
	private Dao dao;

	public void putDao(Dao dao) {
		this.dao = dao;
	}

	public Dao dao() {
		if (dao == null)
			dao = Dao.defaultDao(this);
		return dao;
	}
	// ============= Dao code end =========

	public static final String Table = "user";

}