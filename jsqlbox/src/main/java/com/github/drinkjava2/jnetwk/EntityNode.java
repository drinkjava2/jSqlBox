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
package com.github.drinkjava2.jnetwk;

import java.util.HashSet;
import java.util.Set;

/**
 * EntityNode is a POJO represents a Node in EntityNet, Node allow have many parents,
 * but Node do not allow have any child node, so "Many to one" is the only
 * relationship allowed in EntityNet system, exact like Relational Database.
 * It's easy to understand: if a ManyToOne relationship be defined in EntityNet,
 * the reverse is oneToMany, so we no need invent a oneToMany connection.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNode {
	String id;
	Object entity;
	Set<String> parentIDs = new HashSet<String>();

	public EntityNode() {
	}

	public EntityNode(String id, Object entity, Set<String> parentIDs) {
		this.id = id;
		this.entity = entity;
		this.parentIDs = parentIDs;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public Set<String> getParentIDs() {
		return parentIDs;
	}

	public void setParentIDs(Set<String> parentIDs) {
		this.parentIDs = parentIDs;
	}

}
