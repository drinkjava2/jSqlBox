/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.github.drinkjava2.jdialects.StrUtils;

/**
 * TextUtils is base class for Java text support (multiple line Strings).
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 */
public abstract class TextUtils {// NOSONAR

	private static final Map<Class<?>, String> javaFileCache = new HashMap<Class<?>, String>();

	@SuppressWarnings("all")
	public static String getJavaSourceCodeUTF8(Class<?> clazz) {
		return getJavaSourceCode(clazz, "UTF-8");
	}

	@SuppressWarnings("all")
	public static String getJavaSourceCode(Class<?> clazz, String encoding) {
		if (javaFileCache.containsKey(clazz))
			return javaFileCache.get(clazz);
		String classPathName = StrUtils.substringBefore(clazz.getName(), "$");// aa.bb.Cc
		classPathName = "/" + StrUtils.replace(classPathName, ".", "/");// /aa/bb/Cc
		String fileName = classPathName + ".java";// /aa/bb/Cc.java
		InputStream inputStream = null;
		try {
			inputStream = TextUtils.class.getResourceAsStream(fileName);
			if (inputStream == null) {// Not found, it means in eclipse environment
				File file = new File(clazz.getResource(classPathName + ".class").getFile());
				String absPath = file.getAbsolutePath();
				absPath = StrUtils.replace(absPath, "\\", "/");
				String projectFolder = StrUtils.substringBefore(absPath, "/target/classes/");
				String realFile = projectFolder + "/src/main/resources" + classPathName + ".java";
				file = new File(realFile);
				if (!file.exists()) {
					realFile = projectFolder + "/src/test/resources" + classPathName + ".java";
					file = new File(realFile);
				}
				if (!file.exists())
					throw new IOException("Can not find '" + realFile + "' in resources folder");
				inputStream = new FileInputStream(file);
				if (inputStream == null)
					throw new IOException("Error happen when open '" + realFile + "'");
			}
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1)
				result.write(buffer, 0, length);
			String javaSrc = result.toString(encoding);
			javaFileCache.put(clazz, javaSrc);
			return javaSrc;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}

}