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

	private SqlBoxContext ctx;

	private SqlBox[] configBoxes;

	/** The body of the net */
	private Map<Class<?>, List<Object>> body = new HashMap<Class<?>, List<Object>>();

	public EntityNet(SqlBoxContext ctx, List<Map<String, Object>> listMap, Object... netConfigs) {
		try {
			if (netConfigs != null && netConfigs.length > 0) {
				configBoxes = SpecialSqlUtils.netConfigsToSqlBoxes(ctx, netConfigs);
			} else {
				SqlBox[] thdCachedBoxes = SpecialSqlUtils.netBoxConfigBindedToObject.get().get(listMap);
				if (thdCachedBoxes != null && thdCachedBoxes.length > 0)
					configBoxes = thdCachedBoxes;
			}
			this.ctx = ctx;
		} finally {
			SpecialSqlUtils.netBoxConfigBindedToObject.get().remove(listMap);
		}
		EntityNetUtils.weave(this, listMap);
	}

	// ======getter & setter =======
	public SqlBoxContext getCtx() {
		return ctx;
	}

	public void setCtx(SqlBoxContext ctx) {
		this.ctx = ctx;
	}

	public SqlBox[] getConfigBoxes() {
		return configBoxes;
	}

	public void setConfigBoxes(SqlBox[] configBoxes) {
		this.configBoxes = configBoxes;
	}

	public Map<Class<?>, List<Object>> getBody() {
		return body;
	}

	public void setBody(Map<Class<?>, List<Object>> body) {
		this.body = body;
	}

}
