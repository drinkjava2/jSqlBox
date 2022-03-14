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

/**
 * DDL features template
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLFeatures {

    public static final String NOT_SUPPORT = "NOT_SUPPORT";
    protected String addColumnString;
    protected String addColumnSuffixString;
    protected String addForeignKeyConstraintString;
    protected String addFKeyRefPkeyString;// If ref pkey, can ignore ref columns
    protected String addPrimaryKeyConstraintString;
    protected String columnComment;
    protected String createCatalogCommand;
    protected String createMultisetTableString;// for create table without PKEY
    protected String createPooledSequenceStrings;
    protected String createSchemaCommand;
    protected String createSequenceStrings;
    protected String createTableString; // for create table with PKEY
    protected String currentSchemaCommand;
    protected String dropCatalogCommand;
    protected String dropForeignKeyString;
    protected String dropSchemaCommand;
    protected String dropSequenceStrings;
    protected String dropTableString;
    protected Boolean hasAlterTable;
    protected Boolean hasDataTypeInIdentityColumn;
    protected String identityColumnString;
    protected String identityColumnStringBigINT;
    protected String identitySelectString;
    protected String identitySelectStringBigINT;
    protected Boolean needDropConstraintsBeforeDropTable;
    protected String nullColumnString;// for allowed null column definition
    protected Boolean requiresParensForTupleDistinctCounts;
    protected String selectSequenceNextValString;
    protected String sequenceNextValString;
    protected Boolean supportsColumnCheck;
    protected Boolean supportsCommentOn;
    protected Boolean supportsIdentityColumns;
    protected Boolean supportsIfExistsAfterConstraintName;
    protected String openQuote; // for quote column which used keywords
    protected String closeQuote; // for quote column which used keywords
    
    protected static void initDDLFeatures() {
        for (Dialect d : Dialect.dialects) 
            initDDLFeatures(d); 
    }
    
    protected static boolean isValidDDLTemplate(String featureValue) {
        return !(StrUtils.isEmpty(featureValue) || NOT_SUPPORT.equals(featureValue));
    }

    public boolean supportBasicOrPooledSequence() {
        return supportsSequences || supportsPooledSequences;
    }
     
    /** For dropping a table, can the phrase "if exists" be applied beforeQuery the table name */
    protected Boolean supportsIfExistsAfterTableName;
    protected Boolean supportsIfExistsBeforeConstraintName;
    /** For dropping a table, can the phrase "if exists" be applied afterQuery the table name   */
    protected Boolean supportsIfExistsBeforeTableName;
    protected Boolean supportsInsertSelectIdentity;
    protected Boolean supportsPooledSequences; // support initial & increment
    protected Boolean supportsSequences; // basic sequence
    protected Boolean supportsTableCheck;
    protected String tableTypeString;

    /**
     * For given dialect, set its DDLFeatures with default common DDL templates 
     */
    public static DDLFeatures createDefaultDDLFeatures() {
        DDLFeatures ddl=new DDLFeatures(); 
        ddl.addColumnString = "add";
        ddl.addColumnSuffixString = "";
        ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE";
        ddl.addForeignKeyConstraintString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
        ddl.addPrimaryKeyConstraintString = " add constraint _PKEYNAME primary key ";
        ddl.closeQuote = "\"";
        ddl.columnComment = "";
        ddl.createCatalogCommand = NOT_SUPPORT;
        ddl.createMultisetTableString = "create table";
        ddl.createPooledSequenceStrings = "create sequence _SEQ start with 11 increment by 33";
        ddl.createSchemaCommand = "create schema _SCHEMANAME";
        ddl.createSequenceStrings = "create sequence _SEQ";
        ddl.createTableString = "create table";
        ddl.currentSchemaCommand = NOT_SUPPORT;
        ddl.dropCatalogCommand = NOT_SUPPORT;
        ddl.dropForeignKeyString = " drop constraint ";
        ddl.dropSchemaCommand = "drop schema _SCHEMANAME";
        ddl.dropSequenceStrings = "drop sequence _SEQNAME";
        ddl.dropTableString = "drop table _TABLENAME cascade constraints";
        ddl.hasAlterTable = true;
        ddl.hasDataTypeInIdentityColumn = true;
        ddl.identityColumnString = NOT_SUPPORT;
        ddl.identityColumnStringBigINT = NOT_SUPPORT;
        ddl.identitySelectString = NOT_SUPPORT;
        ddl.identitySelectStringBigINT = NOT_SUPPORT;
        ddl.needDropConstraintsBeforeDropTable = false;
        ddl.nullColumnString = "";
        ddl.openQuote = "\"";
        ddl.requiresParensForTupleDistinctCounts = false;
        ddl.selectSequenceNextValString = "_SEQNAME.nextval";
        ddl.sequenceNextValString = "select _SEQNAME.nextval from dual";
        ddl.supportsColumnCheck = true;
        ddl.supportsCommentOn = true;
        ddl.supportsIdentityColumns = false;
        ddl.supportsIfExistsAfterConstraintName = false;
        ddl.supportsIfExistsBeforeConstraintName = false;
        ddl.supportsPooledSequences = true;
        ddl.supportsSequences = true;
        ddl.supportsTableCheck = true;
        ddl.tableTypeString = "";
        return ddl;
    }
    
    /** Initialize each dialect's DDL features    */
    protected static void initDDLFeatures(Dialect dia) { 
        DDLFeatures ddl=createDefaultDDLFeatures();
        dia.ddlFeatures=ddl;
        switch (DialectType.getDialectType(dia)){//then modify each dialect if features not same as default features
        case SQLiteDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = NOT_SUPPORT;
            ddl.addForeignKeyConstraintString = NOT_SUPPORT;
            ddl.addPrimaryKeyConstraintString = NOT_SUPPORT;
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropForeignKeyString = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.hasAlterTable = false;
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "integer";
            ddl.identityColumnStringBigINT = "integer";
            ddl.identitySelectString = "select last_insert_rowid()";
            ddl.identitySelectStringBigINT = "select last_insert_rowid()";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case AccessDialect: {
            ddl.addColumnString = "add column";
            ddl.addPrimaryKeyConstraintString = " primary key ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropForeignKeyString = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = "nextval('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval('_SEQNAME')";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        case ExcelDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = NOT_SUPPORT;
            ddl.addForeignKeyConstraintString = NOT_SUPPORT;
            ddl.addPrimaryKeyConstraintString = " primary key ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropForeignKeyString = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = "nextval('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval('_SEQNAME')";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        case TextDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = NOT_SUPPORT;
            ddl.addForeignKeyConstraintString = NOT_SUPPORT;
            ddl.addPrimaryKeyConstraintString = " primary key ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropForeignKeyString = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.hasAlterTable = false;
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = "nextval('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval('_SEQNAME')";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        case ParadoxDialect: {
            ddl.addColumnString = "add column";
            ddl.addPrimaryKeyConstraintString = " primary key ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropForeignKeyString = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = "nextval('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval('_SEQNAME')";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        case CobolDialect: {
            ddl.addColumnString = "add column";
            ddl.addPrimaryKeyConstraintString = " primary key ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropForeignKeyString = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.hasAlterTable = false;
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = "nextval('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval('_SEQNAME')";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        case XMLDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = NOT_SUPPORT;
            ddl.addForeignKeyConstraintString = NOT_SUPPORT;
            ddl.addPrimaryKeyConstraintString = " primary key ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropForeignKeyString = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = "nextval('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval('_SEQNAME')";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        case DbfDialect: {
            ddl.addColumnString = "add column";
            ddl.addPrimaryKeyConstraintString = " primary key ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropForeignKeyString = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = "nextval('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval('_SEQNAME')";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        case DamengDialect: {
            ddl.addColumnString = " add column ";
            ddl.createPooledSequenceStrings = "create sequence _SEQ increment by 33 start with 11";
            ddl.dropTableString = "drop table _TABLENAME cascade ";
            ddl.identityColumnString = "identity";
            ddl.identityColumnStringBigINT = "identity";
            ddl.identitySelectString = "select SCOPE_IDENTITY()";
            ddl.identitySelectStringBigINT = "select SCOPE_IDENTITY()";
            ddl.sequenceNextValString = "select _SEQNAME.nextval";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case GBaseDialect: {
            ddl.createPooledSequenceStrings = "create sequence _SEQ start with 11 increment by  33";
            ddl.currentSchemaCommand = "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL";
        }
            break;
        case Cache71Dialect: {
            ddl.addColumnString = " add column";
            ddl.addFKeyRefPkeyString = " ADD CONSTRAINT _FKEYNAME FOREIGN KEY _FKEYNAME (_FK1, _FK2) REFERENCES _REFTABLE (_REF1, _REF2) ";
            ddl.addForeignKeyConstraintString = " ADD CONSTRAINT _FKEYNAME FOREIGN KEY _FKEYNAME (_FK1, _FK2) REFERENCES _REFTABLE (_REF1, _REF2) ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity";
            ddl.identityColumnStringBigINT = "identity";
            ddl.identitySelectString = "SELECT LAST_IDENTITY() FROM %TSQL_sys.snf";
            ddl.identitySelectStringBigINT = "SELECT LAST_IDENTITY() FROM %TSQL_sys.snf";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case CUBRIDDialect: {
            ddl.closeQuote = "]";
            ddl.createPooledSequenceStrings = "create serial _SEQ start with 11 increment by 33";
            ddl.createSequenceStrings = "create serial _SEQ";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSequenceStrings = "drop serial _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "[";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = "select _SEQNAME.next_value from table({1}) as T(X)";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case DataDirectOracle9Dialect: {
        }
            break;
        case DB2Dialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "values identity_val_local()";
            ddl.identitySelectStringBigINT = "values identity_val_local()";
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "values nextval for _SEQNAME";
            ddl.supportsIdentityColumns = true;
        }
            break;
        case DB2390Dialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "select identity_val_local() from sysibm.sysdummy1";
            ddl.identitySelectStringBigINT = "select identity_val_local() from sysibm.sysdummy1";
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "values nextval for _SEQNAME";
            ddl.supportsIdentityColumns = true;
            ddl.supportsSequences = false;
        }
            break;
        case DB2390V8Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ as integer start with 1 increment by 1 minvalue 1 nomaxvalue nocycle nocache start with 11 increment by 33";
            ddl.createSequenceStrings = "create sequence _SEQ as integer start with 1 increment by 1 minvalue 1 nomaxvalue nocycle nocache";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "select identity_val_local() from sysibm.sysdummy1";
            ddl.identitySelectStringBigINT = "select identity_val_local() from sysibm.sysdummy1";
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "select nextval for _SEQNAME from sysibm.sysdummy1";
            ddl.supportsIdentityColumns = true;
        }
            break;
        case DB2400Dialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "select identity_val_local() from sysibm.sysdummy1";
            ddl.identitySelectStringBigINT = "select identity_val_local() from sysibm.sysdummy1";
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "values nextval for _SEQNAME";
            ddl.supportsIdentityColumns = true;
            ddl.supportsSequences = false;
        }
            break;
        case DB297Dialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "values identity_val_local()";
            ddl.identitySelectStringBigINT = "values identity_val_local()";
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "values nextval for _SEQNAME";
            ddl.supportsIdentityColumns = true;
        }
            break;
        case DerbyDialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "values identity_val_local()";
            ddl.identitySelectStringBigINT = "values identity_val_local()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsSequences = false;
        }
            break;
        case DerbyTenFiveDialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "values identity_val_local()";
            ddl.identitySelectStringBigINT = "values identity_val_local()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsSequences = false;
        }
            break;
        case DerbyTenSevenDialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "values identity_val_local()";
            ddl.identitySelectStringBigINT = "values identity_val_local()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "values next value for _SEQNAME";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case DerbyTenSixDialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "values identity_val_local()";
            ddl.identitySelectStringBigINT = "values identity_val_local()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "values next value for _SEQNAME";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case FirebirdDialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = "create generator _SEQ";
            ddl.dropSequenceStrings = "drop generator _SEQNAME";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = "gen_id( _SEQNAME, 1 )";
            ddl.sequenceNextValString = "select gen_id( _SEQNAME, 1 ) from RDB$DATABASE";
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
        }
            break;
        case FrontBaseDialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME cascade";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case H2Dialect: {
            ddl.addColumnString = "add column";
            ddl.closeQuote = "`";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table _TABLENAME if exists";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "call identity()";
            ddl.identitySelectStringBigINT = "call identity()";
            ddl.openQuote = "`";
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "call next value for _SEQNAME";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
        }
            break;
        case HANAColumnStoreDialect: {
            ddl.addColumnString = "add (";
            ddl.addColumnSuffixString = ")";
            ddl.columnComment = "comment '_COMMENT'";
            ddl.createMultisetTableString = "create column table";
            ddl.createTableString = "create column table";
            ddl.currentSchemaCommand = "select current_schema from sys.dummy";
            ddl.dropTableString = "drop table _TABLENAME cascade";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "select current_identity_value() from _table";
            ddl.identitySelectStringBigINT = "select current_identity_value() from _table";
            ddl.sequenceNextValString = "select _SEQNAME.nextval from sys.dummy";
            ddl.supportsColumnCheck = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case HANARowStoreDialect: {
            ddl.addColumnString = "add (";
            ddl.addColumnSuffixString = ")";
            ddl.columnComment = "comment '_COMMENT'";
            ddl.createMultisetTableString = "create row table";
            ddl.createTableString = "create row table";
            ddl.currentSchemaCommand = "select current_schema from sys.dummy";
            ddl.dropTableString = "drop table _TABLENAME cascade";
            ddl.identityColumnString = "generated by default as identity";
            ddl.identityColumnStringBigINT = "generated by default as identity";
            ddl.identitySelectString = "select current_identity_value() from _table";
            ddl.identitySelectStringBigINT = "select current_identity_value() from _table";
            ddl.sequenceNextValString = "select _SEQNAME.nextval from sys.dummy";
            ddl.supportsColumnCheck = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case HSQLDialect: {
            ddl.addColumnString = "add column";
            ddl.createSequenceStrings = "create sequence _SEQ start with 1";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME if exists";
            ddl.dropTableString = "drop table _TABLENAME if exists";
            ddl.identityColumnString = "generated by default as identity (start with 1)";
            ddl.identityColumnStringBigINT = "generated by default as identity (start with 1)";
            ddl.identitySelectString = "call identity()";
            ddl.identitySelectStringBigINT = "call identity()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "call next value for _SEQNAME";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case InformixDialect: {
            ddl.addFKeyRefPkeyString = " add constraint  foreign key (_FK1, _FK2) references _REFTABLE constraint _FKEYNAME";
            ddl.addForeignKeyConstraintString = " add constraint  foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2) constraint _FKEYNAME";
            ddl.addPrimaryKeyConstraintString = " add constraint primary key constraint _PKEYNAME ";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "serial8 not null";
            ddl.identitySelectString = "select dbinfo('sqlca.sqlerrd1') from informix.systables where tabid=1";
            ddl.identitySelectStringBigINT = "select dbinfo('serial8') from informix.systables where tabid=1";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.sequenceNextValString = "select _SEQNAME.nextval from informix.systables where tabid=1";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case Informix10Dialect: {
            ddl.addFKeyRefPkeyString = " add constraint  foreign key (_FK1, _FK2) references _REFTABLE constraint _FKEYNAME";
            ddl.addForeignKeyConstraintString = " add constraint  foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2) constraint _FKEYNAME";
            ddl.addPrimaryKeyConstraintString = " add constraint primary key constraint _PKEYNAME ";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "serial8 not null";
            ddl.identitySelectString = "select dbinfo('sqlca.sqlerrd1') from informix.systables where tabid=1";
            ddl.identitySelectStringBigINT = "select dbinfo('serial8') from informix.systables where tabid=1";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.sequenceNextValString = "select _SEQNAME.nextval from informix.systables where tabid=1";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case IngresDialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.nullColumnString = " with null";
            ddl.sequenceNextValString = "select nextval for _SEQNAME";
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
        }
            break;
        case Ingres10Dialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "not null generated by default as identity";
            ddl.identityColumnStringBigINT = "not null generated by default as identity";
            ddl.identitySelectString = "select last_identity()";
            ddl.identitySelectStringBigINT = "select last_identity()";
            ddl.nullColumnString = " with null";
            ddl.sequenceNextValString = "select nextval for _SEQNAME";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case Ingres9Dialect: {
            ddl.addColumnString = "add column";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identitySelectString = "select last_identity()";
            ddl.identitySelectStringBigINT = "select last_identity()";
            ddl.nullColumnString = " with null";
            ddl.sequenceNextValString = "select nextval for _SEQNAME";
            ddl.supportsCommentOn = false;
        }
            break;
        case InterbaseDialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = "create generator _SEQ";
            ddl.dropSequenceStrings = "delete from RDB$GENERATORS where RDB$GENERATOR_NAME = '_SEQNAME'";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = "gen_id( _SEQNAME, 1 )";
            ddl.sequenceNextValString = "select gen_id( _SEQNAME, 1 ) from RDB$DATABASE";
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
        }
            break;
        case JDataStoreDialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME cascade";
            ddl.identityColumnString = "autoincrement";
            ddl.identityColumnStringBigINT = "autoincrement";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        case MariaDBDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MariaDB53Dialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MariaDB102Dialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MariaDB103Dialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = "nextval(_SEQNAME)";
            ddl.sequenceNextValString = "select nextval(_SEQNAME)";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MariaDB10Dialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MckoiDialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = "nextval('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval('_SEQNAME')";
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
        }
            break;
        case MimerSQLDialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = "create unique sequence _SEQ";
            ddl.dropSequenceStrings = "drop sequence _SEQNAME restrict";
            ddl.dropTableString = "drop table _TABLENAME cascade";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = "select next_value of _SEQNAME from system.onerow";
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
        }
            break;
        case MySQLDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " type=MyISAM";
        }
            break;
        case MySQL5Dialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=MyISAM";
        }
            break;
        case MySQL55Dialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MySQL57Dialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MySQL57InnoDBDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MySQL5InnoDBDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case MySQLInnoDBDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " type=InnoDB";
        }
            break;
        case MySQLMyISAMDialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " type=MyISAM";
        }
            break;
        case MySQL8Dialect: {
            ddl.addColumnString = "add column";
            ddl.addFKeyRefPkeyString = " add constraint _FKEYNAME foreign key (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.closeQuote = "`";
            ddl.columnComment = " comment '_COMMENT'";
            ddl.createCatalogCommand = "create database _CATALOGNAME";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSchemaCommand = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropCatalogCommand = "drop database _CATALOGNAME";
            ddl.dropForeignKeyString = " drop foreign key ";
            ddl.dropSchemaCommand = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table if exists _TABLENAME";
            ddl.identityColumnString = "not null auto_increment";
            ddl.identityColumnStringBigINT = "not null auto_increment";
            ddl.identitySelectString = "select last_insert_id()";
            ddl.identitySelectStringBigINT = "select last_insert_id()";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "`";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " engine=InnoDB";
        }
            break;
        case OracleDialect: {
        }
            break;
        case Oracle10gDialect: {
            ddl.createPooledSequenceStrings = "create sequence _SEQ start with 11 increment by  33";
            ddl.currentSchemaCommand = "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL";
        }
            break;
        case Oracle12cDialect: {
            ddl.createPooledSequenceStrings = "create sequence _SEQ start with 11 increment by  33";
            ddl.currentSchemaCommand = "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL";
            ddl.identityColumnString = "generated as identity";
            ddl.identityColumnStringBigINT = "generated as identity";
            ddl.supportsIdentityColumns = true;
        }
            break;
        case Oracle8iDialect: {
            ddl.createPooledSequenceStrings = "create sequence _SEQ start with 11 increment by  33";
            ddl.currentSchemaCommand = "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL";
        }
            break;
        case Oracle9Dialect: {
        }
            break;
        case Oracle9iDialect: {
            ddl.createPooledSequenceStrings = "create sequence _SEQ start with 11 increment by  33";
            ddl.currentSchemaCommand = "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL";
        }
            break;
        case PointbaseDialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME cascade";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case PostgresPlusDialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
        }
            break;
        case PostgreSQLDialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
        }
            break;
        case PostgreSQL81Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropTableString = "drop table _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
        }
            break;
        case PostgreSQL82Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
        }
            break;
        case PostgreSQL9Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
        }
            break;
        case PostgreSQL91Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
        }
            break;
        case PostgreSQL92Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
        }
            break;
        case PostgreSQL93Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
        }
            break;
        case PostgreSQL94Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
        }
            break;
        case PostgreSQL95Dialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = "create sequence _SEQ start 11 increment 33";
            ddl.dropSequenceStrings = "drop sequence if exists _SEQNAME";
            ddl.dropTableString = "drop table if exists _TABLENAME cascade";
            ddl.hasDataTypeInIdentityColumn = false;
            ddl.identityColumnString = "serial not null";
            ddl.identityColumnStringBigINT = "bigserial not null";
            ddl.identitySelectString = "select currval('_table__col_seq')";
            ddl.identitySelectStringBigINT = "select currval('_table__col_seq')";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.requiresParensForTupleDistinctCounts = true;
            ddl.selectSequenceNextValString = "nextval ('_SEQNAME')";
            ddl.sequenceNextValString = "select nextval ('_SEQNAME')";
            ddl.supportsIdentityColumns = true;
            ddl.supportsIfExistsBeforeConstraintName = true;
        }
            break;
        case ProgressDialect: {
            ddl.addColumnString = "add column";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.hasAlterTable = false;
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case RDMSOS2200Dialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = "";
            ddl.dropTableString = "drop table _TABLENAME including contents";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = "select permuted_id('NEXT',31) from rdms.rdms_dummy where key_col = 1 ";
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case SAPDBDialect: {
            ddl.addFKeyRefPkeyString = " foreign key _FKEYNAME (_FK1, _FK2) references _REFTABLE";
            ddl.addForeignKeyConstraintString = " foreign key _FKEYNAME (_FK1, _FK2) references _REFTABLE (_REF1, _REF2)";
            ddl.addPrimaryKeyConstraintString = " primary key ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.nullColumnString = " null";
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
        }
            break;
        case SQLServerDialect: {
            ddl.closeQuote = "]";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.currentSchemaCommand = "SELECT SCHEMA_NAME()";
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "[";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case SQLServer2005Dialect: {
            ddl.closeQuote = "]";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.currentSchemaCommand = "SELECT SCHEMA_NAME()";
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "[";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case SQLServer2008Dialect: {
            ddl.closeQuote = "]";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.currentSchemaCommand = "SELECT SCHEMA_NAME()";
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "[";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case SQLServer2012Dialect: {
            ddl.closeQuote = "]";
            ddl.currentSchemaCommand = "SELECT SCHEMA_NAME()";
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.openQuote = "[";
            ddl.selectSequenceNextValString = "next value for _SEQNAME";
            ddl.sequenceNextValString = "select next value for _SEQNAME";
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
        }
            break;
        case SybaseDialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.currentSchemaCommand = "select db_name()";
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case Sybase11Dialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.currentSchemaCommand = "select db_name()";
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case SybaseAnywhereDialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.currentSchemaCommand = "select db_name()";
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case SybaseASE15Dialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.currentSchemaCommand = "select db_name()";
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case SybaseASE157Dialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.currentSchemaCommand = "select db_name()";
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "identity not null";
            ddl.identityColumnStringBigINT = "identity not null";
            ddl.identitySelectString = "select @@identity";
            ddl.identitySelectStringBigINT = "select @@identity";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.nullColumnString = " null";
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
            ddl.tableTypeString = " lock datarows";
        }
            break;
        case TeradataDialect: {
            ddl.addColumnString = "Add Column";
            ddl.createMultisetTableString = "create multiset table ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case Teradata14Dialect: {
            ddl.addColumnString = "Add";
            ddl.createMultisetTableString = "create multiset table ";
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.createSequenceStrings = NOT_SUPPORT;
            ddl.dropSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.identityColumnString = "generated by default as identity not null";
            ddl.identityColumnStringBigINT = "generated by default as identity not null";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.selectSequenceNextValString = NOT_SUPPORT;
            ddl.sequenceNextValString = NOT_SUPPORT;
            ddl.supportsCommentOn = false;
            ddl.supportsIdentityColumns = true;
            ddl.supportsPooledSequences = false;
            ddl.supportsSequences = false;
        }
            break;
        case TimesTenDialect: {
            ddl.createPooledSequenceStrings = NOT_SUPPORT;
            ddl.dropTableString = "drop table _TABLENAME";
            ddl.needDropConstraintsBeforeDropTable = true;
            ddl.sequenceNextValString = "select first 1 _SEQNAME.nextval from sys.tables";
            ddl.supportsColumnCheck = false;
            ddl.supportsCommentOn = false;
            ddl.supportsPooledSequences = false;
            ddl.supportsTableCheck = false;
        }
            break;
        default:
        }
    }

    //==========Garbage code below: getter & setters=========
    
    public String getAddColumnString() {
        return addColumnString;
    }

    public void setAddColumnString(String addColumnString) {
        this.addColumnString = addColumnString;
    }

    public String getAddColumnSuffixString() {
        return addColumnSuffixString;
    }

    public void setAddColumnSuffixString(String addColumnSuffixString) {
        this.addColumnSuffixString = addColumnSuffixString;
    }

    public String getAddForeignKeyConstraintString() {
        return addForeignKeyConstraintString;
    }

    public void setAddForeignKeyConstraintString(String addForeignKeyConstraintString) {
        this.addForeignKeyConstraintString = addForeignKeyConstraintString;
    }

    public String getAddFKeyRefPkeyString() {
        return addFKeyRefPkeyString;
    }

    public void setAddFKeyRefPkeyString(String addFKeyRefPkeyString) {
        this.addFKeyRefPkeyString = addFKeyRefPkeyString;
    }

    public String getAddPrimaryKeyConstraintString() {
        return addPrimaryKeyConstraintString;
    }

    public void setAddPrimaryKeyConstraintString(String addPrimaryKeyConstraintString) {
        this.addPrimaryKeyConstraintString = addPrimaryKeyConstraintString;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getCreateCatalogCommand() {
        return createCatalogCommand;
    }

    public void setCreateCatalogCommand(String createCatalogCommand) {
        this.createCatalogCommand = createCatalogCommand;
    }

    public String getCreateMultisetTableString() {
        return createMultisetTableString;
    }

    public void setCreateMultisetTableString(String createMultisetTableString) {
        this.createMultisetTableString = createMultisetTableString;
    }

    public String getCreatePooledSequenceStrings() {
        return createPooledSequenceStrings;
    }

    public void setCreatePooledSequenceStrings(String createPooledSequenceStrings) {
        this.createPooledSequenceStrings = createPooledSequenceStrings;
    }

    public String getCreateSchemaCommand() {
        return createSchemaCommand;
    }

    public void setCreateSchemaCommand(String createSchemaCommand) {
        this.createSchemaCommand = createSchemaCommand;
    }

    public String getCreateSequenceStrings() {
        return createSequenceStrings;
    }

    public void setCreateSequenceStrings(String createSequenceStrings) {
        this.createSequenceStrings = createSequenceStrings;
    }

    public String getCreateTableString() {
        return createTableString;
    }

    public void setCreateTableString(String createTableString) {
        this.createTableString = createTableString;
    }

    public String getCurrentSchemaCommand() {
        return currentSchemaCommand;
    }

    public void setCurrentSchemaCommand(String currentSchemaCommand) {
        this.currentSchemaCommand = currentSchemaCommand;
    }

    public String getDropCatalogCommand() {
        return dropCatalogCommand;
    }

    public void setDropCatalogCommand(String dropCatalogCommand) {
        this.dropCatalogCommand = dropCatalogCommand;
    }

    public String getDropForeignKeyString() {
        return dropForeignKeyString;
    }

    public void setDropForeignKeyString(String dropForeignKeyString) {
        this.dropForeignKeyString = dropForeignKeyString;
    }

    public String getDropSchemaCommand() {
        return dropSchemaCommand;
    }

    public void setDropSchemaCommand(String dropSchemaCommand) {
        this.dropSchemaCommand = dropSchemaCommand;
    }

    public String getDropSequenceStrings() {
        return dropSequenceStrings;
    }

    public void setDropSequenceStrings(String dropSequenceStrings) {
        this.dropSequenceStrings = dropSequenceStrings;
    }

    public String getDropTableString() {
        return dropTableString;
    }

    public void setDropTableString(String dropTableString) {
        this.dropTableString = dropTableString;
    }

    public Boolean getHasAlterTable() {
        return hasAlterTable;
    }

    public void setHasAlterTable(Boolean hasAlterTable) {
        this.hasAlterTable = hasAlterTable;
    }

    public Boolean getHasDataTypeInIdentityColumn() {
        return hasDataTypeInIdentityColumn;
    }

    public void setHasDataTypeInIdentityColumn(Boolean hasDataTypeInIdentityColumn) {
        this.hasDataTypeInIdentityColumn = hasDataTypeInIdentityColumn;
    }

    public String getIdentityColumnString() {
        return identityColumnString;
    }

    public void setIdentityColumnString(String identityColumnString) {
        this.identityColumnString = identityColumnString;
    }

    public String getIdentityColumnStringBigINT() {
        return identityColumnStringBigINT;
    }

    public void setIdentityColumnStringBigINT(String identityColumnStringBigINT) {
        this.identityColumnStringBigINT = identityColumnStringBigINT;
    }

    public String getIdentitySelectString() {
        return identitySelectString;
    }

    public void setIdentitySelectString(String identitySelectString) {
        this.identitySelectString = identitySelectString;
    }

    public String getIdentitySelectStringBigINT() {
        return identitySelectStringBigINT;
    }

    public void setIdentitySelectStringBigINT(String identitySelectStringBigINT) {
        this.identitySelectStringBigINT = identitySelectStringBigINT;
    }

    public Boolean getNeedDropConstraintsBeforeDropTable() {
        return needDropConstraintsBeforeDropTable;
    }

    public void setNeedDropConstraintsBeforeDropTable(Boolean needDropConstraintsBeforeDropTable) {
        this.needDropConstraintsBeforeDropTable = needDropConstraintsBeforeDropTable;
    }

    public String getNullColumnString() {
        return nullColumnString;
    }

    public void setNullColumnString(String nullColumnString) {
        this.nullColumnString = nullColumnString;
    }

    public Boolean getRequiresParensForTupleDistinctCounts() {
        return requiresParensForTupleDistinctCounts;
    }

    public void setRequiresParensForTupleDistinctCounts(Boolean requiresParensForTupleDistinctCounts) {
        this.requiresParensForTupleDistinctCounts = requiresParensForTupleDistinctCounts;
    }

    public String getSelectSequenceNextValString() {
        return selectSequenceNextValString;
    }

    public void setSelectSequenceNextValString(String selectSequenceNextValString) {
        this.selectSequenceNextValString = selectSequenceNextValString;
    }

    public String getSequenceNextValString() {
        return sequenceNextValString;
    }

    public void setSequenceNextValString(String sequenceNextValString) {
        this.sequenceNextValString = sequenceNextValString;
    }

    public Boolean getSupportsColumnCheck() {
        return supportsColumnCheck;
    }

    public void setSupportsColumnCheck(Boolean supportsColumnCheck) {
        this.supportsColumnCheck = supportsColumnCheck;
    }

    public Boolean getSupportsCommentOn() {
        return supportsCommentOn;
    }

    public void setSupportsCommentOn(Boolean supportsCommentOn) {
        this.supportsCommentOn = supportsCommentOn;
    }

    public Boolean getSupportsIdentityColumns() {
        return supportsIdentityColumns;
    }

    public void setSupportsIdentityColumns(Boolean supportsIdentityColumns) {
        this.supportsIdentityColumns = supportsIdentityColumns;
    }

    public Boolean getSupportsIfExistsAfterConstraintName() {
        return supportsIfExistsAfterConstraintName;
    }

    public void setSupportsIfExistsAfterConstraintName(Boolean supportsIfExistsAfterConstraintName) {
        this.supportsIfExistsAfterConstraintName = supportsIfExistsAfterConstraintName;
    }

    public String getOpenQuote() {
        return openQuote;
    }

    public void setOpenQuote(String openQuote) {
        this.openQuote = openQuote;
    }

    public String getCloseQuote() {
        return closeQuote;
    }

    public void setCloseQuote(String closeQuote) {
        this.closeQuote = closeQuote;
    }

    public Boolean getSupportsIfExistsAfterTableName() {
        return supportsIfExistsAfterTableName;
    }

    public void setSupportsIfExistsAfterTableName(Boolean supportsIfExistsAfterTableName) {
        this.supportsIfExistsAfterTableName = supportsIfExistsAfterTableName;
    }

    public Boolean getSupportsIfExistsBeforeConstraintName() {
        return supportsIfExistsBeforeConstraintName;
    }

    public void setSupportsIfExistsBeforeConstraintName(Boolean supportsIfExistsBeforeConstraintName) {
        this.supportsIfExistsBeforeConstraintName = supportsIfExistsBeforeConstraintName;
    }

    public Boolean getSupportsIfExistsBeforeTableName() {
        return supportsIfExistsBeforeTableName;
    }

    public void setSupportsIfExistsBeforeTableName(Boolean supportsIfExistsBeforeTableName) {
        this.supportsIfExistsBeforeTableName = supportsIfExistsBeforeTableName;
    }

    public Boolean getSupportsInsertSelectIdentity() {
        return supportsInsertSelectIdentity;
    }

    public void setSupportsInsertSelectIdentity(Boolean supportsInsertSelectIdentity) {
        this.supportsInsertSelectIdentity = supportsInsertSelectIdentity;
    }

    public Boolean getSupportsPooledSequences() {
        return supportsPooledSequences;
    }

    public void setSupportsPooledSequences(Boolean supportsPooledSequences) {
        this.supportsPooledSequences = supportsPooledSequences;
    }

    public Boolean getSupportsSequences() {
        return supportsSequences;
    }

    public void setSupportsSequences(Boolean supportsSequences) {
        this.supportsSequences = supportsSequences;
    }

    public Boolean getSupportsTableCheck() {
        return supportsTableCheck;
    }

    public void setSupportsTableCheck(Boolean supportsTableCheck) {
        this.supportsTableCheck = supportsTableCheck;
    }

    public String getTableTypeString() {
        return tableTypeString;
    }

    public void setTableTypeString(String tableTypeString) {
        this.tableTypeString = tableTypeString;
    } 
    
}
