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

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Generate 32 letters UUID based on radix 36 encoding, start with time stamp, format: 0012345HdNmpQHeGLy8eozSSq2p1B
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class TimeUUIDGenerator extends ShortUUIDGenerator {
	private static final Random random = new Random();
	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	@Override
	public Object getNextID(SqlBoxContext ctx) {
		return null;
	}

	private static String getTimeUUID() {

		return null;
	}

	public static void main(String[] args) {
		long oneyear = 365l * 24 * 60 * 60 * 1000 * 50;
		for (int i = 1; i <= 100; i++) {
			BigInteger b = new BigInteger("" + oneyear * i);
			String s = b.toString(36);
			System.out.println(s);
		}
	}
}
