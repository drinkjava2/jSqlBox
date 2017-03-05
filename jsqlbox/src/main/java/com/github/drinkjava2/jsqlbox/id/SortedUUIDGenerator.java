/**
* Copyright (C) 2016 Yong Zhu.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.drinkjava2.jsqlbox.id;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * Generate Sorted UUID, it's the combination of TableGenertor and UUIDAnyGenerator, start with table value, followed by
 * radix36 random letters, example: <br/>
 * 123NmpQHeGLy8eozSSq2p1B<br/>
 * 124NmpQHeGLy8eozSSq2p1B<br/>
 * 125NmpQHeGLy8eozSSq2p1B<br/>
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SortedUUIDGenerator implements IdGenerator {
	private TableGenerator tableGenerator;

	private UUIDAnyGenerator uuidAnyGenerator;

	/**
	 * If fixTotalLength>0, will fix total length and put "1" at beginning
	 */
	private Integer fixTotalLength = 0;

	public SortedUUIDGenerator(TableGenerator tableGenerator, UUIDAnyGenerator uuidAnyGenerator,
			Integer fixTotalLength) {
		this.tableGenerator = tableGenerator;
		this.uuidAnyGenerator = uuidAnyGenerator;
		this.fixTotalLength = fixTotalLength;
	}

	@Override
	public Object getNextID(SqlBoxContext ctx) {
		String s = "" + tableGenerator.getNextID(ctx) + (String) uuidAnyGenerator.getNextID(ctx);
		if (fixTotalLength <= 0)
			return s;
		if (fixTotalLength < s.length())
			SqlBoxException
					.throwEX("SortedUUIDGenerator getNextID error, fixTotalLength can not set less than " + s.length());
		StringBuilder sb = new StringBuilder("1");
		for (int i = 1; i < fixTotalLength - s.length(); i++)
			sb.append("0");
		sb.append(s);
		return sb.toString();
	}

	/**
	 * Get the total length of the ID
	 */
	public Integer getFixTotalLength() {
		return fixTotalLength;
	}

	/**
	 * Set the total length of the ID, If >0, will fix total length and put "1" at beginning
	 */
	public void setFixTotalLength(Integer fixTotalLength) {
		this.fixTotalLength = fixTotalLength;
	}

}
