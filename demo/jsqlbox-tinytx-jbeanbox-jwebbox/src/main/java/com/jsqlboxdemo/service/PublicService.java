package com.jsqlboxdemo.service;

import java.util.List;

import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * This PublicService class or subclass of it should use a AOP tool to build a
 * singleton instance to make methods to controlled in transaction.
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class PublicService {

	/** If contexts is empty, will use globalSqlBoxContext */
	protected static SqlBoxContext getContext(SqlBoxContext... contexts) {
		SqlBoxContext ctx;
		if (contexts == null || contexts.length == 0)
			ctx = SqlBoxContext.getGlobalSqlBoxContext();
		else
			ctx = contexts[0];
		return ctx;
	}

	public void doInsert(ActiveRecord ar) {
		ar.insert();
	}

	public void doUpdate(ActiveRecord ar) {
		ar.update();
	}

	public void doDelete(ActiveRecord ar) {
		ar.delete();
	}

	/** Load a entity, if contexts is empty, will use globalSqlBoxContext */
	public <T> T doLoad(Class<T> entityClass, Object pkey, SqlBoxContext... contexts) {
		return getContext(contexts).load(entityClass, pkey);
	}

	/**
	 * Load a entity list, if contexts is empty, will use globalSqlBoxContext
	 */
	public <T> List<T> doLoadAll(Class<T> arClass, SqlBoxContext... contexts) {
		return getContext(contexts).netLoad(arClass).getAllEntityList(arClass);
	}

}
