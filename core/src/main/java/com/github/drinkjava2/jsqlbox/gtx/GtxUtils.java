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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Public static methods of GTX
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxUtils {
	public static Map<Class<?>, TableModel> globalGtxTableModelCache = new ConcurrentHashMap<Class<?>, TableModel>();

	private static final String GTX_ID = "gtx_id";
	private static final String GTX_TYPE = "gtx_type";// tx log type, can be update/exist/insert/delete
	private static final String GTX_LOG_ID = "gtx_log_id";// log number, this is the P-Key of this log table

	/**
	 * Convert an entity class to gtxLog entity class, i.e., add some columns for it
	 */
	public TableModel toGTxlogModel(Class<?> entityClass) {
		TableModel model = globalGtxTableModelCache.get(entityClass);
		if (model != null)
			return model;
		TableModel t = TableModelUtils.entity2Model(entityClass);
		t.setIdGenerators(null);
		t.setIndexConsts(null);
		t.setUniqueConsts(null);
		t.column(GTX_ID).VARCHAR(32);
		t.column(GTX_TYPE).VARCHAR(14);
		t.column(GTX_LOG_ID).LONG().id(); // ID is assigned by outside
		globalGtxTableModelCache.put(entityClass, t);
		return t;
	}
}