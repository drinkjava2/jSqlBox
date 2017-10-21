/*
 * Copyright (C) 2016 Yong Zhu.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EntityNet is a entity net
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNet {

	private Boolean weaved = false;

	private SqlBoxContext ctx;

	private Map<Class<?>, SqlBox> boxConfigMap = new HashMap<Class<?>, SqlBox>();

	private List<List<Map<String, Object>>> listMaps = new ArrayList<List<Map<String, Object>>>();

	/** The body of the net */
	private Map<Class<?>, List<Object>> body = new HashMap<Class<?>, List<Object>>();

	public EntityNet(SqlBoxContext ctx, List<Map<String, Object>> listMap, Object... netConfigs) {
		this.ctx = ctx;
		join(listMap, netConfigs);
	}

	public void join(List<Map<String, Object>> listMap, Object... netConfigs) {
		weaved = false;
		try {
			if (listMap == null)
				throw new SqlBoxException("Can not build EntityNet for null listMap");
			if (netConfigs != null && netConfigs.length > 0) {
				SqlBox[] boxes = SpecialSqlUtils.netConfigsToSqlBoxes(ctx, netConfigs);
				for (SqlBox box : boxes)
					boxConfigMap.put(box.getEntityClass(), box);
			} else {
				SqlBox[] thdCachedBoxes = SpecialSqlUtils.netBoxConfigBindedToObject.get().get(listMap);
				if (thdCachedBoxes != null && thdCachedBoxes.length > 0) {
					SqlBox[] boxes = thdCachedBoxes;
					for (SqlBox box : boxes)
						boxConfigMap.put(box.getEntityClass(), box);
				}
			}
			this.listMaps.add(listMap);
		} finally {
			SpecialSqlUtils.netBoxConfigBindedToObject.get().remove(listMap);
		}
	}

	public void weave() {
		EntityNetUtils.weave(this);
		weaved = true;
	}

	public List<Object> get(Class<?> entityClass) {
		if (!weaved)
			weave();
		return new ArrayList<Object>();
	}

	// ======getter & setter =======
	public Boolean getWeaved() {
		return weaved;
	}

	public void setWeaved(Boolean weaved) {
		this.weaved = weaved;
	}

	public SqlBoxContext getCtx() {
		return ctx;
	}

	public void setCtx(SqlBoxContext ctx) {
		this.ctx = ctx;
	}

	public Map<Class<?>, SqlBox> getBoxConfigMap() {
		return boxConfigMap;
	}

	public void setBoxConfigMap(Map<Class<?>, SqlBox> boxConfigMap) {
		this.boxConfigMap = boxConfigMap;
	}

	public List<List<Map<String, Object>>> getListMaps() {
		return listMaps;
	}

	public void setListMaps(List<List<Map<String, Object>>> listMaps) {
		this.listMaps = listMaps;
	}

	public Map<Class<?>, List<Object>> getBody() {
		return body;
	}

	public void setBody(Map<Class<?>, List<Object>> body) {
		this.body = body;
	}

}
