/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
