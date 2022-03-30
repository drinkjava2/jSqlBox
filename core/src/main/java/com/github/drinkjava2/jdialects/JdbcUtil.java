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
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * A tiny pure JDBC tool to access database, only used for unit test
 * 
 *
 * @author Yong Zhu
 * @version 1.0.0
 */
@SuppressWarnings("unchecked")
public abstract class JdbcUtil {//NOSONAR 

    /**
     * Return sql query result object in first row first column
     * @param con Connection
     * @param sql sql
     * @param params parameters
     * @return object in first row first column
     */
    public static <T> T qryOneObject(Connection con, String sql, Object... params) {
        ResultSet rs = null;
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            int i = 1;
            for (Object obj : params)
                pst.setObject(i++, obj);
            rs = pst.executeQuery();
            if (rs.next())
                return (T) rs.getObject(1);
            else
                return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //eat exception
                }
        }
    }

    /**
     * Execute a sql, return rows quantity be affected
     * @param ds
     * @param sql
     * @param params
     * @return rows quantity be affected
     */
    public static int execute(Connection con, String sql, Object... params) {
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            int i = 1;
            for (Object obj : params)
                pst.setObject(i++, obj);
            pst.execute();
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return sql query result object in first row first column
     * @param ds Datasource
     * @param sql sql
     * @param params parameters
     * @return object in first row first column
     */
    public static <T> T qryOneObject(DataSource ds, String sql, Object... params) {
        ResultSet rs = null;
        try (Connection con = ds.getConnection(); //
                PreparedStatement pst = con.prepareStatement(sql);) {
            int i = 1;
            for (Object obj : params)
                pst.setObject(i++, obj);
            rs = pst.executeQuery();// NOSONAR
            if (rs.next())
                return (T) rs.getObject(1);
            else
                return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //eat exception
                }
        }
    }

    /**
     * Execute a sql, return rows quantity be affected
     * @param ds
     * @param sql
     * @param params
     * @return rows quantity be affected
     */
    public static int execute(DataSource ds, String sql, Object... params) {
        try (Connection con = ds.getConnection(); //
                PreparedStatement pst = con.prepareStatement(sql);) {
            int i = 1;
            for (Object obj : params)
                pst.setObject(i++, obj);
            pst.execute();
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
