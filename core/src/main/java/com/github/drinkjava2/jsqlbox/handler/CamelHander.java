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
package com.github.drinkjava2.jsqlbox.handler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.drinkjava2.jdbpro.DefaultOrderSqlHandler;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.StrUtils;

/**
 * Change map、list、arrayCamelHander change all snake_case map key names to camelCase 
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class CamelHander extends DefaultOrderSqlHandler {

    @Override
    public Object afterExecute(ImprovedQueryRunner runner, PreparedSQL ps, Object result) {
        return transToCamel(result);
    }

    public static Object transToCamel(Object data) {
        if (data == null)
            return null;
        if (data instanceof Map) {//map?
            Map<String, Object> oldMp = (Map<String, Object>) data;
            if (oldMp.isEmpty())
                return oldMp;
            Map<String, Object> newMp = new LinkedHashMap<String, Object>();
            for (Entry<String, Object> ent : oldMp.entrySet()) {
                String camelKey = StrUtils.underScoreToCamel(ent.getKey().toLowerCase());
                newMp.put(camelKey, ent.getValue());
            }
            return newMp;
        } else if (data instanceof List) {//list?
            List<Object> list = (List<Object>) data;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, transToCamel(list.get(i)));
            }
            return list;
        } else if (data.getClass().isArray()) {//array?
            Object[] arr = (Object[]) data;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = transToCamel(arr[i]);
            }
            return arr;
        }
        return data;
    }

}
