/**
 * Copyright 2016 the original author or authors.
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
package com.github.drinkjava2.jsqlbox.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.handler.CamelHander;

/**
 * This is function test for SqlHandlers
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class CamelHanderTest extends TestBase {

    @Table(name = "DemoUser")
    public static class CamelHanderUser extends ActiveRecord<CamelHanderUser> {
        @Id
        String id;
        @Column(name = "user_name")
        String userName;
        @Column(name = "user_age")
        Integer userAge;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Integer getUserAge() {
            return userAge;
        }

        public void setUserAge(Integer userAge) {
            this.userAge = userAge;
        }

    }

    @Before
    public void init() {
        super.init();
        createAndRegTables(CamelHanderUser.class);
        for (int i = 0; i < 50; i++)
            new CamelHanderUser().putField("id", "" + i).putField("userName", "user" + i).putField("userAge", i).insert();
    }

    @Test
    public void testCamelHandler() {
        Map<String, Object> mp = DB.qryMap("select * from DemoUser order by id ", DB.pagin(1, 10));
        System.out.println(mp);
        mp = DB.qryMap("select * from DemoUser order by id ", DB.pagin(1, 10), new CamelHander());
        System.out.println(mp);

        List<Map<String, Object>> listMp = DB.qryMapList("select * from DemoUser order by id ", DB.pagin(1, 10));
        System.out.println(listMp);

        listMp = DB.qryMapList("select * from DemoUser order by id ", DB.pagin(1, 10), new CamelHander());
        System.out.println(listMp);
    }

    @Test
    public void testCamelHandlerStaticMethod() {
        List<Object> l = new ArrayList<Object>();
        l.add("aa");
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("usr_name", "Sam");
        l.add(m);
        Object[] a = new Object[10];
        a[1] = l;
        a[2] = l;
        a = (Object[]) CamelHander.transToCamel(a);
        System.out.println(Arrays.asList(a));
    }

}