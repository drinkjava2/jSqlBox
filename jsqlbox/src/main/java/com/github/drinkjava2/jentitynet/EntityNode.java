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
package com.github.drinkjava2.jentitynet;

import java.util.HashSet;
import java.util.Set;

/**
 * EntityNode is a POJO represents a Node in EntityNet, Node allow have many
 * parents, but Node do not allow have child node, so "Many to one" is the only
 * relationship allowed in EntityNet system, exact like Relational Database, so
 * it's easy to translate a Relational Database into an EntityNet.
 * 
 * It's easy to understand there is no oneToMany relationship: if a ManyToOne be
 * defined in EntityNet, the reverse we can call it oneToMany, so we no need
 * worry there is no oneToMany.
 * 
 * How to determine which one should be "One" side determined by programmer or
 * database structure, for example usually student is "many" , teacher is "one",
 * but if in a school has much more teachers than students, can set student as
 * teacher's parent
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNode {
	/** a unique String represents the Node */
	String id;

	Object entity;

	Object entityClass;

	/** Label is not unique, many nodes can have same label name */
	String label;

	/** Point to parent nodes' id, a node can have many parents */
	Set<String> parentIDs = new HashSet<String>();

	public EntityNode(String id, Object entity) {
		this.id = id;
		this.entity = entity;
		if (entity != null)
			this.entityClass = entity.getClass();
	}

	public EntityNode(String id, Object entity, Set<String> parentIDs) {
		this(id, entity);
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

	public Object getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Object entityClass) {
		this.entityClass = entityClass;
	}

}
