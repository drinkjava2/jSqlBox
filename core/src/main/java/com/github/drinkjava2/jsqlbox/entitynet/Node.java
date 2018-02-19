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
package com.github.drinkjava2.jsqlbox.entitynet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Node is the basic unit of entity net, a node allow have many parent nodes,
 * but node do not allow have child node, so "Many to one" is the only
 * relationship allowed in EntityNet system, exact like Relational Database, so
 * it's easy to translate a Relational Database into an EntityNet.
 * 
 * @author Yong Zhu 
 * @since 1.0.0
 */
public class Node {
	/**
	 * A String ID by combining PKey column values into one String
	 */
	String id;

	/** The entity instance */
	Object entity;

	/** Mark how many fields loaded from database */
	Set<String> loadedFields = new HashSet<String>();

	/// ** Mark how many fields be modified */
	// Set<String> modifiedFields;

	private List<ParentRelation> parentRelations;

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

	public List<ParentRelation> getParentRelations() {
		return parentRelations;
	}

	public void setParentRelations(List<ParentRelation> parentRelations) {
		this.parentRelations = parentRelations;
	}

	public Set<String> getLoadedFields() {
		return loadedFields;
	}

	public void setLoadedFields(Set<String> loadedFields) {
		this.loadedFields = loadedFields;
	}

}
