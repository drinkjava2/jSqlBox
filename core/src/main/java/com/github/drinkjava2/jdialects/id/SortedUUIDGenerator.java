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
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;

/**
 * Generate a Sorted UUID, total length = sortedLength + uuidLength, <br/>
 * for example, SortedUUIDGenerator(5,20): <br/>
 * 10001NmpQHeGLy8eozSSq2p1B<br/>
 * 10002DLIGkILFISKJF23KLSDF<br/>
 * 10003LVBIFI35LDFJIA31KDSF<br/>
 * 
 * @author Yong Zhu
 * @since 1.0.6
 */
public class SortedUUIDGenerator implements IdGenerator {
	private String name;
	private int sortedLength;
	private int uuidLength;

	public SortedUUIDGenerator(String name, int sortedLength, int uuidLength) {
		this.name = name;
		this.sortedLength = sortedLength;
		this.uuidLength = uuidLength;
	}

	@Override
	public GenerationType getGenerationType() {
		return GenerationType.SORTED_UUID;
	}

	@Override
	public String getIdGenName() {
		return name;
	}

	@Override
	public Boolean dependOnAutoIdGenerator() {
		return true;
	}
	
	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect, Type dataType) {
		String s = "" + AutoIdGenerator.INSTANCE.getNextID(jdbc, dialect, dataType);
		if (s.length() > (sortedLength - 1))
			throw new DialectException("SortedLength should set bigger than auto generated ID length");
		StringBuilder sb = new StringBuilder("1");
		for (int i = 1; i < sortedLength - s.length(); i++)
			sb.append("0");
		sb.append(s);
		sb.append(UUIDAnyGenerator.getAnyLengthRadix36UUID(uuidLength));
		return sb.toString();
	}

	@Override
	public IdGenerator newCopy() {
		return new SortedUUIDGenerator(name, sortedLength, uuidLength);
	}

	public String getName() {
		return name;
	}

	public int getSortedLength() {
		return sortedLength;
	}

	public int getUuidLength() {
		return uuidLength;
	}

}
