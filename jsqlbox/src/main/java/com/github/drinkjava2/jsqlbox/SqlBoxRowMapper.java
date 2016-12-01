/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.drinkjava2.jsqlbox;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * SqlBoxRowMapper implements RowMapper interface
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0
 */
public class SqlBoxRowMapper implements RowMapper<Object> {
	SqlBoxRowMapperData data = new SqlBoxRowMapperData();

	public SqlBoxRowMapper(SqlBox box) {
		this.data.box = box;
	}

	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Object bean = null;
		try {
			bean = data.box.getBeanClass().newInstance();
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBox getRowMapper error, beanClass=" + data.box.getBeanClass());
		}
		return bean;
	}
}
