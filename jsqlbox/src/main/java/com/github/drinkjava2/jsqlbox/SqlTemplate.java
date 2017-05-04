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
package com.github.drinkjava2.jsqlbox;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.github.drinkjava2.jdialects.StrUtils;

/**
 * SqlTemplate is the base class for SQL Template files, detail see demo
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */

public class SqlTemplate { 

	/*  
	 * Get the SQL from SqlTemplate file comments
	 */
	@Override
	public String toString() {
		String className = this.getClass().getName(); 
		className = SqlBoxStringUtils.substringAfterLast(className, ".");
		String templateSqlFile = SqlBoxStringUtils.substringBefore(className, "$") + ".java";
		String sqlPublicStaticClass = SqlBoxStringUtils.substringAfter(className, "$"); 
		String templateText = getSqlTemplate(templateSqlFile);
		return extractSqlInComments(templateText, sqlPublicStaticClass);
	}

	public static String extractSqlInComments(String templateText, String sqlPublicStaticClass) {
		String classText = StrUtils.substringBetween(templateText, "public " + sqlPublicStaticClass + "(",
				"public static class");
		if (StrUtils.isEmpty(classText))
			return (String) SqlBoxException
					.throwEX("Can not find sql template class started with: public " + sqlPublicStaticClass + "(");
		String[] comments = SqlBoxStringUtils.substringsBetween(classText, "/*", "*/");
		StringBuilder sb = new StringBuilder();
		for (String str : comments) {
			sb.append(str);
		}
		return sb.toString();
	}

	public static String getSqlTemplate(String javaFileName) {
		String str;
		InputStream input = null;
		try {// NOSONAR
			input = ClassLoader.getSystemResourceAsStream(javaFileName);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] data = new byte[4096];
			int count = -1;// NOSONAR
			while ((count = input.read(data, 0, 4096)) != -1)
				outStream.write(data, 0, count);
			data = null;// NOSONAR
			str = new String(outStream.toByteArray(), "ISO-8859-1");
		} catch (Exception e) {
			return (String) SqlBoxException.throwEX(e, "Can not read resource file:" + javaFileName);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (Exception e) {
				SqlBoxException.eatException(e);
			}
		}
		return str;
	}
}
