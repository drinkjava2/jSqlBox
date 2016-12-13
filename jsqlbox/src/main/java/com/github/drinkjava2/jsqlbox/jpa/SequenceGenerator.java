package com.github.drinkjava2.jsqlbox.jpa;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Define a Sequence ID generator, implements of JPA SequenceGenerator
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */

public class SequenceGenerator implements IdGenerator {
	/**
	 * (Required) A unique generator name that can be referenced by one or more classes to be the generator for primary
	 * key values.
	 */
	String name;

	/**
	 * (Optional) The name of the database sequence object from which to obtain primary key values. Defaults to a
	 * provider-chosen value.
	 */
	String sequenceName = "";

	/**
	 * (Optional) The catalog of the sequence generator.
	 */
	String catalog = "";

	/**
	 * (Optional) The schema of the sequence generator.
	 */
	String schema = "";

	/**
	 * (Optional) The value from which the sequence object is to start generating.
	 */
	int initialValue = 1;

	/**
	 * (Optional) The amount to increment by when allocating sequence numbers from the sequence.
	 */
	int allocationSize = 50;

	public SequenceGenerator(String name, String sequenceName) {
		this.name = name;
		this.sequenceName = sequenceName;
	}

	public boolean ifEqual(String name, String sequenceName) {
		return this.name.equals(name) && this.sequenceName.equals(sequenceName);
	}

	@Override
	public Object getNextID(SqlBoxContext ctx) {
		return ctx.getJdbc().queryForObject("select " + sequenceName + ".nextval from dual", Integer.class);
	}

	// Getter & Setters below

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public int getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(int initialValue) {
		this.initialValue = initialValue;
	}

	public int getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(int allocationSize) {
		this.allocationSize = allocationSize;
	}

}
