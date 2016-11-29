/**
* Copyright (C) 2016 Yong Zhu.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.drinkjava2.jsqlbox.id;

/**
 * Defines the value of primary key generation strategy, copied from JPA
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class GeneratedValue {
	/**
	 * (Optional) The primary key generation strategy that the persistence provider must use to generate the annotated
	 * entity primary key.
	 * 
	 * @return strategy
	 */
	private GenerationType generationType = null;

	/**
	 * (Optional) The name of the primary key generator to use as specified in the {@link SequenceGenerator} or
	 * {@link TableGenerator} annotation.
	 * <p>
	 * Defaults to the id generator supplied by persistence provider.
	 * 
	 * @return generator
	 */
	private IdGenerator value = null;

	public GeneratedValue(GenerationType generationType) {
		this.generationType = generationType;
	}

	public GeneratedValue(GenerationType generationType, IdGenerator value) {
		this.generationType = generationType;
		this.value = value;
	}

	public GenerationType getGenerationType() {
		return generationType;
	}

	public void setGenerationType(GenerationType generationType) {
		this.generationType = generationType;
	}

	public IdGenerator getValue() {
		return value;
	}

	public void setValue(IdGenerator value) {
		this.value = value;
	}

}
