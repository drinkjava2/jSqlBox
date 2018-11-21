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

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * The tool to convert TableModel to Java source code
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public abstract class TableModelUtilsOfJavaSrc {

	/**
	 * Map DB column name to entity field name, example: <br/>
	 * user_name -> userName <br/>
	 * USER_NAME -> userName <br/>
	 * User_naMe -> userName <br/>
	 * UserName -> userName <br/>
	 * USERNAME -> uSERNAME <br/>
	 * userName -> userName <br/>
	 * username -> username <br/>
	 */
	private static String transColumnNameToFieldName(String colName) {
		if (StrUtils.isEmpty(colName))
			return colName;
		if (!colName.contains("_"))
			return StrUtils.toLowerCaseFirstOne(colName);
		StringBuilder sb = new StringBuilder();
		char[] chars = colName.toLowerCase().toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '_')
				continue;
			if ((i > 0) && (chars[i - 1]) == '_' && sb.length() > 0)
				sb.append(Character.toUpperCase(c));
			else
				sb.append(c);
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
		if (model.getEntityClass() != null)
			className = model.getEntityClass().getSimpleName();
		else {
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
	 * @param linkStyle
	 *            if true, create linked style setter, otherwise create normal
	 *            setter
	 * @param activeRecord
	 *            if true, build a jSqlBox ActiveRecord Entity class, otherwise
	 *            build a POJO class
	 * @param packageName
	 *            Optional, the package name of this entity class
	 * @return Java Bean source code of entity
	 */
	public static String modelToJavaSourceCode(TableModel model, boolean linkStyle, boolean activeRecord,
			String packageName) {
		// head
		StringBuilder body = new StringBuilder();
		if (!StrUtils.isEmpty(packageName))
			body.append("package ").append(packageName).append(";\n");
		body.append("import com.github.drinkjava2.jdialects.annotation.jdia.*;\n");
		body.append("import com.github.drinkjava2.jdialects.annotation.jpa.*;\n");
		if (activeRecord) {
			body.append("import com.github.drinkjava2.jsqlbox.*;\n");
			body.append("import static com.github.drinkjava2.jsqlbox.JSQLBOX.*;\n");
		}
		body.append("\n");

		// @table
		String className = getClassNameFromTableModel(model);
		if (!className.equals(model.getTableName())) {
			body.append("@Table").append("(name=\"").append(model.getTableName()).append("\")\n");
		}

		// Compound FKEY
		int fkeyCount = 0;
		for (FKeyModel fkey : model.getFkeyConstraints()) {
			if (fkey.getColumnNames().size() <= 1)// Not compound Fkey
				continue;
			body.append("@FKey");
			if (fkeyCount > 0)
				body.append(fkeyCount);
			body.append("(");
			fkeyCount++;
			if (!StrUtils.isEmpty(fkey.getFkeyName()))
				body.append("name=\"").append(fkey.getFkeyName()).append("\", ");
			if (!fkey.getDdl())
				body.append("ddl=false, ");
			String fkeyCols = StrUtils.listToString(fkey.getColumnNames());
			fkeyCols = StrUtils.replace(fkeyCols, ",", "\",\"");
			String refCols = StrUtils.arrayToString(fkey.getRefTableAndColumns());
			refCols = StrUtils.replace(refCols, ",", "\",\"");
			body.append("columns={\"").append(fkeyCols).append("\"}, refs={\"").append(refCols).append("\"}");
			body.append(")\n");
		}

		// class
		if (activeRecord)
			body.append("public class ").append(className).append(" extends ActiveRecord<").append(className)
					.append("> {\n");
		else
			body.append("public class ").append(className).append(" {\n");

		// Fields
		StringBuilder pkeySB = new StringBuilder();
		StringBuilder normalSB = new StringBuilder();
		StringBuilder sb = null;
		for (ColumnModel col : model.getColumns()) {
			Class<?> javaType = TypeUtils.dialectTypeToJavaType(col.getColumnType());
			if (javaType == null)
				continue;
			sb = col.getPkey() ? pkeySB : normalSB;
			String fieldName = col.getEntityField();
			if (StrUtils.isEmpty(fieldName))
				fieldName = transColumnNameToFieldName(col.getColumnName());
			// @Id
			if (col.getPkey())
				sb.append("  @Id\n");

			// @Column
			boolean isStr = Type.VARCHAR.equals(col.getColumnType()) || Type.CHAR.equals(col.getColumnType());
			if (!fieldName.equalsIgnoreCase(col.getColumnName()) || (isStr && 255 != col.getLength())) {
				sb.append("  @Column(name=\"").append(col.getColumnName()).append("\"");
				if (isStr && 255 != col.getLength())
					sb.append(", length=").append(col.getLength());
				sb.append(")\n");
			}

			// @SingleFKey
			for (FKeyModel fkey : model.getFkeyConstraints()) {
				if (fkey.getColumnNames().size() != 1)// Not compound Fkey
					continue;
				if (!col.getColumnName().equalsIgnoreCase(fkey.getColumnNames().get(0)))
					continue;
				sb.append("  @SingleFKey");
				sb.append("(");
				fkeyCount++;
				if (!StrUtils.isEmpty(fkey.getFkeyName()))
					sb.append("name=\"").append(fkey.getFkeyName()).append("\", ");
				if (!fkey.getDdl())
					sb.append("ddl=false, ");
				String refCols = StrUtils.arrayToString(fkey.getRefTableAndColumns());
				refCols = StrUtils.replace(refCols, ",", "\",\"");
				sb.append("refs={\"").append(refCols).append("\"}");
				sb.append(")\n");
			}
			sb.append("  private ").append(javaType.getSimpleName()).append(" ").append(fieldName).append(";\n\n");
		}
		body.append(pkeySB.toString()).append(normalSB.toString());

		pkeySB.setLength(0);
		normalSB.setLength(0);
		for (ColumnModel col : model.getColumns()) {
			// getter
			Class<?> javaType = TypeUtils.dialectTypeToJavaType(col.getColumnType());
			if (javaType == null)
				continue;
			sb = col.getPkey() ? pkeySB : normalSB;
			String fieldName = col.getEntityField();
			if (StrUtils.isEmpty(fieldName))
				fieldName = transColumnNameToFieldName(col.getColumnName());
			String getFieldName = "get" + StrUtils.toUpperCaseFirstOne(fieldName);
			sb.append("  public ").append(javaType.getSimpleName()).append(" ").append(getFieldName).append("(){\n");
			sb.append("    return ").append(fieldName).append(";\n");
			sb.append("  }\n\n");

			// settter
			String setFieldName = "set" + StrUtils.toUpperCaseFirstOne(fieldName);
			sb.append("  public ").append(linkStyle ? className : "void").append(" ").append(setFieldName).append("(")
					.append(javaType.getSimpleName()).append(" ").append(fieldName).append("){\n");
			sb.append("    this.").append(fieldName).append("=").append(fieldName).append(";\n");
			if (linkStyle)
				sb.append("    return this;\n");
			sb.append("  }\n\n");
		}
		body.append(pkeySB.toString()).append(normalSB.toString());

		body.append("}");

		return body.toString();
	}

}
