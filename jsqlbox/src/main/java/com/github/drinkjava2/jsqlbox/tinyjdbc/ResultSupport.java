/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.drinkjava2.jsqlbox.tinyjdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * 
 * This class is copied from javax.servlet.jsp.jstl.sql package (Yong said)
 * 
 * <p>
 * Supports the creation of a javax.servlet.jsp.jstl.sql.Result object from a source java.sql.ResultSet object. A Result
 * object makes it much easier for page authors to access and manipulate the data resulting from a SQL query.
 * </p>
 * <p>
 * Note that these methods hide any SQLExceptions that are caused and return null - to work around that use
 * org.apache.taglibs.standard.tag.common.sql.ResultImpl directly.
 *
 * @author Justyna Horwat
 * @author Yong Zhu (in jSQLBox project)
 */
public class ResultSupport {

	private ResultSupport() {
		// private
	}

	/**
	 * Converts a <code>ResultSet</code> object to a <code>Result</code> object.
	 *
	 * @param rs
	 *            the <code>ResultSet</code> object
	 * @return The <code>Result</code> object created from the <code>ResultSet</code>
	 */
	public static TinyResult toResult(ResultSet rs) {
		try {
			return new TinyResult(rs, -1, -1);
		} catch (SQLException ex) {
			SqlBoxException.eatException(ex);
			return null;
		}
	}

	/**
	 * Converts <code>maxRows</code> of a <code>ResultSet</code> object to a <code>Result</code> object.
	 *
	 * @param rs
	 *            the <code>ResultSet</code> object
	 * @param maxRows
	 *            the maximum number of rows to be cached into the <code>Result</code> object.
	 * @return The <code>Result</code> object created from the <code>ResultSet</code>, limited by <code>maxRows</code>
	 */
	public static TinyResult toResult(ResultSet rs, int maxRows) {
		try {
			return new TinyResult(rs, -1, maxRows);
		} catch (SQLException ex) {
			SqlBoxException.eatException(ex);
			return null;
		}
	}

}
