/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdialects.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdbpro.NormalJdbcTool;

/**
 * A tiny pure JDBC tool to access database, only used for unit test
 * 
 *
 * @author Yong Zhu
 * @version 1.0.0
 */
public class TinyJdbc implements NormalJdbcTool {
	private DataSource ds;

	public TinyJdbc(DataSource ds) {
		this.ds = ds;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T nQueryForObject(String sql, Object... params) {
		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = ds.getConnection();
			int i = 1;
			pst = con.prepareStatement(sql);// NOSONAR
			for (Object obj : params)
				pst.setObject(i++, obj);
			rs = pst.executeQuery();
			if (rs.next())
				return (T) rs.getObject(1);
			else
				return null;
		} catch (SQLException e) {
			Systemout.println("Exception found: " + e.getMessage());
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				Systemout.println("Exception found: " + e.getMessage());
			}
		}
		return null;
	}

	@Override
	public int nUpdate(String sql, Object... params) {
		return nExecute(sql, params);
	}

	@Override
	public int nExecute(String sql, Object... params) {
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = ds.getConnection();
			int i = 1;
			pst = con.prepareStatement(sql);// NOSONAR
			for (Object obj : params)
				pst.setObject(i++, obj);
			pst.execute();
			return 1;
		} catch (SQLException e) {
			Systemout.println("Exception found: " + e.getMessage());
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				Systemout.println("Exception found: " + e.getMessage());
			}
		}
		return 0;
	}

}
