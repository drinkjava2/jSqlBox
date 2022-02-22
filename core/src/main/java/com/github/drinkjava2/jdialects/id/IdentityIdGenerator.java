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
package com.github.drinkjava2.jdialects.id;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.DDLFeatures;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;

/**
 * Define an Identity type generator, supported by MySQL, SQL Server, DB2,
 * Derby, Sybase, PostgreSQL
 * 
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class IdentityIdGenerator implements IdGenerator {
    private String table;

    private String column;

    public IdentityIdGenerator(String table, String column) {
        this.table = table;
        this.column = column;
    }

    @Override
    public GenerationType getGenerationType() {
        return GenerationType.IDENTITY;
    }

    @Override
    public String getIdGenName() {
        return "IDENTITY";
    }

    @Override
    public Boolean dependOnAutoIdGenerator() {
        return false;
    }

    @Override
    public Object getNextID(NormalJdbcTool jdbc, Dialect dialect, Type dataType) {
        if (!dialect.ddlFeatures.getSupportsIdentityColumns())
            throw new DialectException("Dialect '" + dialect + "' does not support identity type");
        String sql = null;
        if (Type.BIGINT.equals(dataType))
            sql = dialect.ddlFeatures.getIdentitySelectStringBigINT();
        else
            sql = dialect.ddlFeatures.getIdentitySelectString();
        if (StrUtils.isEmpty(sql) || DDLFeatures.NOT_SUPPORT.equals(sql))
            throw new DialectException("Dialect '" + dialect + "' does not support identity type");
        sql=StrUtils.replaceFirst(sql, "_table__col", new StringBuffer(table).append("_").append(column).toString());
        return jdbc.jdbcQueryForObject(sql);
    }

    @Override
    public IdGenerator newCopy() {
        return new IdentityIdGenerator(table, column);
    }

    // getter & setter==============
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
