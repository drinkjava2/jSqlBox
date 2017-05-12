package com.github.drinkjava2.jsqlbox;

import java.util.Set;

/**
 * <pre>
 Each entity bean should extends from EntityBase class (for DK7 and below) or
 simply implements Entity interface(For JDK8+), This project always release 2 different versions:
 
JAVA7:
    <groupId>com.github.drinkjava2</groupId>
    <artifactId>jsqlbox</artifactId>
    <version>1.0.0-JRE7-SNAPSHOT</version>
 
JAVA8:
    <groupId>com.github.drinkjava2</groupId>
    <artifactId>jsqlbox</artifactId>
    <version>1.0.0-JRE8-SNAPSHOT</version>
 
  To release in Java8, need use default methods replace interface virtual methods
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
//@formatter:off
@SuppressWarnings("all")
public interface Entity {

	// below methods are for Java7 only 
//	public SqlBox box();
//
//	public SqlBox box(SqlBoxContext context);
//
//	public String table();
//
//	public String all();
//
//	public <T> T insert();
//
//	public <T> T update();
//
//	public <T> T delete();
//
//	public String nextUUID();
//
//	public String fieldID(String realColumnName);
//
//	public String alias(String realColumnName);
//
//	public String aliasByFieldID(String fieldID);
//
//	public <T> T configAlias(String tableAlias);
//
//	public String configMapping(String... mappingSql);
//
//	public String automaticQuerySQL();
//
//	public <T> Set<T> getUniqueNodeSet(Class<?> entityClass);
//
//	public <T> Set<T> getChildNodeSet(Class<?> entityClass, String fieldID);
//
//	public <T> Set<T> getChildNodeSet(Class<?> entityClass);
//
//	public <T> T getChildNode(Class<?> entityClass, String fieldID);
//
//	public <T> T getChildNode(Class<?> entityClass);
//
//	public <T> T getParentNode(Class<?> entityClass);
//
//	public String paginate(int pageNumber, int pageSize, String... sql);
//	
 
	 //Below are for JAVA8 only
	public default SqlBox box() {
		return SqlBoxContext.getDefaultBox(this);
	}

	public default SqlBox box(SqlBoxContext context) {
		return context.getBox(this);
	}

	public default String table() {
		return box().table();
	}

	public default String all() {
		return box().all();
	}

	public default <T> T insert() {
		box().insert();
		return (T) this;
	}

	public default <T> T update() {
		box().update();
		return (T) this;
	}

	public default <T> T delete() {
		box().delete();
		return (T) this;
	}

	public default String nextUUID() {
		SqlBoxContext ctx = box().getSqlBoxContext();
		return (String) ctx.getDefaultUUIDGenerator().getNextID(ctx);
	}

	public default String fieldID(String realColumnName) {
		return box().fieldID(realColumnName);
	}

	public default String alias(String realColumnName) {
		return box().aliasByRealColumnName(realColumnName);
	}

	public default String aliasByFieldID(String fieldID) {
		return box().aliasByFieldID(fieldID);
	}

	public default <T> T configAlias(String tableAlias) {
		box().configAlias(tableAlias);
		return (T) this;
	}

	public default String configMapping(String... mappingSql) {
		return box().configMapping(mappingSql);
	}

	public default String automaticQuerySQL() {
		return box().automaticQuerySQL();
	}

	public default <T> Set<T> getUniqueNodeSet(Class<?> entityClass) {
		return (Set<T>) box().getUniqueNodeSet(entityClass);
	}

	public default <T> Set<T> getChildNodeSet(Class<?> entityClass, String fieldID) {
		return box().getChildNodeSet(entityClass, fieldID);
	}

	public default <T> Set<T> getChildNodeSet(Class<?> entityClass) {
		return box().getChildNodeSet(entityClass, null);
	}

	public default <T> T getChildNode(Class<?> entityClass, String fieldID) {
		return box().getChildNode(entityClass, fieldID);
	}

	public default <T> T getChildNode(Class<?> entityClass) {
		return box().getChildNode(entityClass, null);
	}

	public default <T> T getParentNode(Class<?> entityClass) {
		return box().getParentNode(entityClass);
	}

	public default String paginate(int pageNumber, int pageSize, String... sql) {
		StringBuilder sb = new StringBuilder();
		for (String str : sql)
			sb.append(str);
		return box().getDialect().paginate(pageNumber, pageSize, sb.toString());
	}
 
}