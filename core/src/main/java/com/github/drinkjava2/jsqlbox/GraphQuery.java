package com.github.drinkjava2.jsqlbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.StrUtils;

@SuppressWarnings("all")
public class GraphQuery { //DQL Object
    //input
    private String key; //key name
    private Object[] sqlItems = new Object[]{}; //normal sql items
    private List<GraphQuery> childGQ = new ArrayList<GraphQuery>(); //child GraphQuery List
    private String[] masterIds; //master slave Ids,
    private String[] slaveIds; //master slave Ids,

    //output
    private List<Map<String, Object>> records; //each record is a line in database table

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

    public List<Map<String, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, Object>> records) {
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

    //========End of getter& setter======= 

    /** return a sqlitem, name is "MASTERSLAVE_IDS", parameters store master and slave ids, usage: masterSlave("masterId1", "masterId2", "slaveId1", "slaveId2") */
    public static SqlItem masterSlave(String... masterSlaveIds) {
        SqlItem item = new SqlItem(null, (Object[]) masterSlaveIds);
        item.setName("MASTERSLAVE_IDS");
        return item;
    }

    /** change key setting */
    public static SqlItem key(String key) {
        SqlItem item = new SqlItem(null, new Object[]{key});
        item.setName("KEY");
        return item;
    }

    
    /**
     * return a DQL Object, items stored as normal sqlItems except extract key and masterSlave Ids. 
     */
    public static GraphQuery graphQL(Object... items) {
        GraphQuery d = new GraphQuery();
        DbException.assureTrue(items != null && items.length > 0, "DQL items can not be empty");
        d.setSqlItems(items);

        //first item is key, can be single word or "select * from key" or "select * from table as key"
        String key = StrUtils.replace((String) items[0], "\t", " ");
        key = StrUtils.trimWhitespace(key);
        DbException.assureNotEmpty(key);
        if (key.contains(" ")) { //if key have space use last word as key,  for example "select * from usr as u", will use "u" as key
            key = StrUtils.substringAfterLast(key, " ");
        }
        d.setKey(key);

        if (StrUtils.startsWithIgnoreCase((String) (d.sqlItems[0]), "select "))
            d.sqlItems[0] = d.sqlItems[0] + " ";
        else
            d.sqlItems[0] = "select * from " + d.sqlItems[0] + " ";

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
                    d.setMasterIds(masterIds);
                    d.setSlaveIds(slaveIds);
                    items[i] = ""; //remove this item by change it to "".   
                } else if ("KEY".equals(sqlItem.getName())) {
                    d.setKey((String) sqlItem.getParameters()[0]); //change key to given value
                    items[i] = ""; //remove this item by change it to "". 
                }
            }
        }
        return d;
    }

    /**
     * Execute DQL query, allow multiple DQL
     * @param db
     * @param dqls
     * @return Map<String, List> result
     */
    public static Map<String, List> graphQuery(DbContext db, GraphQuery... dqls) {
        Map<String, List> result = new HashMap<String, List>();
        for (GraphQuery graphQuery : dqls) {
            singleGraphQuery(db, null, graphQuery);
            result.put(graphQuery.getKey(), graphQuery.getRecords());
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
            for (Map<String, Object> m : parent.getRecords()) {
                where.add("(");
                for (String mid : self.getMasterIds()) {
                    where.add("?");
                    where.add(DB.param(m.get(mid))); //jSqlBox inline SQl parameter usage
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
        self.setRecords(records);
        alignMasterSlaveData(parent, self); //if have parent, align master and slave data by id

        for (Object item : self.getSqlItems()) //child DQL query
            if (item instanceof GraphQuery)
                singleGraphQuery(db, self, (GraphQuery) item);
    }

    /**
     * align master and slave data by id
     * @param parent
     * @param child
     */
    private static void alignMasterSlaveData(GraphQuery parent, GraphQuery child) {
        if (parent == null || child == null || parent.getRecords() == null || child.getRecords() == null || parent.getRecords().isEmpty() || child.getRecords().isEmpty())
            return;
        if (child.getMasterIds() == null || child.getChildGQ() == null || (child.getMasterIds().length != child.getSlaveIds().length))
            return;
        int idQTY = child.getMasterIds().length;
        for (Map<String, Object> mRecord : parent.getRecords()) {
            Object[] mIdValue = new Object[idQTY];
            for (int i = 0; i < idQTY; i++) {
                mIdValue[i] = mRecord.get(child.getMasterIds()[i]);
            }

            for (Map<String, Object> cRecord : child.getRecords()) {
                Object[] cIdValue = new Object[idQTY];
                for (int i = 0; i < idQTY; i++) {
                    cIdValue[i] = cRecord.get(child.getSlaveIds()[i]);
                }

                if (idValueEqual(mIdValue, cIdValue)) { //if id equal, link child record to master record
                    List l = (List) mRecord.get(child.getKey());
                    if (l == null) {
                        l = new ArrayList<>();
                        mRecord.put(child.getKey(), l);
                    }
                    l.add(cRecord);
                }
            }
        }
    }

    private static boolean idValueEqual(Object[] idValue1, Object[] idValue2) {//compare 2 object array
        for (int i = 0; i < idValue1.length; i++) {
            Object o1 = idValue1[i];
            Object o2 = idValue2[i];
            if (o1 == null || o2 == null)
                return false;
            if (!o1.equals(o2))
                return false;
        }
        return true;
    }

}
