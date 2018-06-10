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
package com.github.drinkjava2.jdbpro;

import com.github.drinkjava2.jdbpro.DbProLogger.DefaultDbProLogger;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * DbProConfig class is used to store constructor parameters for build DbPro
 * instance, this is a transient object, never try to re-use it, re-use it will
 * break thread safe of jDbPro
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
public class DbProConfig {
	protected static Boolean globalNextAllowShowSql = false;
	protected static SqlOption globalNextMasterSlaveOption = SqlOption.USE_AUTO;
	protected static ConnectionManager globalNextConnectionManager = null;
	protected static DbProLogger globalNextLogger = DefaultDbProLogger.getLog(ImprovedQueryRunner.class);
	protected static Integer globalNextBatchSize = 300;
	protected static SqlTemplateEngine globalNextTemplateEngine = BasicSqlTemplate.instance();
	protected static IocTool globalNextIocTool = null;
	protected static SqlHandler[] globalNextSqlHandlers = null;
	protected static SpecialSqlItemPreparer[] globalNextSpecialSqlItemPreparers = null;
	
	
	private SqlTemplateEngine templateEngine = globalNextTemplateEngine;
	private DbProLogger logger = globalNextLogger;
	private Boolean allowSqlSql = globalNextAllowShowSql;
	private ConnectionManager connectionManager = globalNextConnectionManager;
	private IocTool iocTool =globalNextIocTool ;
	private SqlOption masterSlaveOption = globalNextMasterSlaveOption ;
	private Integer batchSize = globalNextBatchSize ;
	private SqlHandler[] sqlHandlers = globalNextSqlHandlers ;
	private SpecialSqlItemPreparer[] specialSqlItemPreparers = globalNextSpecialSqlItemPreparers ;
	private DbPro[] slaves;
	private DbPro[] masters;

	public SqlTemplateEngine getTemplateEngine() {
		return templateEngine;
	}

	public void setTemplateEngine(SqlTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public DbProLogger getLogger() {
		return logger;
	}

	public void setLogger(DbProLogger logger) {
		this.logger = logger;
	}

	public Boolean getAllowSqlSql() {
		return allowSqlSql;
	}

	public void setAllowSqlSql(Boolean allowSqlSql) {
		this.allowSqlSql = allowSqlSql;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public SqlHandler[] getSqlHandlers() {
		return sqlHandlers;
	}

	public void setSqlHandlers(SqlHandler[] sqlHandlers) {
		this.sqlHandlers = sqlHandlers;
	}

	public IocTool getIocTool() {
		return iocTool;
	}

	public void setIocTool(IocTool iocTool) {
		this.iocTool = iocTool;
	}

	public DbPro[] getSlaves() {
		return slaves;
	}

	public void setSlaves(DbPro[] slaves) {
		this.slaves = slaves;
	}

	public DbPro[] getMasters() {
		return masters;
	}

	public void setMasters(DbPro[] masters) {
		this.masters = masters;
	}

	public SqlOption getMasterSlaveSelect() {
		return masterSlaveOption;
	}

	public void setMasterSlaveSelect(SqlOption masterSlaveSelect) {
		this.masterSlaveOption = masterSlaveSelect;
	}

	public SpecialSqlItemPreparer[] getSpecialSqlItemPreparers() {
		return specialSqlItemPreparers;
	}

	public void setSpecialSqlItemPreparers(SpecialSqlItemPreparer[] specialSqlItemPreparers) {
		this.specialSqlItemPreparers = specialSqlItemPreparers;
	}

	protected void staticGlobalNextMethods_____________________() {// NOSONAR
	}

	public static DbProLogger getGlobalNextLogger() {
		return globalNextLogger;
	}

	public static void setGlobalNextLogger(DbProLogger dbProLogger) {
		globalNextLogger = dbProLogger;
	}

	public static Integer getGlobalNextBatchSize() {
		return globalNextBatchSize;
	}

	public static void setGlobalNextBatchSize(Integer batchSize) {
		globalNextBatchSize = batchSize;
	}

	public static SqlTemplateEngine getGlobalNextTemplateEngine() {
		return globalNextTemplateEngine;
	}

	public static void setGlobalNextTemplateEngine(SqlTemplateEngine sqlTemplateEngine) {
		globalNextTemplateEngine = sqlTemplateEngine;
	}

	public static Boolean getGlobalNextAllowShowSql() {
		return globalNextAllowShowSql;
	}

	public static void setGlobalNextAllowShowSql(Boolean allowShowSql) {
		 globalNextAllowShowSql = allowShowSql;
	}

	public static SqlOption getGlobalNextMasterSlaveOption() {
		return globalNextMasterSlaveOption;
	}

	public static void setGlobalNextMasterSlaveOption(SqlOption  masterSlaveOption) {
		 globalNextMasterSlaveOption = masterSlaveOption;
	}

	public static ConnectionManager getGlobalNextConnectionManager() {
		return globalNextConnectionManager;
	}

	public static void setGlobalNextConnectionManager(ConnectionManager connectionManager) {
	 globalNextConnectionManager = connectionManager;
	}

	public static SqlHandler[] getGlobalNextSqlHandlers() {
		return globalNextSqlHandlers;
	}

	public static void setGlobalNextSqlHandlers(SqlHandler... sqlHandlers) {
		globalNextSqlHandlers = sqlHandlers;
	}

	public static SpecialSqlItemPreparer[] getGlobalNextSpecialSqlItemPreparers() {
		return globalNextSpecialSqlItemPreparers;
	}

	public static void setGlobalNextSpecialSqlItemPreparers(SpecialSqlItemPreparer[] specialSqlItemPreparers) {
		globalNextSpecialSqlItemPreparers = specialSqlItemPreparers;
	}

	public static IocTool getGlobalNextIocTool() {
		return globalNextIocTool;
	}

	public static void setGlobalNextIocTool(IocTool nextIocTool) {
		globalNextIocTool = nextIocTool;
	}
}