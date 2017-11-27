/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jtinynet.parser;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jtinynet.parser.TinyParser.Item;

/**
 * The utility class put some public static methods for TinyParser
 */
@SuppressWarnings("all")
public class TinyParserUtils {

	/**
	 * Explain functions, functions can have 1 or 2 parameters, function items is in
	 * the middle of 2 parameters
	 */
	public static void doTheMath(Item func, Item last, Item next) {
		String f = (String) func.value;
		char lastType = (last == null) ? '0' : last.type;
		char nextType = (next == null) ? '0' : next.type;
		if ("+".equals(f)) {
			if (lastType == '0' || lastType == 'F') {// only 1 parameter
				if (nextType == 'D' || nextType == 'L') {
					func.type = nextType;
					func.value = next.value;
					deleteItem(next);
				} else
					throwExp(f);
			} else {// 2 parameters
				if (lastType == 'S' || lastType == 'N' || lastType == 'B' || nextType == 'S' || nextType == 'N'
						|| nextType == 'B') {
					func.type = 'S';
					func.value = "" + last.value + next.value;
				} else if (lastType == 'D' && nextType == 'D') {
					func.type = 'D';
					func.value = (Double) last.value + (Double) next.value;
				} else if (lastType == 'D' && nextType == 'L') {
					func.type = 'D';
					func.value = (Double) last.value + (double) (Long) next.value;
				} else if (lastType == 'L' && nextType == 'D') {
					func.type = 'D';
					func.value = (double) (Long) last.value + (Double) next.value;
				} else if (lastType == 'L' && nextType == 'L') {
					func.type = 'L';
					func.value = (Long) last.value + (Long) next.value;
				} else
					throwExp(f);
				deleteItem(last, next);
			}
		} else if ("-".equals(f)) {
			if (lastType == '0' || lastType == 'F') {// only 1 parameter
				func.type = nextType;
				if (nextType == 'D') {
					func.value = -(Double) next.value;
				} else if (nextType == 'L') {
					func.value = -(Long) next.value;
				} else
					throwExp(f);
				deleteItem(next);
			} else {// 2 parameters
				if (lastType == 'D' && nextType == 'D') {
					func.type = 'D';
					func.value = (Double) last.value - (Double) next.value;
				} else if (lastType == 'D' && nextType == 'L') {
					func.type = 'D';
					func.value = (Double) last.value - (double) (Long) next.value;
				} else if (lastType == 'L' && nextType == 'D') {
					func.type = 'D';
					func.value = (double) (Long) last.value - (Double) next.value;
				} else if (lastType == 'L' && nextType == 'L') {
					func.type = 'L';
					func.value = (Long) last.value - (Long) next.value;
				} else
					throwExp(f);
				deleteItem(last, next);
			}
		} else if ("*".equals(f)) {
			if (lastType == 'D' && nextType == 'D') {
				func.type = 'D';
				func.value = (Double) last.value * (Double) next.value;
			} else if (lastType == 'D' && nextType == 'L') {
				func.type = 'D';
				func.value = (Double) last.value * (double) (Long) next.value;
			} else if (lastType == 'L' && nextType == 'D') {
				func.type = 'D';
				func.value = (double) (Long) last.value * (Double) next.value;
			} else if (lastType == 'L' && nextType == 'L') {
				func.type = 'L';
				func.value = (Long) last.value * (Long) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("/".equals(f)) {
			if (lastType == 'D' && nextType == 'D') {
				func.type = 'D';
				func.value = (Double) last.value / (Double) next.value;
			} else if (lastType == 'D' && nextType == 'L') {
				func.type = 'D';
				func.value = (Double) last.value / (double) (Long) next.value;
			} else if (lastType == 'L' && nextType == 'D') {
				func.type = 'D';
				func.value = (double) (Long) last.value / (Double) next.value;
			} else if (lastType == 'L' && nextType == 'L') {
				func.type = 'L';
				func.value = (Long) last.value / (Long) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("NOT".equals(f)) {
			if (nextType == 'B') {
				func.type = 'B';
				func.value = !(Boolean) next.value;
				deleteItem(next);
			} else if (nextType == 'F' && "NOT".equals(next.value)) {
				deleteItem(func, next);
			} else
				throwExp(f);
		} else if ("AND".equals(f)) {
			if (lastType == 'B' && nextType == 'B') {
				func.type = 'B';
				func.value = (Boolean) last.value && (Boolean) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("OR".equals(f)) {
			if (lastType == 'B' && nextType == 'B') {
				func.type = 'B';
				func.value = (Boolean) last.value || (Boolean) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if (">".equals(f)) {
			if (lastType == 'N' || nextType == 'N') {
				func.type = 'B';
				func.value = false;
			} else if (lastType == 'D' && nextType == 'D') {
				func.type = 'B';
				func.value = (Double) last.value > (Double) next.value;
			} else if (lastType == 'D' && nextType == 'L') {
				func.type = 'B';
				func.value = (Double) last.value > (double) (Long) next.value;
			} else if (lastType == 'L' && nextType == 'D') {
				func.type = 'B';
				func.value = (double) (Long) last.value > (Double) next.value;
			} else if (lastType == 'L' && nextType == 'L') {
				func.type = 'B';
				func.value = (Long) last.value > (Long) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("<".equals(f)) {
			if (lastType == 'N' || nextType == 'N') {
				func.type = 'B';
				func.value = false;
			} else if (lastType == 'D' && nextType == 'D') {
				func.type = 'B';
				func.value = (Double) last.value < (Double) next.value;
			} else if (lastType == 'D' && nextType == 'L') {
				func.type = 'B';
				func.value = (Double) last.value < (double) (Long) next.value;
			} else if (lastType == 'L' && nextType == 'D') {
				func.type = 'B';
				func.value = (double) (Long) last.value < (Double) next.value;
			} else if (lastType == 'L' && nextType == 'L') {
				func.type = 'B';
				func.value = (Long) last.value < (Long) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("=".equals(f)) {
			if (lastType == 'N' || nextType == 'N') {
				func.type = 'B';
				func.value = last.value == next.value;
			} else if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = ((String) last.value).equals((String) next.value);
			} else if (lastType == 'D' && nextType == 'D') {
				func.type = 'B';
				func.value = ((Double) last.value - (Double) next.value) == 0;
			} else if (lastType == 'D' && nextType == 'L') {
				func.type = 'B';
				func.value = ((Double) last.value - (double) (Long) next.value) == 0;
			} else if (lastType == 'L' && nextType == 'D') {
				func.type = 'B';
				func.value = ((double) (Long) last.value - (Double) next.value) == 0;
			} else if (lastType == 'L' && nextType == 'L') {
				func.type = 'B';
				func.value = ((Long) last.value - (Long) next.value) == 0;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("<>".equals(f)) {
			if (lastType == 'N' || nextType == 'N') {
				func.type = 'B';
				func.value = !(last.value == next.value);
			} else if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = !((String) last.value).equals((String) next.value);
			} else if (lastType == 'D' && nextType == 'D') {
				func.type = 'B';
				func.value = (Double) last.value != (Double) next.value;
			} else if (lastType == 'D' && nextType == 'L') {
				func.type = 'B';
				func.value = (Double) last.value != (double) (Long) next.value;
			} else if (lastType == 'L' && nextType == 'D') {
				func.type = 'B';
				func.value = (double) (Long) last.value != (Double) next.value;
			} else if (lastType == 'L' && nextType == 'L') {
				func.type = 'B';
				func.value = (Long) last.value != (Long) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if (">=".equals(f)) {
			if (lastType == 'N' || nextType == 'N') {
				func.type = 'B';
				func.value = false;
			} else if (lastType == 'D' && nextType == 'D') {
				func.type = 'B';
				func.value = (Double) last.value >= (Double) next.value;
			} else if (lastType == 'D' && nextType == 'L') {
				func.type = 'B';
				func.value = (Double) last.value >= (double) (Long) next.value;
			} else if (lastType == 'L' && nextType == 'D') {
				func.type = 'B';
				func.value = (double) (Long) last.value >= (Double) next.value;
			} else if (lastType == 'L' && nextType == 'L') {
				func.type = 'B';
				func.value = (Long) last.value >= (Long) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("<=".equals(f)) {
			if (lastType == 'N' || nextType == 'N') {
				func.type = 'B';
				func.value = false;
			} else if (lastType == 'D' && nextType == 'D') {
				func.type = 'B';
				func.value = (Double) last.value <= (Double) next.value;
			} else if (lastType == 'D' && nextType == 'L') {
				func.type = 'B';
				func.value = (Double) last.value <= (double) (Long) next.value;
			} else if (lastType == 'L' && nextType == 'D') {
				func.type = 'B';
				func.value = (double) (Long) last.value <= (Double) next.value;
			} else if (lastType == 'L' && nextType == 'L') {
				func.type = 'B';
				func.value = (Long) last.value <= (Long) next.value;
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("EQUALS".equals(f)) {
			if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = ((String) last.value).equals((String) next.value);
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("EQUALSIGNORECASE".equals(f)) {
			if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = ((String) last.value).equalsIgnoreCase((String) next.value);
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("CONTAINS".equals(f)) {
			if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = ((String) last.value).contains((String) next.value);
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("CONTAINSIGNORECASE".equals(f)) {
			if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = StrUtils.containsIgnoreCase((String) last.value, (String) next.value);
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("STARTWITH".equals(f)) {
			if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = ((String) last.value).startsWith((String) next.value);
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("STARTWITHIGNORECASE".equals(f)) {
			if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = StrUtils.startsWithIgnoreCase((String) last.value, (String) next.value);
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("ENDWITH".equals(f)) {
			if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = ((String) last.value).endsWith((String) next.value);
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("ENDWITHIGNORECASE".equals(f)) {
			if (lastType == 'S' && nextType == 'S') {
				func.type = 'B';
				func.value = ((String) last.value).toUpperCase().endsWith(((String) next.value).toUpperCase());
			} else
				throwExp(f);
			deleteItem(last, next);
		} else if ("IS".equals(f)) {
			if (last != null && nextType == 'N' && next.value == null) {
				func.type = 'B';
				func.value = null == last.value;
				deleteItem(last, next);
			} else
				throwExp(f);
		}

		else
			throw new TinyParserException("Un-parsed expression function: '" + f + "'");
	}

	private static void throwExp(String f) {
		throw new TinyParserException("Expression error near '" + f + "'");
	}

	public static void deleteItem(Item item) {
		if (item != null)
			item.type = '0';
	}

	public static void deleteItem(Item lastItem, Item nextItem) {
		if (lastItem != null)
			lastItem.type = '0';
		if (nextItem != null)
			nextItem.type = '0';
	}

	public static boolean isLetterNumber(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '.';
	}
}