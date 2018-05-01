/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.hibernatesrc.pagination;

/**
 * Represents a selection criteria for rows in a JDBC {@link java.sql.ResultSet}
 *
 * @author Gavin King
 * @author Yong Zhu(modify)
 */
public final class RowSelection {
	private Integer firstRow;
	private Integer maxRows;

	public RowSelection(int firstRow, int maxRows) {
		this.firstRow = firstRow;
		this.maxRows = maxRows;
	}

	public void setFirstRow(Integer firstRow) {
		this.firstRow = firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public Integer getFirstRow() {
		return firstRow;
	}

	public void setMaxRows(Integer maxRows) {
		this.maxRows = maxRows;
	}

	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}

	public Integer getMaxRows() {
		return maxRows;
	}

	public boolean definesLimits() {
		return maxRows != null || (firstRow != null && firstRow <= 0);
	}

}
