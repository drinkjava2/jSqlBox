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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.drinkjava2.jbeanbox.ClassScanner;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * The tool to convert entity classes to TableModels
 * 
 * @author Yong Zhu
 * @since 1.0.6
 */
@SuppressWarnings("all")
public abstract class TableModelUtilsOfExcel {// NOSONAR

    /**  Export entity to excel csv format file   */
    public static void entityPackage2Excel(String pkgName, String fileName) {
        List<Class> classes = ClassScanner.scanPackages(pkgName);
        Collections.sort(classes, new Comparator<Class>() {
            public int compare(Class a, Class b) {
                if (a.getName().compareToIgnoreCase(b.getName()) > 0)
                    return 1;
                else
                    return -1;
            }
        });
        StringBuilder sb = new StringBuilder();
        for (Class claz : classes) {
            sb.append(model2CSVString(TableModelUtils.entity2Model(claz)));
            sb.append("\r\n");
        }
        writeToFile(fileName, sb);
    }

    /**  Export TableModel array  to excel csv format file   */
    public static void model2Excel(String fileName, TableModel... models) {
        List<TableModel> tbList = new ArrayList<TableModel>();
        for (TableModel tb : models)
            tbList.add(tb);
        Collections.sort(tbList, new Comparator<TableModel>() {
            public int compare(TableModel a, TableModel b) {
                if (a.getTableName().compareToIgnoreCase(b.getTableName()) > 0)
                    return 1;
                else
                    return -1;
            }
        });
        StringBuilder sb = new StringBuilder();
        for (TableModel tb : tbList) {
            sb.append(model2CSVString(tb));
            sb.append("\r\n");
        }
        writeToFile(fileName, sb);
    }

    private static void writeToFile(String fileName, StringBuilder sb) {
        BufferedWriter out = null;
        try {
            File writename = new File(fileName);
            writename.createNewFile();// NOSONAR
            out = new BufferedWriter(new FileWriter(writename));
            out.write(sb.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Translate tableModel to CSV String*/
    public static String model2CSVString(TableModel t) {
        StringBuilder sb = new StringBuilder();
        Class<?> claz = t.getEntityClass();
        sb.append(claz == null ? "," : claz.getName() + ",");
        sb.append("Table" + ",");
        sb.append("EntityField" + ",");
        sb.append("EntityType" + ",");
        sb.append("Pkey" + ",");
        sb.append("Column" + ",");
        sb.append("ColumnType" + ",");
        sb.append("Definition" + ",");

        sb.append("Length" + ",");
        sb.append("Precision" + ",");
        sb.append("Scale" + ",");
        sb.append("Nullable" + ",");
        sb.append("Check" + ",");
        sb.append("Transient" + ",");
        sb.append("Comment");

        sb.append("\r\n");
        for (ColumnModel c : t.getColumns())
            exportColInfo(sb, t, c, true);
        for (ColumnModel c : t.getColumns())
            exportColInfo(sb, t, c, false);
        sb.append("\r\n");
        return sb.toString();
    }

    private static void exportColInfo(StringBuilder sb, TableModel t, ColumnModel c, boolean isId) {
        if (isId && !c.getPkey())
            return;
        if (!isId && c.getPkey())
            return;
        sb.append(" " + ",");
        sb.append(t.getTableName() + ",");
        sb.append(c.getEntityField() == null ? "," : c.getEntityField() + ",");
        if (StrUtils.isEmpty(c.getEntityField()))
            sb.append(",");
        else {
            try {
                Method m = ClassCacheUtils.getClassFieldReadMethod(t.getEntityClass(), c.getEntityField());
                sb.append(m.getReturnType().getSimpleName() + ",");
            } catch (Exception e) {
                sb.append(",");
            }
        }
        sb.append(Boolean.TRUE.equals(c.getPkey()) ? "* ," : " " + ",");
        sb.append(c.getColumnName() + ",");
        sb.append(c.getColumnType() == null ? "," : c.getColumnType() + ",");
        sb.append(transExcelCellStr(c.getColumnDefinition()) + ",");
        sb.append(c.getLength() + ",");
        sb.append(c.getPrecision() + ",");
        sb.append(c.getScale() + ",");
        sb.append(Boolean.TRUE.equals(c.getNullable()) ? "Y," : " ,");
        sb.append(c.getCheck() == null ? "," : c.getCheck() + " ,");
        sb.append(Boolean.TRUE.equals(c.getTransientable()) ? "T," : " " + ",");
        sb.append(transExcelCellStr(c.getComment()));
        sb.append("\r\n");
    }

    private static String transExcelCellStr(String colDef) {
        if (colDef == null)
            colDef = "";
        if (colDef.indexOf("\"") != -1 || colDef.indexOf(",") != -1) {
            colDef = colDef.replaceAll("\"", "\"\"");
            colDef = "\"" + colDef;
            colDef += "\"";
        }
        return colDef;
    }
}
