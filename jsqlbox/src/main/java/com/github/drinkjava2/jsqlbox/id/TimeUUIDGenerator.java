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
 * Generate 32 letters UUID based on Base58 encoding, format: HdNmpQHeGLy8eozSSq2p1B
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class TimeUUIDGenerator implements IdGenerator {
	private static final SecureRandom random = new SecureRandom();
	private static final char[] Base58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
			.toCharArray();

	@Override
	public Object getNextID(SqlBoxContext ctx) {
		return getTimeUUID();
	}

	/**
	 * Get random base58 IDs by give length
	 */
	private static String randomBase58ID(int length) {
		char[] cs = new char[length];
		for (int i = 0; i < cs.length; i++) {
			cs[i] = Base58_ALPHABET[random.nextInt(Base58_ALPHABET.length)];
		}
		return new String(cs);
	}

	/**
	 * Get a sorted UUID
	 */
	public static String getTimeUUID() {
		return randomBase58ID(16);
	}

	public static void main(String[] args) {
		String s;
		for (int i = 0; i < 1000; i++) {
			s = getTimeUUID();
			System.out.println(s);
		}
	}

}
