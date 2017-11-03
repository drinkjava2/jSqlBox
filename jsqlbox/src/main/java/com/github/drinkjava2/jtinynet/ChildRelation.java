/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jtinynet;

/**
 * ChildRelation of node determined by 3 facts: childClass, childId,
 * childFkeyColumnNames
 * 
 * For example: <br/>
 * Student.class, Tom_Li, student_fisrtName_lastName  <br/> 
 * Student.class, Sam_Zhu, student_fisrtName_lastName  <br/> 
 * SuperStudent.class, Tom_li, student_fisrtName_lastName  <br/> 
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class ChildRelation {
	Class<?> childClass;
	String childId;
	String childFkeyColumnNames;

	public Class<?> getChildClass() {
		return childClass;
	}

	public void setChildClass(Class<?> childClass) {
		this.childClass = childClass;
	}

	public String getChildId() {
		return childId;
	}

	public void setChildId(String childId) {
		this.childId = childId;
	}

	public String getChildFkeyColumnNames() {
		return childFkeyColumnNames;
	}

	public void setChildFkeyColumnNames(String childFkeyColumnNames) {
		this.childFkeyColumnNames = childFkeyColumnNames;
	}

}
