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
package com.github.drinkjava2.jsqlbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * GraphQuery, usage see jSqlBox user manual
 * 
 * @author Yong Zhu
 * @since 5.0.14
*/
@SuppressWarnings("all")
public class GraphQuery { //DQL Object
    private String key; //key name
    private Object[] sqlItems = new Object[]{}; //normal sql items
    private List<GraphQuery> childGQ = new ArrayList<GraphQuery>(); //child GraphQuery List
    private String[] masterIds; //master slave Ids,
    private String[] slaveIds; //master slave Ids,
    private boolean one = false; //if one is true, store result in Object or Map, not in list<Object> or List<Map>
    private Class<?> entity; //optional, if not null, conver sql result Map to entity
    private List<Object> records; //each record is a line in database table, Object here can be entity or Map

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object[] getSqlItems() {
        return sqlItems;
    }

    public void setSqlItems(Object[] sqlItems) {
        this.sqlItems = sqlItems;
    }

    public List<GraphQuery> getChildGQ() {
        return childGQ;
    }

    public void setChildGQ(List<GraphQuery> childGQ) {
        this.childGQ = childGQ;
    }

    public List<Object> getRecords() {
        return records;
    }

    public void setRecords(List<Object> records) {
        this.records = records;
    }

    public String[] getMasterIds() {
        return masterIds;
    }

    public void setMasterIds(String[] masterIds) {
        this.masterIds = masterIds;
    }

    public String[] getSlaveIds() {
        return slaveIds;
    }

    public void setSlaveIds(String[] slaveIds) {
        this.slaveIds = slaveIds;
    }

    public boolean isOne() {
        return one;
    }

    public void setOne(boolean one) {
        this.one = one;
    }

    public Class<?> getEntity() {
        return entity;
    }

    public void setEntity(Class<?> entity) {
        this.entity = entity;
    }

    private static Object readValueFromMapOrEntity(GraphQuery q, Object mapOrEntity, String field) {//as title
        if (q == null || mapOrEntity == null)
            return null;
        if (q.getEntity() != null)
            return ClassCacheUtils.readValueFromBeanField(q.getEntity(), mapOrEntity, field);
        else
            return ((Map) mapOrEntity).get(field);
    }

    private static void writeValueToMapOrEntity(GraphQuery q, Object mapOrEntity, String field, Object value) {//as title
        if (q == null || mapOrEntity == null)
            return;
        if (q.getEntity() != null)
            ClassCacheUtils.writeValueToBeanField(q.getEntity(), mapOrEntity, field, value);
        else
            ((Map) mapOrEntity).put(field, value);
    }

    /** return a sqlitem, name is "MASTERSLAVE_IDS", parameters store master and slave ids, usage: masterSlave("masterId1", "masterId2", "slaveId1", "slaveId2") */
    public static SqlItem masterSlave(String... masterSlaveIds) {
        return new SqlItem("MASTERSLAVE_IDS", null, (Object[]) masterSlaveIds);
    }

    /** change key setting */
    public static SqlItem key(String key) {
        return new SqlItem("KEY", null, new Object[]{key});
    }

    /** Set a entityClass item */
    public static SqlItem entity(Class<?> entityClass) {
        return new SqlItem("ENTITY", null, new Object[]{entityClass});
    }

    /** tell child only have one record, no need put in records list, but put in record */
    public static SqlItem one() {
        return new SqlItem("ONE", null, new Object[]{});
    }

    /**
     * return a DQL Object, items stored as normal sqlItems except extract key and masterSlave Ids. 
     */
    public static GraphQuery graphQuery(Object... items) {
        GraphQuery q = new GraphQuery();
        DbException.assureTrue(items != null && items.length > 0, "DQL items can not be empty");
        q.setSqlItems(items);

        String key = StrUtils.replace((String) items[0], "\t", " "); // start to find key
        key = StrUtils.trimWhitespace(key);
        DbException.assureNotEmpty(key);
        if (key.contains(" ")) //if key have space use last word as key,  for example "select * from usr as u", will use "u" as key
            key = StrUtils.substringAfterLast(key, " ");
        q.setKey(key);

        if (StrUtils.startsWithIgnoreCase((String) (q.sqlItems[0]), "select "))
            q.sqlItems[0] = q.sqlItems[0] + " ";
        else
            q.sqlItems[0] = "select * from " + q.sqlItems[0] + " ";

        for (int i = 0; i < items.length; i++) {
            Object item = items[i];
            if (item instanceof SqlItem) { //if is masterSlave, key item
                SqlItem sqlItem = (SqlItem) item;
                if ("MASTERSLAVE_IDS".equals(sqlItem.getName())) {
                    String[] msids = (String[]) sqlItem.getParameters();
                    int idQTY = msids.length / 2;
                    String[] masterIds = new String[idQTY];
                    String[] slaveIds = new String[idQTY];
                    System.arraycopy(msids, 0, masterIds, 0, idQTY);//masterIds
                    System.arraycopy(msids, idQTY, slaveIds, 0, idQTY);//childIds 
                    q.setMasterIds(masterIds);
                    q.setSlaveIds(slaveIds);
                    items[i] = ""; //remove this item by change it to "".   
                } else if ("KEY".equals(sqlItem.getName())) {
                    q.setKey((String) sqlItem.getParameters()[0]); //change key to given value
                    items[i] = "";
                } else if ("ONE".equals(sqlItem.getName())) {
                    q.setOne(true);
                    items[i] = "";
                } else if ("ENTITY".equals(sqlItem.getName())) {
                    q.setEntity((Class<?>) sqlItem.getParameters()[0]);
                    items[i] = "";
                }
            }
        }
        return q;
    }

