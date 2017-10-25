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

import static com.github.drinkjava2.jsqlbox.SqlBoxContext.netProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * EntityNet is a entity net
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNet {

	private Boolean weaved = false;
	private Object[] netConfigs;

	private List<Map<String, Object>> listMaps = new ArrayList<Map<String, Object>>();

	/** The body of the net */
	private Map<Class<?>, List<Object>> body = new HashMap<Class<?>, List<Object>>();

	public EntityNet(List<Map<String, Object>> listMap, Object... netConfigs) {
		joinList(listMap, netConfigs);
	}

	/**
	 * Load all rows in database tables listed in configs as EntityNet, usage
	 * example: loadAll(ctx, User.class, Email.class); <br/>
	 * or loadAll(ctx, new User(), new Email());
	 */
	public static EntityNet loadAll(SqlBoxContext ctx, Object... configs) {
		EntityNet net = new EntityNet(new ArrayList<Map<String, Object>>(), configs);
		SqlBox[] boxes = NetSqlExplainer.netConfigsToSqlBoxes(ctx, configs);
		for (SqlBox box : boxes) {
			TableModel t = box.getTableModel();
			List<Map<String, Object>> mapList1 = ctx.nQuery(new MapListHandler(netProcessor(configs)),
					"select " + t.getTableName() + ".** from " + t.getTableName() + " as " + t.getTableName());
			net.joinList(mapList1, configs);
		}
		return net;
	}

	private static Object[] concatArray(Object[] first, Object[] second) {
		Object[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public void joinList(List<Map<String, Object>> listMap, Object... configs) {
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
