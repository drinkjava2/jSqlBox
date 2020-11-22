/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * The tool to convert database structure(meta data) to TableModels
 * 
 * @author Yong Zhu
 * @since 1.0.6
 */
public abstract class TableModelUtilsOfDb {// NOSONAR
    private static final String TABLE_NAME = "TABLE_NAME";

    /**
     * Convert JDBC connected database structure to TableModels, note: <br/>
     * 1)This method does not close connection <br/>
     * 2)This method does not support sequence, foreign keys, primary keys..., but
     * will improve later.
     */
    public static TableModel[] db2Models(Connection con, Dialect dialect) {// NOSONAR
        List<TableModel> tableModels = new ArrayList<TableModel>();
        SQLException sqlException = null;
        ResultSet rs = null;
        PreparedStatement pst = null;

        try{
            DatabaseMetaData meta = con.getMetaData();
            String catalog = con.getCatalog();
            // get Tables
            rs = meta.getTables(catalog, dialect.isOracleFamily() ? meta.getUserName() : null, null, new String[]{"TABLE"});
            while (rs.next()){
                String tableName = rs.getString(TABLE_NAME);
                if(!StrUtils.isEmpty(tableName)){
                    if(ReservedDBWords.isReservedWord(dialect, tableName))
                        tableName = dialect.ddlFeatures.openQuote + tableName + dialect.ddlFeatures.openQuote;
                    TableModel model = new TableModel(tableName);
                    tableModels.add(model);
                    String comment = rs.getString("REMARKS");
                    if(!StrUtils.isEmpty(comment))
                        model.setComment(comment);
                }
            }
            rs.close();

            // Build Columns
            for (TableModel model : tableModels){
                String tableName = model.getTableName();
                rs = meta.getColumns(catalog, null, tableName, null); // detail see meta.getC alt + /
                while (rs.next()){// NOSONAR
                    String colName = rs.getString("COLUMN_NAME");
                    if(ReservedDBWords.isReservedWord(dialect, colName))
                        colName = dialect.ddlFeatures.openQuote + colName + dialect.ddlFeatures.openQuote;
                    ColumnModel col = new ColumnModel(colName);
                    model.addColumn(col);

                    int javaSqlType = rs.getInt("DATA_TYPE");
                    try{
                        col.setColumnType(TypeUtils.javaSqlTypeToDialectType(javaSqlType));
                    }catch (Exception e1){
                        throw new DialectException("jDialect does not supported java.sql.types value " + javaSqlType, e1);
                    }

                    col.setLength(rs.getInt("CHAR_OCTET_LENGTH"));
                    col.setPrecision(rs.getInt("COLUMN_SIZE"));
                    col.setScale(rs.getInt("DECIMAL_DIGITS"));
                    col.setNullable(rs.getInt("NULLABLE") > 0);
                    col.setDefaultValue(rs.getString("COLUMN_DEF"));
                    col.setComment(rs.getString("REMARKS"));

                    try{
                        if(((Boolean) (true)).equals(rs.getBoolean("IS_AUTOINCREMENT")))
                            col.identityId();
                    }catch (Exception e){
                    }

                    try{
                        if("YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")))
                            col.identityId();
                    }catch (Exception e){
                    }
                }
                rs.close();
            }

            // Get Primary Keys for each model
            for (TableModel model : tableModels){
                rs = meta.getPrimaryKeys(catalog, null, model.getTableName());
                while (rs.next()){
                    model.getColumnByColName(rs.getString("COLUMN_NAME")).setPkey(true);
                }
                rs.close();
            }
            // Get Foreign Keys for each model
            for (TableModel model : tableModels){
                ResultSet foreignKeyResultSet = meta.getImportedKeys(catalog, null, model.getTableName());
                while (foreignKeyResultSet.next()){
                    String fkname = foreignKeyResultSet.getString("FK_NAME");
                    int keyseq = foreignKeyResultSet.getInt("KEY_SEQ");
                    String fkColumnName = foreignKeyResultSet.getString("FKCOLUMN_NAME");
                    String pkTablenName = foreignKeyResultSet.getString("PKTABLE_NAME");
                    String pkColumnName = foreignKeyResultSet.getString("PKCOLUMN_NAME");
                    FKeyModel fkeyModel = model.getFkey(fkname);

                    if(keyseq == 1){
                        model.fkey(fkname).columns(fkColumnName).refs(pkTablenName, pkColumnName);
                    }else{
                        fkeyModel.getColumnNames().add(fkColumnName);
                        String[] newRefs = ArrayUtils.appendStrArray(fkeyModel.getRefTableAndColumns(), pkColumnName);
                        fkeyModel.setRefTableAndColumns(newRefs);
                    }
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
            sqlException = e;
        }finally{
            if(pst != null)
                try{
                    pst.close();
                }catch (SQLException e1){
                    if(sqlException != null)
                        sqlException.setNextException(e1);
                    else
                        sqlException = e1;
                }
            try{
                if(rs != null)
                    rs.close();
            }catch (SQLException e2){
                if(sqlException != null)
                    sqlException.setNextException(e2);
                else
                    sqlException = e2;
            }
        }
        if(sqlException != null)
            throw new DialectException(sqlException);
        return tableModels.toArray(new TableModel[tableModels.size()]);
    }

