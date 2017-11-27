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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

import functiontest.helloworld.SqlStyleDemo.User;
import functiontest.orm.entities.Privilege;

/**
 * This class store public static methods to calculate and set auto path
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class PathCalculateUtils {

	/**
	 * If has autoPathTarget, this method calculate and build a path chain point to
	 * target, and set the top of the chain as nextPath
	 * 
	 * @param path The start path
	 * @param target The target table name or target class
	 * @param net The TinyNet instance
	 */
	public static void calculateAutoPath(Path path, TinyNet net) {
		if (path.autoPathTarget == null)
			return;
		if (path.nextPath != null)
			throw new TinyNetException("Not allow set autoPath for a path which already have nextPath");
		if (net == null)
			throw new TinyNetException("To calculate auto path, TinyNet instance can not be null");
		Map<Class<?>, TableModel> models = net.getConfigModels();
		if (models == null || models.isEmpty())
			throw new TinyNetException("To calculate auto path, TinyNet's configModels can not be empty");
		Set<Class<?>> autoPath = searchNodePath(models, User.class, Privilege.class);
		for (Class<?> clz : autoPath) {
			System.out.print(clz + ",");
		}
	}

	static Class<?> findClassByTableName(Map<Class<?>, TableModel> models, String tableName) {
		if (models == null || models.isEmpty())
			return null;
		for (Entry<Class<?>, TableModel> entry : models.entrySet()) {
			if (entry.getValue() != null && tableName.equalsIgnoreCase(entry.getValue().getTableName()))
				return entry.getKey();
		}
		return null;
	}

	static Set<Class<?>> searchNodePath(Map<Class<?>, TableModel> models, Class<?> from, Class<?> to) {// NOSONAR
		Set<Class<?>> checked = new HashSet<Class<?>>();
		checked.add(from);
		Set<Class<?>> path = new LinkedHashSet<Class<?>>();
		path.add(from);

		List<Set<Class<?>>> paths = new ArrayList<Set<Class<?>>>();
		paths.add(path);
		int i = 0;
		Set<Class<?>> newPath = null;
		do {
			i++;
			Class<?> foundClass;
			do {
				foundClass = null;
				for (Set<Class<?>> set : paths) {
					if (set.size() == i) {// NOSONAR
						Class<?> last = getLastElement(set);
						for (Entry<Class<?>, TableModel> entry : models.entrySet()) {// NOSONAR
							Class<?> c = entry.getKey();
							List<FKeyModel> fkeyList = entry.getValue().getFkeyConstraints();
							for (FKeyModel fKeyModel : fkeyList) {
								String parentTableName = fKeyModel.getRefTableAndColumns()[0];
								Class<?> p = findClassByTableName(models, parentTableName); 
								if (!checked.contains(c) && p != null && p.equals(last)) {
									foundClass = c;
									break;
								} else if (!checked.contains(p) && c.equals(last)) {
									foundClass = p;
									break;
								}
								if (foundClass != null)
									break;
							}
							if (foundClass != null)
								break;
						}
					}
					if (foundClass != null) {// NOSONAR
						newPath = new LinkedHashSet<Class<?>>();
						newPath.add(foundClass);
						if (foundClass.equals(to))
							return newPath;
						checked.add(foundClass);
						break;
					}
				}
				if (newPath != null)
					paths.add(newPath);
			} while (foundClass != null);
		} while (i < 100);
		if (i > 100)
			throw new TinyNetException("Not found availible auto path");
		return path;// this
	}

	/** Get the last element from ordered Collection */
	static <E> E getLastElement(Collection<E> c) {
		E last = null;
		for (E e : c)
			last = e;
		return last;
	}
}
