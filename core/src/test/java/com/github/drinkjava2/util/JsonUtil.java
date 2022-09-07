/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 
 * Small Jackson util， all methods will return null if exception happen
 * to use this utility, need add below dependency in pom.xml:
 * 
 *  <dependency>
 *      <groupId>com.fasterxml.jackson.core</groupId>
 *      <artifactId>jackson-databind</artifactId>
 *       <version>2.13.3</version> <!--or latest version-->
 *  </dependency>
 *  
 *  
 * @author Yong
 */
public class JsonUtil {
    //为了避免每次 new ObjectMapper()，这里全局使用单例。如果不同方法需要有不同配置的，以后可以用多个不同配置的单例 
    public static final ObjectMapper singleTonObjectMapper_NON_NULL = new ObjectMapper(); //如果返回json中字段为null，则忽略该字段

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        singleTonObjectMapper_NON_NULL.setSerializationInclusion(Include.NON_NULL);
        singleTonObjectMapper_NON_NULL.setDateFormat(dateFormat); 
    }

    /**
     * 从json字符串中直接用下面语法读取节点对象，返回值可能是Map/List/基本类型或它们的组合：
     * Object obj = JsonUtil.get(json,"node1.node2.x.node4"); //x表示列表下标序号
     * 如获取失败返回null 
     */
    private static Object getFromJsonStr(String json, String nodeKey) {
        Object obj=null;
        try {
             obj = singleTonObjectMapper_NON_NULL.readValue(json, Object.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return get(obj, nodeKey);
    }
    
    /** 如果一个obj是Map/list/基本类型或它们的组合，用下面语法读取节点对象：
     * Object obj = JsonUtil.get(json,"node1.node2.x.node4"); //x表示列表下标序号
     * 如获取失败返回null 
     */
    @SuppressWarnings("unchecked")
    public static Object get(Object obj, String nodeKey) {
            if(obj instanceof String)
                return getFromJsonStr(((String)obj),nodeKey);
            int pos;
            String key = nodeKey;
            do {
                pos = key.indexOf(".");
                if (pos >= 0) {
                    if (obj == null)
                        throw new RuntimeException("Node key '" + nodeKey + "' does not exist.");
                    if (obj instanceof Map)
                        obj = ((Map<String, Object>) obj).get(key.substring(0, pos));
                    else
                        obj = ((List<?>) obj).get(Integer.parseInt(key.substring(0, pos)));
                    key = key.substring(pos + 1);
                } else {
                    if (obj == null)
                        throw new RuntimeException("Node key '" + nodeKey + "' does not exist.");
                    if (obj instanceof Map)
                        obj = ((Map<String, Object>) obj).get(key);
                    else
                        obj = ((List<?>) obj).get(Integer.parseInt(key));
                    return obj;
                }
            } while (true);
    }

    /** 把json字符串转为指定类的实例对象, 如失败返回null  */
    public static <T> T toObj(String json, Class<T> claz) {
        try {
            return singleTonObjectMapper_NON_NULL.readValue(json, claz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** 把object转为json, 如失败返回null  */
    public static String toJSON(Object object) {
        ObjectWriter objectWriter = singleTonObjectMapper_NON_NULL.writer();
        return toJSON(object, objectWriter);
    }

    /** 把object转为格式化json, 如失败返回null  */
    public static String toJSONFormatted(Object object) {
        ObjectWriter objectWriter = singleTonObjectMapper_NON_NULL.writer().withDefaultPrettyPrinter();
        return toJSON(object, objectWriter);
    }

    /** 把json字符串格式化, 如失败返回null  */
    public static String formatJSON(String input) {
        Object json;
        String indented = "";
        try {
            json = singleTonObjectMapper_NON_NULL.readValue(input, Object.class);
            indented = singleTonObjectMapper_NON_NULL.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return indented;
    }

    /** 私有方法，以指定的ObjectWriter转object为json字符串 , 如失败返回null */
    private static String toJSON(Object object, ObjectWriter objectWriter) {
        try {
            return objectWriter.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getAsText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null)
            return null;
        String s = n.asText();
        if (s==null || s.length()==0)
            s = n.toString();
        return s;
    }
}