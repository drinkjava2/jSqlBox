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

import com.github.drinkjava2.jdbpro.DbPro;

/**
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public interface ActiveRecordType {

	/** @return SqlBox instance */
	public SqlBox box();

	/** @return SqlBoxContext instance */
	public SqlBoxContext context();

	/** @return DbPro instance */
	public DbPro dbPro();

	/** Insert entity to database */
	public <T> T insert();

	/** Update entity in database */
	public <T> T update();

	/** Delete entity in database */
	public <T> T delete();

	/** Load entity from database by primary key, key can be single value or Map */
	public <T> T load(Object pkey);

}