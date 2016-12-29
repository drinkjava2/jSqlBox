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

import java.security.SecureRandom;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Generate any length UUID String based on radix 36, use 0-9 a-z characters <br/>
 * Default length is 20;
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class UUIDAnyGenerator implements IdGenerator {

	private static final SecureRandom random = new SecureRandom();
	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	private Integer length = 20;

	public UUIDAnyGenerator() {
		// default constructor
	}

	/**
	 * Build a give length UUID Generator
	 */
	public UUIDAnyGenerator(Integer length) {
		this.length = length;
	}

	@Override
	public Object getNextID(SqlBoxContext ctx) {
		return getAnyLengthRadix36UUID(length);
	}

	protected static String getAnyLengthRadix36UUID(Integer length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(ALPHABET[random.nextInt(32)]);
		}
		return sb.toString();
	}

	// getter & setter
	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

}
