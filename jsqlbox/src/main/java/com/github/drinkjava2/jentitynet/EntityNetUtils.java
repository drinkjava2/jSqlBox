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

import static com.github.drinkjava2.jsqlbox.SqlBoxContext.netProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.NetSqlExplainer;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * EntityNetUtils is utility class store static methods about EntityNet
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNetUtils {
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

	public static void weave(EntityNet net) {
		//TODO work at here
		// SqlBox[] boxes = net.get();
		// if (boxes == null || boxes.length == 0)
		// throw new SqlBoxException("Can not weave EntityNet with empty config
		// parameters");
		//
		net.setWeaved(true);
	}
 
}
