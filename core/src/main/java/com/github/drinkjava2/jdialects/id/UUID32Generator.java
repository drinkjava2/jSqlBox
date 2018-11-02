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

import java.util.UUID;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;

/**
 * Generate a JDK 32 letters random UUID based on Base16 encoding, example:
 * bca5414e9b1b4bdfa257125e05428b92
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class UUID32Generator implements IdGenerator {
	public static final UUID32Generator INSTANCE = new UUID32Generator();

	@Override
	public GenerationType getGenerationType() {
		return GenerationType.UUID25;
	}

	@Override
	public String getIdGenName() {
		return "UUID32";
	}

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect, Type dataType) {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	@Override
	public Boolean dependOnAutoIdGenerator() {
		return false;
	}

	@Override
	public IdGenerator newCopy() {
		return INSTANCE;
	}
}