    /** Execute GraphQuery, allow multiple GraphQuerys  */
    public static Map<String, Object> graphQuery(DbContext db, GraphQuery... dqls) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (GraphQuery q : dqls) {
            singleGraphQuery(db, null, q);
            if (q.isOne()) {
                if (q.getRecords() != null && !q.getRecords().isEmpty())
                    result.put(q.getKey(), q.getRecords().get(0));
            } else
                result.put(q.getKey(), q.getRecords());
        }
        return result;
    }

    /** Execute single DQL query, parent can be null */
    private static void singleGraphQuery(DbContext db, GraphQuery parent, GraphQuery self) {
        if (parent != null && ((parent.getRecords() == null || parent.getRecords().isEmpty())))
            return;
        List sqlItems = new ArrayList<>();
        for (Object item : self.getSqlItems())
            if (!(item instanceof GraphQuery)) //dql object is not sqlItem, need exclude
                sqlItems.add(item);

        //start to build a " where (xx,xx) in ((),()...,()) " sql to insert into child's sqlItem 
        List<Object> where = null;
        if (parent != null) {
            DbException.assureTrue(self.getMasterIds() != null && self.getMasterIds().length > 0, "master ids missing, need use ms(\"xx\", \"xx\") method");
            where = new ArrayList<>();
            where.add(" where (");
            for (String id : self.slaveIds) {
                where.add(id);
                where.add(",");
            }
            where.remove(where.size() - 1); //remove last ","

            where.add(") in (");
            for (Object m : parent.getRecords()) {
                where.add("(");
                for (String mid : self.getMasterIds()) {
                    where.add("?");
                    where.add(DB.param(readValueFromMapOrEntity(parent, m, mid))); //jSqlBox inline SQl parameter usage
                    where.add(",");
                }
                where.remove(where.size() - 1); //remove last ","
                where.add(")");
                where.add(",");
            }
            where.remove(where.size() - 1); //remove last ","
            where.add(") ");

        }
        if (where != null)
            sqlItems.addAll(1, where);
        List<Map<String, Object>> records = db.qryMapList(sqlItems.toArray(new Object[sqlItems.size()]));
        if (self.getEntity() == null)
            self.setRecords((List) records);
        else {
            List<Object> beanList = new ArrayList<>();
            TableModel model = TableModelUtils.entity2Model(self.getEntity());
            for (Map<String, Object> row : records) {
                Object entityBean = DbContextUtils.mapToEntityBean(model, row); //Conver to entityBean
                beanList.add(entityBean);
            }
            self.setRecords(beanList);
        }

        alignMasterSlaveData(parent, self); //if have parent, align master and slave data by id 
        for (Object item : self.getSqlItems()) //child DQL query
            if (item instanceof GraphQuery)
                singleGraphQuery(db, self, (GraphQuery) item);
    }

    private static void alignMasterSlaveData(GraphQuery parent, GraphQuery child) {
        if (parent == null || child == null || child.getMasterIds() == null || child.getSlaveIds() == null || (child.getMasterIds().length != child.getSlaveIds().length))
            return;
        if (parent.getRecords() == null || child.getRecords() == null || parent.getRecords().isEmpty() || child.getRecords().isEmpty())
            return;
        int idQTY = child.getMasterIds().length;
        for (Object mRecord : parent.getRecords()) {
            Object[] mIdValue = new Object[idQTY];
            for (int i = 0; i < idQTY; i++)
                mIdValue[i] = readValueFromMapOrEntity(parent, mRecord, child.getMasterIds()[i]); //parent id values, may be compound ids 

            for (Object cRecord : child.getRecords()) {
                Object[] cIdValue = new Object[idQTY];
                for (int i = 0; i < idQTY; i++)
                    cIdValue[i] = readValueFromMapOrEntity(child, cRecord, child.getSlaveIds()[i]);

                boolean foundOne = false;
                if (idValueEqual(mIdValue, cIdValue)) { //if id equal, link child record to master record
                    if (child.isOne()) {
                        foundOne = true;
                        writeValueToMapOrEntity(parent, mRecord, child.getKey(), cRecord);
                    } else {
                        List l = (List) readValueFromMapOrEntity(parent, mRecord, child.getKey());
                        if (l == null) {
                            l = new ArrayList<>();
                            writeValueToMapOrEntity(parent, mRecord, child.getKey(), l);
                        }
                        l.add(cRecord);
                    }
                }
                if (child.isOne() && foundOne) //if child is one and found one for parent, break
                    break;
            }
        }
    }

    private static boolean idValueEqual(Object[] idValue1, Object[] idValue2) {//compare 2 object array
        for (int i = 0; i < idValue1.length; i++) {
            Object o1 = idValue1[i];
            Object o2 = idValue2[i];
            if (o1 == null || o2 == null || !o1.equals(o2))
                return false;
        }
        return true;
    }
}