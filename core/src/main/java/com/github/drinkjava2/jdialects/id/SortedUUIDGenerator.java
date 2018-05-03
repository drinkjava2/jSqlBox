/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
