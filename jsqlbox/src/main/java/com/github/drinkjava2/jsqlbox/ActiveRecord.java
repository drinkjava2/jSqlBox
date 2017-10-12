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

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Entity class extended from ActiveRecord will get CRUD methods, see below
 * difference in jSqlBox to save ActiveRecord entity and POJO entity into
 * database:
 * 
 * <pre>
 * ActiveRecord Entity:    
 *     SqlBoxContext.setDefaultContext(new SqlBoxContext(dataSource));           
 *     entity.insert(); 
 * 
 * Non-ActiveRecord entity:   
 *     SqlBoxContext ctx=new SqlBoxContext(dataSource);
 *     ctx.insert(entity);
 * </pre>
 * 
 * ActiveRecord only works when a global defaultContext be set.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class ActiveRecord implements IActiveRecord {

	@Override
	public SqlBox box() {
		return SqlBoxFindUtils.findBox(this);
	}

	public TableModel tableModel() {
		return box().getTableModel();
	};

	@Override
	public String table() {
		return box().getTableModel().getTableName();
	}

	@Override
	public SqlBoxContext context() {
		return box().getContext();
	}

	@Override
	public void insert() {
		context().insert(this);
	}

	@Override
	public void update() {
	}

	@Override
	public void delete() {
	}

	@Override
	public <T> T load(Object pkey) {
		return null;
	}

}