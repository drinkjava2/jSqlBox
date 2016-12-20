package com.github.drinkjava2.jsqlbox.id;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Define a Sequence type ID generator, supported by Oracle, postgreSQL, DB2
 * 
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */

public class SequenceGenerator implements IdGenerator {

	String sequenceName = "";

	public SequenceGenerator() {
		// Default constructor
	}

	public SequenceGenerator(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	@Override
	public Object getNextID(SqlBoxContext ctx) {
		return ctx.getJdbc().queryForObject("select " + sequenceName + ".nextval from dual", Integer.class);
	}

	// Getter & Setters below

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

}
