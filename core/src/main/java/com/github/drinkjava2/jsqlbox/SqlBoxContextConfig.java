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
package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jdbpro.DbProConfig;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jsqlbox.sharding.ShardingModTool;
import com.github.drinkjava2.jsqlbox.sharding.ShardingRangeTool;
import com.github.drinkjava2.jsqlbox.sharding.ShardingTool;

/**
 * SqlBoxContextConfig class is used to store constructor parameters for build
 * SqlBoxContext
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
public class SqlBoxContextConfig extends DbProConfig {
	protected static Dialect globalNextDialect = null;
	protected static SqlMapperGuesser globalNextSqlMapperGuesser = SqlMapperDefaultGuesser.instance;
	protected static ShardingTool[] globalNextShardingTools = new ShardingTool[] { new ShardingModTool(),
			new ShardingRangeTool() };
	protected static SnowflakeCreator globalNextSnowflakeCreator = null;
	protected static Object[] globalNextSsModels = null;

	private Dialect dialect = globalNextDialect;
	private SqlMapperGuesser sqlMapperGuesser = globalNextSqlMapperGuesser;
	private SnowflakeCreator snowflakeCreator = globalNextSnowflakeCreator;
	private ShardingTool[] shardingTools = globalNextShardingTools;

	public SqlBoxContextConfig() {
		super();
	}

	protected void getterAndSetters______________________() {// NOSONAR
	}

	public Dialect getDialect() {
		return dialect;
	}

	public SqlBoxContextConfig setDialect(Dialect dialect) {
		this.dialect = dialect;
		return this;
	}

	public SqlMapperGuesser getSqlMapperGuesser() {
		return sqlMapperGuesser;
	}

	public void setSqlMapperGuesser(SqlMapperGuesser sqlMapperGuesser) {
		this.sqlMapperGuesser = sqlMapperGuesser;
	}

	public ShardingTool[] getShardingTools() {
		return shardingTools;
	}

	public void setShardingTools(ShardingTool[] shardingTools) {
		this.shardingTools = shardingTools;
	}

	public SnowflakeCreator getSnowflakeCreator() {
		return snowflakeCreator;
	}

	public void setSnowflakeCreator(SnowflakeCreator snowflakeCreator) {
		this.snowflakeCreator = snowflakeCreator;
	}

	protected void staticGlobalNextMethods______________________() {// NOSONAR
	}

	public static Dialect getGlobalNextDialect() {
		return globalNextDialect;
	}

	public static SqlMapperGuesser getGlobalNextSqlMapperGuesser() {
		return globalNextSqlMapperGuesser;
	}

	public static void setGlobalNextSqlMapperGuesser(SqlMapperGuesser sqlMapperGuesser) {
		globalNextSqlMapperGuesser = sqlMapperGuesser;
	}

	public static void setGlobalNextDialect(Dialect dialect) {
		globalNextDialect = dialect;
	}

	public static ShardingTool[] getGlobalNextShardingTools() {
		return globalNextShardingTools;
	}

	public static void setGlobalNextShardingTools(ShardingTool[] shardingTools) {
		globalNextShardingTools = shardingTools;
	}

	public static SnowflakeCreator getGlobalNextSnowflakeCreator() {
		return globalNextSnowflakeCreator;
	}

	public static void setGlobalNextSnowflakeCreator(SnowflakeCreator snowflakeCreator) {
		globalNextSnowflakeCreator = snowflakeCreator;
	}

	public static Object[] getGlobalNextSsModels() {
		return globalNextSsModels;
	}

	public static void setGlobalNextSsModels(Object[] globalNextSsModels) {
		SqlBoxContextConfig.globalNextSsModels = globalNextSsModels;
	}

}