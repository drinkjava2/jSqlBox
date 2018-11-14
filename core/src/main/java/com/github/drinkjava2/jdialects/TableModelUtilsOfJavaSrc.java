/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

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
	private static String getClassNameFromTableModel(TableModel model) {
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

	@Table(name = "demo_entity")
	public class DemoEntity extends ActiveRecord<DemoEntity> {
		@Id
		private Integer id;

		@Column(length = 10)
		private String name;

		@Column(name = "cust_id")
		@SingleFKey(refs = { "sys_customer", "id" })
		private Integer custId;

		DemoEntity demoEntity;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getCustId() {
			return custId;
		}

		public void setCustId(Integer custId) {
			this.custId = custId;
		}

		public DemoEntity getDemoEntity() {
			return demoEntity;
		}

		public void setDemoEntity(DemoEntity demoEntity) {
			this.demoEntity = demoEntity;
		}

	}

	/**
	 * Convert a TablemModel instance to Java entity class source code
	 * 
	 * @param model
	 *            The TableModel instance
	 * @param linkStyleGetterSetter
	 *            if true, create linked style setter/getter, otherwise create
	 *            normal getter/setter
	 * @param activeRecord
	 *            if true, build a jSqlBox ActiveRecord Entity class, otherwise
	 *            build a POJO class
	 * @param optionalPackageName
	 *            Optional, the package name of this entity class
	 * @return Java Bean source code of entity
	 */
	public static String modelToJavaSourceCode(TableModel model, boolean linkStyleGetterSetter, boolean activeRecord,
			String optionalPackageName) {
		//head
		StringBuilder head = new StringBuilder();
		if (!StrUtils.isEmpty(optionalPackageName))
			head.append("package ").append(optionalPackageName).append(";\n");
		head.append("import com.github.drinkjava2.jdialects.annotation.jdia.*;\n");
		head.append("import com.github.drinkjava2.jdialects.annotation.jpa.*;\n");
		if (activeRecord) {
			head.append("import com.github.drinkjava2.jsqlbox.*;\n");
			head.append("import static com.github.drinkjava2.jsqlbox.JSQLBOX.*;\n");
		}
		head.append("\n");

		 //Class
		StringBuilder body = new StringBuilder();
		String className = getClassNameFromTableModel(model);
		if (!className.equals(model.getTableName())) {
			body.append("@Table").append("");
		} 
		body.append("\n");
		if (activeRecord)
			body.append("public class ").append(className).append(" extends ActiveRecord<").append(className)
					.append("> {\n");
		else
			body.append("public class ").append(className).append(" {\n");
 
		//Fields
		for (ColumnModel col : model.getColumns()) { 
			Class<?> javaType = TypeUtils.typeToJavaClass(col.getColumnType());
			if (javaType == null)
				continue;
			String fieldName = col.getEntityField();
			if (StrUtils.isEmpty(fieldName))
				fieldName = transColumnNameToFieldName(col.getColumnName());

			if (!fieldName.equalsIgnoreCase(col.getColumnName()) || 255 != col.getLength()) {
				body.append("   @Column(name=\"").append(col.getColumnName()).append("\"");
				if (255 != col.getLength())
					body.append(", length=").append(col.getLength());
				body.append(")\n");
			}
			body.append("   private ").append(javaType.getSimpleName()).append(" ").append(fieldName).append(";\n");
		}

		return head.toString() + body.toString();
	}

	public static void main(String[] args) {
		TableModel model = TableModelUtilsOfEntity.entity2ReadOnlyModel(DemoEntity.class);
		System.out.println(modelToJavaSourceCode(model, true, true, "somepackage"));
	}
}
