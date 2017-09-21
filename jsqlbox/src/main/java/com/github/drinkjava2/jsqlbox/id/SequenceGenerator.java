package com.github.drinkjava2.jsqlbox.id;

import com.github.drinkjava2.jdialects.utils.StrUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

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
		SqlBoxException.assureNotEmpty(sequenceName, "sequenceName can not be empty");
		String sequenctSQL = ctx.getDialect().getDdlFeatures().getSequenceNextValString();
		sequenctSQL = StrUtils.replace(sequenctSQL, "_SEQNAME", sequenceName);
		return ctx.getDbPro().nQueryForObject(sequenctSQL);
	}

	// Getter & Setters below

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

}
