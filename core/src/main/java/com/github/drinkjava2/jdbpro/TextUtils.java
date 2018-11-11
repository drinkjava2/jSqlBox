/**
 * Copyright 2016 the original author or authors.
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdialects.StrUtils;

/**
 * TextUtils is used to read Java source file from sources folder, usage: String
 * src=TextUtils.getJavaSourceCode(Foo.class, "UTF-8"); To use this function
 * need copy java src file into resources folder or set a plug-in in pom.xml,
 * detail see jSqlBox's documents
 * 
 * @author Yong Zhu
 */
public abstract class TextUtils {// NOSONAR

	protected static final Map<String, String> javaFileCache = new ConcurrentHashMap<String, String>();

	@SuppressWarnings("all")
	public static String getJavaSourceCodeUTF8(Class<?> clazz) {
		return getJavaSourceCode(clazz, "UTF-8");
	}

	public static String getJavaSourceCodeUTF8(String classFullName) {
		return getJavaSourceCode(classFullName, "UTF-8");
	}

	public static String getJavaSourceCode(String classFullName, String encoding) {
		if (javaFileCache.containsKey(classFullName))
			return javaFileCache.get(classFullName);
		try {
			Class<?> clazz = Class.forName(classFullName);
			return getJavaSourceCode(clazz, encoding);
		} catch (ClassNotFoundException e) {
			throw new DbProRuntimeException(e);
		}
	}

	@SuppressWarnings("all")
	public static String getJavaSourceCode(Class<?> clazz, String encoding) {
		if (clazz == null)
			return null;
		if (javaFileCache.containsKey(clazz.getName()))
			return javaFileCache.get(clazz.getName());
		String classPathName = StrUtils.substringBefore(clazz.getName(), "$");// aa.bb.Cc
		classPathName = "/" + StrUtils.replace(classPathName, ".", "/");// /aa/bb/Cc
		String fileName = classPathName + ".java";// /aa/bb/Cc.java
		InputStream inputStream = null;
		try {
			inputStream = TextUtils.class.getResourceAsStream(fileName);
			if (inputStream == null) {// Not found, it means in eclipse
				File file = new File(clazz.getResource(classPathName + ".class").getFile());
				String absPath = file.getAbsolutePath();
				absPath = StrUtils.replace(absPath, "\\", "/");
				String projectFolder = StrUtils.substringBefore(absPath, "/target/");
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
					throw new IOException("Error happen when open file '" + realFile + "'");
			}
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1)
				result.write(buffer, 0, length);
			String javaSrc = result.toString(encoding);
			javaFileCache.put(clazz.getName(), javaSrc);
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