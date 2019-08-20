/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox.gtx;

/**
 * Store undo log in memory
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxLog {
	private String logType;
	private Object entity;
	private Integer gtxDB;
	private String gtxTB;

	public GtxLog(String logType, Object entity) {
		this.logType = logType;
		this.entity = entity;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public Integer getGtxDB() {
		return gtxDB;
	}

	public void setGtxDB(Integer gtxDB) {
		this.gtxDB = gtxDB;
	}

	public String getGtxTB() {
		return gtxTB;
	}

	public void setGtxTB(String gtxTB) {
		this.gtxTB = gtxTB;
	}

}