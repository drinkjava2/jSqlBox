/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jbeanbox;

/**
 * A simple string matcher tool
 * 
 * @author Yong Zhu
 * @since 2.5.0
 */
public class NameMatchUtil { // NOSONAR
	/**
	 * A simple string matcher, only 1 * allowed, but many regex string can
	 * seperated by "|" char, for example: <br/>
	 * "abc.ef*|*gh|ijk*lmn" matches "abc.efxxx", "xxxgh","ijkxxxlmn"
	 */
	public static boolean nameMatch(String regex, String name) {
		if (regex == null || regex.length() == 0 || name == null || name.length() == 0)
			return false;
		do {
			int i = regex.indexOf('|');
			if (i < 0)
				return doSingleNameMatch(regex, name);
			if (doSingleNameMatch(regex.substring(0, i), name))
				return true;
			regex = regex.substring(i + 1);// NOSONAR
		} while (true);
	}

	/** A simple matcher, only 1 * allowed represents any string */
	private static boolean doSingleNameMatch(String regex, String name) {
		if (regex == null || regex.length() == 0 || name == null || name.length() == 0)
			return false;
		if ('*' == (regex.charAt(0))) {
			return name.endsWith(regex.substring(1));
		} else if (regex.endsWith("*")) {
			return name.startsWith(regex.substring(0, regex.length() - 1));
		} else {
			int starPos = regex.indexOf('*');
			if (-1 == starPos)
				return regex.equals(name);
			return name.startsWith(regex.substring(0, starPos)) && name.endsWith(regex.substring(starPos + 1));
		}
	}
}