    public static void compareDB(Connection con1, Connection con2) {//compare 2 database
        doCompareDB(con1, con2, false);
    }

    public static void compareDbIgnoreLength(Connection con1, Connection con2) {//compare 2 database, ignore legnth, precision and scale
        doCompareDB(con1, con2, true);
    }

    private static void doCompareDB(Connection con1, Connection con2, boolean ignoreLength) {
        TableModel[] models1 = TableModelUtils.db2Models(con1, Dialect.guessDialect(con1));
        TableModel[] models2 = TableModelUtils.db2Models(con2, Dialect.guessDialect(con2));

        for (TableModel model1 : models1){
            String tableName1 = model1.getTableName();
            boolean foundTable = false;
            List<ColumnModel> cols1 = model1.getColumns();
            for (TableModel model2 : models2){
                if(tableName1.equalsIgnoreCase(model2.getTableName())){
                    String tableName2 = model2.getTableName();
                    foundTable = true;
                    List<ColumnModel> cols2 = model2.getColumns();
                    for (ColumnModel col1 : cols1){
                        for (ColumnModel col2 : cols2){
                            if(col1.getColumnName().equalsIgnoreCase(col2.getColumnName())){
                                if(!("" + col1.getColumnType()).equalsIgnoreCase("" + col2.getColumnType())){
                                    print("DB1 column " + tableName1 + "." + col1.getColumnName() + " type is " + col1.getColumnType() + ", but " + "DB2 column " + tableName2 + "."
                                            + col2.getColumnName() + " type is " + col2.getColumnType());
                                }
                                if(!ignoreLength){ //if ignore length check?
                                    if(!("" + col1.getLength()).equalsIgnoreCase("" + col2.getLength())){
                                        print("DB1 column " + tableName1 + "." + col1.getColumnName() + " length is " + col1.getLength() + ", but " + "DB2 column " + tableName2 + "."
                                                + col2.getColumnName() + " length is " + col2.getLength());
                                    }
                                    if(!("" + col1.getPrecision()).equalsIgnoreCase("" + col2.getPrecision())){
                                        print("DB1 column " + tableName1 + "." + col1.getColumnName() + " precision is " + col1.getPrecision() + ", but " + "DB2 column " + tableName2 + "."
                                                + col2.getColumnName() + " precision is " + col2.getPrecision());
                                    }
                                    if(!("" + col1.getScale()).equalsIgnoreCase("" + col2.getScale())){
                                        print("DB1 column " + tableName1 + "." + col1.getColumnName() + " scale is " + col1.getScale() + ", but " + "DB2 column " + tableName2 + "."
                                                + col2.getColumnName() + " scale is " + col2.getScale());
                                    }
                                }
                                break;
                            }
                        }
                    }

                    for (ColumnModel col1 : cols1){
                        boolean found = false;
                        for (ColumnModel col2 : cols2)
                            if(col1.getColumnName().equalsIgnoreCase(col2.getColumnName())){
                                found = true;
                                break;
                            }
                        if(!found)
                            print("DB1 column " + tableName1 + "." + col1.getColumnName() + " not found in " + "DB2 table " + tableName2);
                    }

                    for (ColumnModel col2 : cols2){
                        boolean found = false;
                        for (ColumnModel col1 : cols1)
                            if(col2.getColumnName().equalsIgnoreCase(col1.getColumnName())){
                                found = true;
                                break;
                            }
                        if(!found)
                            print("DB2 column " + tableName1 + "." + col2.getColumnName() + " not found in " + "DB1 table " + tableName1);
                    }

                    break;
                }
            }
            if(!foundTable)
                print("DB1 table" + tableName1 + " not found in " + "DB2 ");
        }

        for (TableModel model2 : models1){
            String tableName2 = model2.getTableName();
            boolean foundTable = false;
            for (TableModel model1 : models1){
                if(tableName2.equalsIgnoreCase(model1.getTableName())){
                    foundTable = true;
                    break;
                }
            }
            if(!foundTable)
                print("DB2 table " + tableName2 + " not found in " + "DB1 ");
        }
    }

    private static void print(String s) {
        System.out.println(s);
    }
}
