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
package com.github.drinkjava2.jsqlbox.handler;

import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * MapListWrap is a POJO to store a List<Map<String, Object>> + TableModel[]
 * pair, usually used to build or join into an EntityNet.
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
@SuppressWarnings("all")
public class MapListWrap {
	List<Map<String, Object>> mapList;
	TableModel[] config; // the config tableModels

	public MapListWrap() {
		// Default public Constructor
	}

	public MapListWrap(List<Map<String, Object>> mapList, TableModel[] config) {
		this.mapList = mapList;
		this.config = config;
	}

	public List<Map<String, Object>> getMapList() {
		return mapList;
	}

	public void setMapList(List<Map<String, Object>> mapList) {
		this.mapList = mapList;
	}

	public TableModel[] getConfig() {
		return config;
	}

	public void setConfig(TableModel[] config) {
		this.config = config;
	}

}
