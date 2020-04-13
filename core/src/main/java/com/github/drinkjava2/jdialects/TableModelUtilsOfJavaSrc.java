/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects;

import static com.github.drinkjava2.jdialects.StrUtils.clearQuote;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.springsrc.utils.StringUtils;

/**
 * The tool to convert TableModel to Java source code
 *
 * @author Yong Zhu
 * @since 2.0.4
 */
public abstract class TableModelUtilsOfJavaSrc {// NOSONAR

	/**
	 * Map DB column name to entity field name, example: <br/>
	 * user_name -> userName <br/>
	 * USER_NAME -> userName <br/>
	 * User_naMe -> userName <br/>
	 * UserName -> userName <br/>
	 * USERNAME -> username <br/>
	 * userName -> userName <br/>
	 * username -> username <br/>
	 */
	private static String transColumnNameToFieldName(String colName) {
		if (StrUtils.isEmpty(colName)) {
			return colName;
		}

		String rawColName = clearQuote(colName);
		if (!colName.contains("_")) {
			// return StrUtils.toLowerCaseFirstOne(colName);
			if (rawColName.toUpperCase().equals(rawColName)) {
				// 全大写
				return rawColName.toLowerCase();
			} else {
				return StrUtils.toLowerCaseFirstOne(rawColName);
			}
		}

		StringBuilder sb = new StringBuilder();
		char[] chars = rawColName.toLowerCase().toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '_') {
				continue;
			}
			if ((i > 0) && (chars[i - 1]) == '_' && sb.length() > 0) {
				sb.append(Character.toUpperCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Map database table name to entity class name, example: <br/>
	 * user_name -> UserName <br/>
	 * USER_NAME -> UserName <br/>
	 * User_naMe -> UserName <br/>
	 * UserName -> UserName <br/>
	 * USERNAME -> Username <br/>
	 * userName -> UserName <br/>
	 * username -> Username <br/>
	 */
	public static String getClassNameFromTableModel(TableModel model) {
		DialectException.assureNotNull(model, "TableModel can not be null");
		String className;
		if (model.getEntityClass() != null) {
			className = model.getEntityClass().getSimpleName();
		} else {
			DialectException.assureNotEmpty(model.getTableName(), "TableName can not be empty in TableModel");
			String tableName = transColumnNameToFieldName(model.getTableName());

			if (tableName.toUpperCase().equals(tableName)) {
				// 全大写
				tableName = tableName.toLowerCase();
			}
			className = StrUtils.toUpperCaseFirstOne(tableName);
		}
		DialectException.assureNotEmpty(className, "TableName can not be empty in TableModel");
		return className;
	}

	/**
	 * Convert a TablemModel instance to Java entity class source code
	 *
	 * @param model
	 *            The TableModel instance
	 * @param setting
	 *            The setting options, for example:
	 *
	 *            <pre>
	 *            Map<String, Object> setting = new HashMap<String, Object>();
	 *            setting.put(TableModelUtils.OPT_LINK_STYLE, false);
	 *            setting.put(TableModelUtils.OPT_PACKAGE_NAME, "somepackage");
	 *            setting.put(TableModelUtils.OPT_FIELD_FLAGS, true);
	 *            setting.put(TableModelUtils.OPT_IMPORTS, "some imports");
	 *            setting.put(TableModelUtils.OPT_CLASS_DEFINITION, "public class $1 extends ActiveRecord<$1>");
	 *            </pre>
	 *
	 * @return Java Bean source code of entity
	 */
	public static String modelToJavaSourceCode(TableModel model, Map<String, Object> setting) {
		// head
		StringBuilder body = new StringBuilder();
		generatePackage(setting, body);
		generateImports(setting, body);

		// @table
		String className = getClassNameFromTableModel(model);
		int fkeyCount = generateAnnotationForClass(model, body, className);

		// class
		generateClassBegin(setting, body, className);
		generateStaticFields(model, setting, body, className);
		generateFields(model, setting, fkeyCount, body);
		generateGetterAndSetter(model, setting, className, body);
		generateClassEnd(body);

		return body.toString();
	}

	private static void generateStaticFields(TableModel model, Map<String, Object> setting, StringBuilder body,
			String className) {
		boolean fieldFlags = Boolean.TRUE.equals(setting.get(TableModelUtils.OPT_FIELD_FLAGS));
		StringBuilder fieldSB = new StringBuilder();
		// fieldStaticNames
		// 不知道为什么有些表的column 定义信息是重复的，我不是DBA不清楚原因。
		// 也需要还有什么废弃的标志，但前面的处理没有过滤，这里先临时处理一下。
		Set<String> processed = new HashSet<>();
		if (fieldFlags) {
			fieldSB.append("\tpublic static final String TABLE_NAME = \"").append(model.getTableName())
					.append("\";\n\n");
			for (ColumnModel col : model.getColumns()) {
				String columnName = col.getColumnName();
				if (processed.contains(columnName)) {
					continue;
				}
				String rawColName = clearQuote(columnName);
				processed.add(columnName);

				fieldSB.append("\tpublic static final String ").append(rawColName.toUpperCase()).append(" = \"")
						.append(columnName).append("\";\n\n");
			}
		}
		body.append(fieldSB.toString()).append("\n\n");
	}

	private static void generateClassEnd(StringBuilder body) {
		body.append("}\n");
	}

	private static void generateClassBegin(Map<String, Object> setting, StringBuilder body, String className) {
		String classDefinition = (String) setting.get(TableModelUtils.OPT_CLASS_DEFINITION);
		body.append(StrUtils.replace(classDefinition, "$1", className)).append(" {\n\n");
	}

	private static int generateAnnotationForClass(TableModel model, StringBuilder body, String className) {
		if (!StringUtils.isEmpty(model.getComment())) {
			body.append("/**\n * ").append(model.getComment()).append("\n */\n");
		}
		if (!className.equals(model.getTableName())) {
			body.append("@Table").append("(name=\"").append(model.getTableName()).append("\")\n");
		}

		// Compound FKEY
		int fkeyCount = 0;
		for (FKeyModel fkey : model.getFkeyConstraints()) {
			if (fkey.getColumnNames().size() <= 1)/* Not compound Fkey */ {
				continue;
			}
			body.append("@FKey");
			if (fkeyCount > 0) {
				body.append(fkeyCount);
			}
			body.append("(");
			fkeyCount++;
			if (!StrUtils.isEmpty(fkey.getFkeyName())) {
				body.append("name=\"").append(fkey.getFkeyName()).append("\", ");
			}
			if (!fkey.getDdl()) {
				body.append("ddl=false, ");
			}
			String fkeyCols = StrUtils.listToString(fkey.getColumnNames());
			fkeyCols = StrUtils.replace(fkeyCols, ",", "\",\"");
			String refCols = StrUtils.arrayToString(fkey.getRefTableAndColumns());
			refCols = StrUtils.replace(refCols, ",", "\",\"");
			body.append("columns={\"").append(fkeyCols).append("\"}, refs={\"").append(refCols).append("\"}");
			body.append(")\n");
		}
		return fkeyCount;
	}

	private static void generatePackage(Map<String, Object> setting, StringBuilder body) {
		String packageName = (String) setting.get(TableModelUtils.OPT_PACKAGE_NAME);
		if (!StrUtils.isEmpty(packageName)) {
			body.append("package ").append(packageName).append(";\n");
		}
		body.append("\n");
	}

	private static void generateImports(Map<String, Object> setting, StringBuilder body) {
		Boolean removeDefaultImports = (Boolean) setting.get(TableModelUtils.OPT_REMOVE_DEFAULT_IMPORTS);
		if (removeDefaultImports == null || !removeDefaultImports) {// default imports
			body.append("import static com.github.drinkjava2.jsqlbox.JAVA8.*;\n");
			body.append("import static com.github.drinkjava2.jsqlbox.SQL.*;\n");
			body.append("import static com.github.drinkjava2.jsqlbox.DB.*;\n");
			body.append("import com.github.drinkjava2.jdbpro.SqlItem;\n");
			body.append("import com.github.drinkjava2.jdialects.annotation.jdia.*;\n");
			body.append("import com.github.drinkjava2.jdialects.annotation.jpa.*;\n");
			body.append("import com.github.drinkjava2.jsqlbox.*;\n");
		}
		String imports = (String) setting.get(TableModelUtils.OPT_IMPORTS);
		if (!StrUtils.isEmpty(imports)) {
			body.append(imports);
		}
		body.append("\n");
	}

	private static void generateFields(TableModel model, Map<String, Object> setting, int fkeyCount,
			StringBuilder body) {
		boolean enablePublicField = Boolean.TRUE.equals(setting.get(TableModelUtils.OPT_PUBLIC_FIELD));
		StringBuilder pkeySB = new StringBuilder();
		StringBuilder normalSB = new StringBuilder();
		// 不知道为什么有些表的column 定义信息是重复的，我不是DBA不清楚原因。
		// 也需要还有什么废弃的标志，但前面的处理没有过滤，这里先临时处理一下。
		Set<String> processed = new HashSet<>();
		StringBuilder sb;
		for (ColumnModel col : model.getColumns()) {
			String columnName = col.getColumnName();
			if (processed.contains(columnName)) {
				continue;
			}
			processed.add(columnName);
			Class<?> javaType = TypeUtils.dialectTypeToJavaType(col.getColumnType());

			if (javaType == null) {
				continue;
			}

			sb = col.getPkey() ? pkeySB : normalSB;

			if (!StringUtils.isEmpty(col.getComment())) {
				sb.append("\t/**\n\t * ").append(col.getComment()).append("\n\t */\n");
			}

			String fieldName = col.getEntityField();

			if (StrUtils.isEmpty(fieldName)) {
				fieldName = transColumnNameToFieldName(columnName);
			}

			// @Id
			if (col.getPkey()) {
				sb.append("\t@Id\n");
			}

			// @Column
			boolean isStr = Type.VARCHAR.equals(col.getColumnType()) || Type.CHAR.equals(col.getColumnType());
			// 255 is JPA's default @Column length
			boolean lenNotEq255 = 255 != col.getLength();
			if (!fieldName.equalsIgnoreCase(columnName) || (isStr && lenNotEq255)) {
				sb.append("\t@Column(");
				sb.append("name=\"").append(columnName).append("\", ");

				if (isStr && lenNotEq255) {
					sb.append("length=").append(col.getLength()).append(", ");
				}
				sb.setLength(sb.length() - 2);
				sb.append(")\n");
			}

			// @SingleFKey
			for (FKeyModel fkey : model.getFkeyConstraints()) {
				/* Not compound Fkey */
				if (fkey.getColumnNames().size() != 1) {
					continue;
				}
				if (!columnName.equalsIgnoreCase(fkey.getColumnNames().get(0))) {
					continue;
				}
				sb.append("\t@SingleFKey");
				sb.append("(");
				fkeyCount++;
				if (!StrUtils.isEmpty(fkey.getFkeyName())) {
					sb.append("name=\"").append(fkey.getFkeyName()).append("\", ");
				}
				if (!fkey.getDdl()) {
					sb.append("ddl=false, ");
				}
				String refCols = StrUtils.arrayToString(fkey.getRefTableAndColumns());
				refCols = StrUtils.replace(refCols, ",", "\",\"");
				sb.append("refs={\"").append(refCols).append("\"}");
				sb.append(")\n");
			}
			String accessModifier = "private";
			if (enablePublicField) {
				accessModifier = "public";
			}
			sb.append("\t").append(accessModifier).append(' ').append(javaType.getSimpleName()).append(' ')
					.append(fieldName).append(";\n\n");
		}

		body.append(pkeySB.toString()).append("\n\n").append(normalSB.toString()).append("\n\n");
	}

	private static void generateGetterAndSetter(TableModel model, Map<String, Object> setting, String className,
			StringBuilder body) {
		boolean linkStyle = Boolean.TRUE.equals(setting.get(TableModelUtils.OPT_LINK_STYLE));
		StringBuilder pkeySB = new StringBuilder();
		StringBuilder normalSB = new StringBuilder();
		// 不知道为什么有些表的column 定义信息是重复的，我不是DBA不清楚原因。
		// 也需要还有什么废弃的标志，但前面的处理没有过滤，这里先临时处理一下。
		Set<String> processed = new HashSet<>();
		StringBuilder sb;
		for (ColumnModel col : model.getColumns()) {
			if (processed.contains(col.getColumnName())) {
				continue;
			}

			processed.add(col.getColumnName());
			// getter
			Class<?> javaType = TypeUtils.dialectTypeToJavaType(col.getColumnType());

			if (javaType == null) {
				continue;
			}

			sb = col.getPkey() ? pkeySB : normalSB;
			String fieldName = col.getEntityField();
			if (StrUtils.isEmpty(fieldName)) {
				fieldName = transColumnNameToFieldName(col.getColumnName());
			}
			String getFieldName = "get" + StrUtils.toUpperCaseFirstOne(fieldName);
			sb.append("\tpublic ").append(javaType.getSimpleName()).append(" ").append(getFieldName).append("(){\n");
			sb.append("\t\treturn ").append(fieldName).append(";\n");
			sb.append("\t}\n\n");

			// settter
			String setFieldName = "set" + StrUtils.toUpperCaseFirstOne(fieldName);
			if (linkStyle) {
				sb.append("\tpublic ").append(className).append(" ").append(setFieldName).append("(")
						.append(javaType.getSimpleName()).append(" ").append(fieldName).append("){\n");
				sb.append("\t\tthis.").append(fieldName).append("=").append(fieldName).append(";\n");
				sb.append("\t\treturn this;\n");
			} else {
				sb.append("\tpublic ").append("void").append(" ").append(setFieldName).append("(")
						.append(javaType.getSimpleName()).append(" ").append(fieldName).append("){\n");
				sb.append("\t\tthis.").append(fieldName).append("=").append(fieldName).append(";\n");
			}
			sb.append("\t}\n\n");
		}
		body.append(pkeySB.toString()).append(normalSB.toString());
	}

}
