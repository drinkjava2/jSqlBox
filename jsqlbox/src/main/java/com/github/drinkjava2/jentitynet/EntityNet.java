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
package com.github.drinkjava2.jentitynet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * EntityNet is a entity net
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNet {

	private Boolean weaved = false;
	
	private TableModel[] netConfigs;

	private List<Map<String, Object>> listMaps = new ArrayList<Map<String, Object>>();

	/** The body of the net */
	private Map<Class<?>, List<Object>> body = new HashMap<Class<?>, List<Object>>();
	
	
	public EntityNet(List<Map<String, Object>> listMap, TableModel... configs) {
		joinList(listMap, netConfigs);
	}



	private static Object[] concatArray(Object[] first, Object[] second) {
		Object[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public void joinList(List<Map<String, Object>> listMap, TableModel... configs) {
		weaved = false;
		if (listMap == null)
			throw new SqlBoxException("Can not join null listMap");
		if (configs != null && configs.length > 0) {
			if (netConfigs == null)
				netConfigs = configs;
			else
				netConfigs = concatArray(netConfigs, configs);
		}
		for (Map<String, Object> map : listMap) {
			listMaps.add(map);
		}
	}

	public void weave() {
		EntityNetUtils.weave(this);
	}

	public List<Object> get(Class<?> entityClass) {
		if (!weaved)
			weave();
		// TODO work at here
		return new ArrayList<Object>();
	}

	// ======getter & setter =======
	public Boolean getWeaved() {
		return weaved;
	}

	public void setWeaved(Boolean weaved) {
		this.weaved = weaved;
	}

	public Object[] getNetConfigs() {
		return netConfigs;
	}

	public void setNetConfigs(Object... netConfigs) {
		this.netConfigs = netConfigs;
	}

	public List<Map<String, Object>> getListMaps() {
		return listMaps;
	}

	public void setListMaps(List<Map<String, Object>> listMaps) {
		this.listMaps = listMaps;
	}

	public Map<Class<?>, List<Object>> getBody() {
		return body;
	}

	public void setBody(Map<Class<?>, List<Object>> body) {
		this.body = body;
	}

}
