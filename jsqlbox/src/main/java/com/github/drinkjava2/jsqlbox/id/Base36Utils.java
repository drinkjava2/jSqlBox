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

/**
 * Base36 encode and decode tool <br/>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class Base36Utils {

	private static final Random random = new Random();
	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	private Base36Utils() {
		// Hide constructor
	}

	public static String convertHexToBase36(String hex) {
		BigInteger big = new BigInteger(hex, 16);
		StringBuilder sb = new StringBuilder(big.toString(36));
		return sb.reverse().toString();
	}

	public static String convertBase36ToHex(String b36) {
		StringBuilder sb = new StringBuilder(b36);
		BigInteger base = new BigInteger(sb.reverse().toString(), 36);
		return base.toString(16);
	}

	public static String getBase36UUID() {
		String s = convertHexToBase36(UUID.randomUUID().toString().replaceAll("-", ""));
		while (s.length() < 25)
			s = s + ALPHABET[random.nextInt(36)];// NOSONAR
		return s;
	}

	public static void main(String[] args) {
		System.out.println(getBase36UUID());

	}
}
