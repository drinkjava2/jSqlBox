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

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Generate 22 letters UUID based on Base58 encoding, format: HdNmpQHeGLy8eozSSq2p1B
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShortUUIDGenerator implements IdGenerator {

	private static final Random random = new Random();
	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	private static String convertHexToBase36(String hex) {
		BigInteger big = new BigInteger(hex, 16);
		StringBuilder sb = new StringBuilder(big.toString(36));
		return sb.reverse().toString();
	}

	protected static String convertBase36ToHex(String b36) {
		StringBuilder sb = new StringBuilder(b36);
		BigInteger base = new BigInteger(sb.reverse().toString(), 36);
		return base.toString(16);
	}

	protected static String getBase36UUID() {
		String s = convertHexToBase36(UUID.randomUUID().toString().replaceAll("-", ""));
		while (s.length() < 25)
			s = s + ALPHABET[random.nextInt(36)];// NOSONAR
		return s;
	}

	@Override
	public Object getNextID(SqlBoxContext ctx) {
		return getBase36UUID();
	}

}
