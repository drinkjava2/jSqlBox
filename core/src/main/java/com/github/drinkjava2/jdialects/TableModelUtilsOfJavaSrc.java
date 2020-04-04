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

import com.github.drinkjava2.jdialects.springsrc.utils.StringUtils;
import java.util.HashSet;
import java.util.Map;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import java.util.Set;

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
	 * userName -> username <br/>
	 * username -> username <br/>
	 */
	private static String transColumnNameToFieldName(String colName) {
		if (StrUtils.isEmpty(colName)) return colName;

		if (!colName.contains("_")) {
			//return StrUtils.toLowerCaseFirstOne(colName);
			// 这才是符合 java bean 规范
			return colName.toLowerCase();
		}
		StringBuilder sb = new StringBuilder();
		char[] chars = colName.toLowerCase().toCharArray();
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
	 * USERNAME -> USERNAME <br/>
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
			className = StrUtils.toUpperCaseFirstOne(transColumnNameToFieldName(model.getTableName()));
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
	 *            setting.put("linkStyle", false);
	 *            setting.put("activeRecord", false);
	 *            setting.put("activeEntity", true);
	 *            setting.put("packageName", "somepackage");
	 *            setting.put("fieldFlags", true);
	 *            </pre>
	 * 
	 * @return Java Bean source code of entity
	 */
	public static String modelToJavaSourceCode(TableModel model, Map<String, Object> setting) {
		boolean linkStyle = Boolean.TRUE.equals(setting.get("linkStyle"));
		boolean fieldFlags = Boolean.TRUE.equals(setting.get("fieldFlags"));
		String classDefinition = (String) setting.get("classDefinition");
		String packageName = (String) setting.get("packageName");
		String imports = (String) setting.get("imports");
		// head
		StringBuilder body = new StringBuilder();
		if (!StrUtils.isEmpty(packageName)) {
			body.append("package ").append(packageName).append(";\n");
		}
		if (!StrUtils.isEmpty(imports)) {
			body.append(imports);
		}
		body.append("\n");

		// @table
		String className = getClassNameFromTableModel(model);
		if (!StringUtils.isEmpty(model.getComment())) {
			body.append("/**\n * ").append(model.getComment()).append("\n */\n");
		}
		if (!className.equals(model.getTableName())) {
			body.append("@Table").append("(name=\"").append(model.getTableName()).append("\")\n");
		}

		// Compound FKEY
		int fkeyCount = 0;
		for (FKeyModel fkey : model.getFkeyConstraints()) {
			if (fkey.getColumnNames().size() <= 1)/* Not compound Fkey*/ continue;
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

		// class
		body.append(StrUtils.replace(classDefinition, "$1", className)).append(" {\n\n");

		// Fields
		StringBuilder fieldSB = new StringBuilder();
		StringBuilder pkeySB = new StringBuilder();
		StringBuilder normalSB = new StringBuilder();
		StringBuilder sb = null;
		// 不知道为什么有些表的column 定义信息是重复的，我不是DBA不清楚原因。
		// 也需要还有什么废弃的标志，但前面的处理没有过滤，这里先临时处理一下。
		Set<String> processed = new HashSet<>();
		// fieldStaticNames
		if (fieldFlags) {
			for (ColumnModel col : model.getColumns()) {
				if (processed.contains(col.getColumnName())) {
					continue;
				}
				processed.add(col.getColumnName());

				fieldSB.append("\tpublic static final String ").append(col.getColumnName().toUpperCase()).append(" = \"").append(col.getColumnName()) .append("\";\n");
			}
		}
		processed.clear();
		for (ColumnModel col : model.getColumns()) {
			if (processed.contains(col.getColumnName()))  continue;
			processed.add(col.getColumnName());
			Class<?> javaType = TypeUtils.dialectTypeToJavaType(col.getColumnType());

			if (javaType == null) continue;

			sb = col.getPkey() ? pkeySB : normalSB;

			if (!StringUtils.isEmpty(col.getComment())) {
				sb.append("\t/**\n\t * ").append(col.getComment()).append("\n\t */\n");
			}

			String fieldName = col.getEntityField();

			if (StrUtils.isEmpty(fieldName)) {
				fieldName = transColumnNameToFieldName(col.getColumnName());
			}

			// @Id
			if (col.getPkey()) {
				sb.append("\t@Id\n");
			}

			// @Column
			boolean isStr = Type.VARCHAR.equals(col.getColumnType()) || Type.CHAR.equals(col.getColumnType());
			// 好魔幻的数字
			boolean lenNotEq250 = 250 != col.getLength();
			if (!fieldName.equalsIgnoreCase(col.getColumnName()) || (isStr && lenNotEq250)) {
				sb.append("\t@Column(");
				sb.append("name=\"").append(col.getColumnName()).append("\", ");

				if (isStr && lenNotEq250) {
					sb.append("length=").append(col.getLength()).append(", ");
				}
				sb.setLength(sb.length() - 2);
				sb.append(")\n");
			}

			// @SingleFKey
			for (FKeyModel fkey : model.getFkeyConstraints()) {
				/* Not compound Fkey*/
				if (fkey.getColumnNames().size() != 1) continue;
				if (!col.getColumnName().equalsIgnoreCase(fkey.getColumnNames().get(0))) continue;
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


			sb.append(" \tprivate ").append(javaType.getSimpleName()).append(" ").append(fieldName).append(";\n\n");
		}
		body.append(fieldSB.toString()).append("\n\n")
		    .append(pkeySB.toString()).append("\n\n")
		    .append(normalSB.toString()).append("\n\n");
		fieldSB.setLength(0);
		pkeySB.setLength(0);
		normalSB.setLength(0);
		processed.clear();
		for (ColumnModel col : model.getColumns()) {
			if (processed.contains(col.getColumnName())) continue;

			processed.add(col.getColumnName());
			// getter
			Class<?> javaType = TypeUtils.dialectTypeToJavaType(col.getColumnType());

			if (javaType == null) continue;

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

		body.append("}\n");

		return body.toString();
	}

}
