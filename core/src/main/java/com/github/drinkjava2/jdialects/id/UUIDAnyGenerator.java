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

import java.security.SecureRandom;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;

/**
 * Generate any length UUID String based on radix 36, use 0-9 a-z characters
 * <br/>
 * Default length is 20;
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class UUIDAnyGenerator implements IdGenerator {

	private static final SecureRandom random = new SecureRandom();
	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	private String name;
	private Integer length = 36;

	public UUIDAnyGenerator() {
		// default constructor
	}

	/**
	 * Build a give length UUID Generator
	 */
	public UUIDAnyGenerator(String name, Integer length) {
		this.name = name;
		this.length = length;
	}

	@Override
	public GenerationType getGenerationType() {
		return GenerationType.UUID_ANY;
	}

	@Override
	public String getIdGenName() {
		return name;
	}

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect, Type dataType) {
		return getAnyLengthRadix36UUID(length);
	}

	@Override
	public Boolean dependOnAutoIdGenerator() {
		return false;
	}

	@Override
	public IdGenerator newCopy() {
		return new UUIDAnyGenerator(name, length);
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
