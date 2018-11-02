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

import java.math.BigInteger;
import java.util.UUID;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;

/**
 * Compress JDK UUID to 25 letters based on radix 36, use 0-9 a-z characters,
 * example: pbicz3grgu0zk3ipe1yur03h7
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class UUID25Generator implements IdGenerator {
	public static final UUID25Generator INSTANCE = new UUID25Generator();

	@Override
	public GenerationType getGenerationType() {
		return GenerationType.UUID25;
	}

	@Override
	public String getIdGenName() {
		return "UUID25";
	}

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect, Type dataType) {
		return get25LettersRadix36UUID();
	}

	@Override
	public Boolean dependOnAutoIdGenerator() {
		return false;
	}

	@Override
	public IdGenerator newCopy() {
		return INSTANCE;
	}

	private static String get25LettersRadix36UUID() {
		String uuidHex = UUID.randomUUID().toString().replaceAll("-", "");
		BigInteger b = new BigInteger(uuidHex, 16);
		String s = b.toString(36);
		while (s.length() < 25)
			s = s + "0";// NOSONAR
		return s;
	}

}
