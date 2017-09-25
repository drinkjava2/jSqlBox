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

/**
 * Entity class extended from ActiveRecord will get CRUD methods, see below
 * difference in jSqlBox to save ActiveRecord entity and POJO entity into
 * database:
 * 
 * <pre>
 * ActiveRecord Entity:               
 *     entity.insert(); 
 * 
 * POJO entity:   
 *     context.insert(entity);
 * </pre>
 * 
 * To make ActiveRecord methods like entity.insert() work, need configure
 * entity's SqlBoxContext property or set a global Default SqlBoxContext
 * instance in advance, detail see README.MD
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class ActiveRecord implements ActiveRecordSupport {

	@Override
	public SqlBox box() {
		return SqlBoxUtils.findBox(this);
	}

	@Override
	public SqlBoxContext context() {
		return box().getContext();
	}

	@Override
	public <T> T insert() {
		return context().insert(this);
	}

	@Override
	public <T> T update() {
		return null;
	}

	@Override
	public <T> T delete() {
		return null;
	}

	@Override
	public <T> T load(Object pkey) {
		return null;
	}

}