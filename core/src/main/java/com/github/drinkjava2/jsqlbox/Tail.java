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

/**
 * Tail is an empty entity only used to deal tables no any entity mapping, so
 * each CURD need add a tail("table") as parameter, for example:
 * 
 * new Tail().putTail("user_name", "Tom", "age", 10).insert(JSQLBOX.tail("some_table"));
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class Tail extends ActiveRecord<Tail> {
}