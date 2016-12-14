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
	 * (Optional) The primary key generation strategy that the persistence
	 * provider must use to generate the entity primary key.
	 */
	private GenerationType generationType = null;

	/**
	 * (Optional) The name of the primary key generator
	 */
	private String generatorName;

	public GeneratedValue(GenerationType generationType) {
		this.generationType = generationType;
	}

	public GeneratedValue(GenerationType generationType, String generatorName) {
		this.generationType = generationType;
		this.generatorName = generatorName;
	}

	public GenerationType getGenerationType() {
		return generationType;
	}

	public void setGenerationType(GenerationType generationType) {
		this.generationType = generationType;
	}

	public String getGeneratorName() {
		return generatorName;
	}

	public void setGeneratorName(String generatorName) {
		this.generatorName = generatorName;
	}

}
