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

import java.util.ArrayList;
import java.util.List;

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
	private SqlTemplateEngine templateEngine = DbPro.getGlobalNextTemplateEngine();
	private DbProLogger logger = DbPro.getGlobalNextLogger();
	private Boolean allowSqlSql = DbPro.getGlobalNextAllowShowSql();
	private Boolean useMaster = DbPro.getGlobalNextUseMaster();
	private Boolean ussSlave = DbPro.getGlobalNextUseSlave();
	private Integer batchSize = DbPro.getGlobalNextBatchSize();
	private ConnectionManager connectionManager = DbPro.getGlobalNextConnectionManager();
	private SqlHandler[] sqlHandlers = DbPro.getGlobalNextSqlHandlers();
	private IocTool iocTool = DbPro.getGlobalNextIocTool();
	private List<DbPro> slaves;

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

	public List<DbPro> getSlaves() {
		return slaves;
	}

	public void setSlaves(List<DbPro> slaves) {
		this.slaves = slaves;
	}

	public void addSlave(DbPro dbPro) {
		if (slaves == null)
			slaves = new ArrayList<DbPro>();
		slaves.add(dbPro);
	}

	public Boolean getUseMaster() {
		return useMaster;
	}

	public void setUseMaster(Boolean useMaster) {
		this.useMaster = useMaster;
	}

	public Boolean getUssSlave() {
		return ussSlave;
	}

	public void setUssSlave(Boolean ussSlave) {
		this.ussSlave = ussSlave;
	}

}