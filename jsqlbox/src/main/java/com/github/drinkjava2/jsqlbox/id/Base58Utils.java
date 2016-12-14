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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;

/**
 * Transfer long to Base58 <br/>
 * This class is copied from:http://www.cnblogs.com/lic309/p/4433966.html
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class Base58Utils {
	private static long lastTimeCount = 0;
	private static int lastOrderCount = 0;
	private static final Random random = new Random();

	private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
	private static final int[] INDEXES = new int[128];

	private Base58Utils() {
		// Hide constructor
	}

	static {
		for (int i = 0; i < INDEXES.length; i++) {
			INDEXES[i] = -1;
		}
		for (int i = 0; i < ALPHABET.length; i++) {
			INDEXES[ALPHABET[i]] = i;
		}
	}

	/**
	 * Encodes the given bytes in base58. No checksum is appended.
	 */
	private static String encode(byte[] input) {// NOSONAR
		if (input.length == 0) {
			return "";
		}
		byte[] in = copyOfRange(input, 0, input.length);
		// Count leading zeroes.
		int zeroCount = 0;
		while (zeroCount < in.length && in[zeroCount] == 0) {
			++zeroCount;
		}
		// The actual encoding.
		byte[] temp = new byte[in.length * 2];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < in.length) {
			byte mod = divmod58(in, startAt);
			if (in[startAt] == 0) {
				++startAt;
			}
			temp[--j] = (byte) ALPHABET[mod];
		}

		// Strip extra '1' if there are some after decoding.
		while (j < temp.length && temp[j] == ALPHABET[0]) {
			++j;
		}
		// Add as many leading '1' as there were leading zeros.
		while (--zeroCount >= 0) {
			temp[--j] = (byte) ALPHABET[0];
		}

		byte[] output = copyOfRange(temp, j, temp.length);
		try {
			return new String(output, "US-ASCII");
		} catch (UnsupportedEncodingException e) {// NOSONAR, cannot happen
			return null;
		}
	}

	private static byte[] decode(String input) throws IllegalArgumentException {// NOSONAR
		if (input.length() == 0) {
			return new byte[0];
		}
		byte[] input58 = new byte[input.length()];
		// Transform the String to a base58 byte sequence
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);

			int digit58 = -1;
			if (c >= 0 && c < 128) {
				digit58 = INDEXES[c];
			}
			if (digit58 < 0) {
				throw new IllegalArgumentException("Illegal character " + c + " at " + i);
			}

			input58[i] = (byte) digit58;
		}
		// Count leading zeroes
		int zeroCount = 0;
		while (zeroCount < input58.length && input58[zeroCount] == 0) {
			++zeroCount;
		}
		// The encoding
		byte[] temp = new byte[input.length()];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input58.length) {
			byte mod = divmod256(input58, startAt);
			if (input58[startAt] == 0) {
				++startAt;
			}

			temp[--j] = mod;
		}
		// Do no add extra leading zeroes, move j to first non null byte.
		while (j < temp.length && temp[j] == 0) {
			++j;
		}

		return copyOfRange(temp, j - zeroCount, temp.length);
	}

	//
	// number -> number / 58, returns number % 58
	//
	private static byte divmod58(byte[] number, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number.length; i++) {
			int digit256 = (int) number[i] & 0xFF;
			int temp = remainder * 256 + digit256;

			number[i] = (byte) (temp / 58);

			remainder = temp % 58;
		}

		return (byte) remainder;
	}

	//
	// number -> number / 256, returns number % 256
	//
	private static byte divmod256(byte[] number58, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number58.length; i++) {
			int digit58 = (int) number58[i] & 0xFF;
			int temp = remainder * 58 + digit58;

			number58[i] = (byte) (temp / 256);

			remainder = temp % 256;
		}

		return (byte) remainder;
	}

	private static byte[] copyOfRange(byte[] source, int from, int to) {
		byte[] range = new byte[to - from];
		System.arraycopy(source, from, range, 0, range.length);

		return range;
	}

	public static String base58Uuid() {
		UUID uuid = UUID.randomUUID();
		return base58Uuid(uuid);
	}

	private static String base58Uuid(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return encode(bb.array());
	}

	public static String encodeBase58Uuid(String uuidString) {
		UUID uuid = UUID.fromString(uuidString);
		return base58Uuid(uuid);
	}

	public static String decodeBase58Uuid(String base58uuid) {
		byte[] byUuid = Base58Utils.decode(base58uuid);
		ByteBuffer bb = ByteBuffer.wrap(byUuid);
		UUID uuid = new UUID(bb.getLong(), bb.getLong());
		return uuid.toString();
	}

	private static synchronized long getNextTimeOrder(long currentTime) {
		if (currentTime != lastTimeCount) {
			lastTimeCount = currentTime;
			lastOrderCount = 0;
		} else {
			lastOrderCount = lastOrderCount + random.nextInt(30);
			if (lastOrderCount > 10000)
				lastOrderCount = 0;
		}
		return lastTimeCount + lastOrderCount;
	}

	public static String getSortedUUID() {
		long time = getNextTimeOrder((System.currentTimeMillis() << 14) & 0xfffffffffffffl);
		ByteBuffer bb = ByteBuffer.wrap(new byte[8]);
		bb.putLong(time);
		return encode(bb.array()) + base58Uuid();
	}

	public static void main(String[] args) {
		for (int i = 0; i < 2000; i++) {
			System.out.println(getSortedUUID());
		}

	}
}
