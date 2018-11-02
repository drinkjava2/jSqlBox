/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects.id;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;

/**
 * The platform-independent SequenceGen model, similar like JPA
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SequenceIdGenerator implements IdGenerator {

	/**
	 * A unique generator name that can be referenced by one or more classes to
	 * be the generator for primary key values.
	 */
	private String name;

	/**
	 * The name of the sequence in database
	 */
	private String sequenceName;

	/**
	 * The value from which the sequence is to start generating.
	 */
	private Integer initialValue = 0;

	/**
	 * The amount to allocationSize by when allocating sequence numbers from the
	 * sequence, in Oracle this is identical to "INCREMENT BY", for JPA and ORM
	 * tools this usually value is 50
	 */
	private Integer allocationSize = 1;

	public SequenceIdGenerator() {
		// default constructor
	}

	public SequenceIdGenerator(String name, String sequenceName, Integer initialValue, Integer allocationSize) {
		this.name = name;
		this.sequenceName = sequenceName;
		this.initialValue = initialValue;
		this.allocationSize = allocationSize;
	}

 	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect, Type dataType) {
		DialectException.assureNotEmpty(sequenceName, "sequenceName can not be empty");
		String sequenctSQL = dialect.getDdlFeatures().getSequenceNextValString();
		sequenctSQL = StrUtils.replace(sequenctSQL, "_SEQNAME", sequenceName);
		return jdbc.nQueryForObject(sequenctSQL);
	}
	 
	@Override
	public GenerationType getGenerationType() { 
		return GenerationType.SEQUENCE;
	}

	@Override
	public String getIdGenName() { 
		return name;
	}

	@Override
	public IdGenerator newCopy() {
		return new SequenceIdGenerator(name,sequenceName,initialValue,allocationSize);
	}
	
	@Override
	public Boolean dependOnAutoIdGenerator() {
		return false;
	}
	
	// getter & setter==============
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

	public Integer getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(Integer initialValue) {
		this.initialValue = initialValue;
	}

	public Integer getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(Integer allocationSize) {
		this.allocationSize = allocationSize;
	}

}
