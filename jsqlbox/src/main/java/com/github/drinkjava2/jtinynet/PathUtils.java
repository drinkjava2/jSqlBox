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

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * This class store public static methods concern to Path
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class PathUtils {

	/**
	 * Calculate path's Ref Columns and join it to one String
	 */
	public static String calculateRefColumns(Path path) {
		TableModel childOrParentModelOrSelf = path.targetModel;
		if ("P".equalsIgnoreCase(path.getType().substring(0, 1)))
			childOrParentModelOrSelf = path.fatherPath.targetModel;
		// compare tableModel to determine refs are field names or column names
		StringBuilder sb = new StringBuilder();
		for (String ref : path.refs) {
			boolean found = false;
			for (ColumnModel col : childOrParentModelOrSelf.getColumns()) {
				if (ref.equalsIgnoreCase(col.getEntityField())) {
					if (found)
						throw new TinyNetException("Can't judge '" + ref + "' is a field or a column name.");
					found = true;
					if (sb.length() > 0)
						sb.append(TinyNet.COMPOUND_COLUMNNAME_SEPARATOR);
					sb.append(col.getColumnName());
				} else if (ref.equalsIgnoreCase(col.getColumnName())) {
					if (ref.equalsIgnoreCase(col.getEntityField())) {
						if (found)
							throw new TinyNetException("Can't judge '" + ref + "' is a field or a column name.");
						found = true;
						if (sb.length() > 0)
							sb.append(TinyNet.COMPOUND_COLUMNNAME_SEPARATOR);
						sb.append(ref);
					}
				}
			}
			if (!found)
				throw new TinyNetException("Can't find reference column name for '" + ref + "'  ");
		}
		return sb.toString();
	}

	/**
	 * If has autoPathTarget, this method calculate and build a path chain point to
	 * target, and set the top of the chain as nextPath
	 * 
	 * @param path The start path
	 * @param target The target table name or target class
	 * @param net The TinyNet instance
	 */
	public static void calculateAutoPath(TinyNet net, Path path) {
		if (path.autoPathTarget == null)
			return;
		if (path.nextPath != null)
			throw new TinyNetException("Not allow set autoPath for a path which already have nextPath");
		if (net == null)
			throw new TinyNetException("To calculate auto path, TinyNet instance can not be null");
		Map<Class<?>, TableModel> models = net.getConfigModels();
		if (models == null || models.isEmpty())
			throw new TinyNetException("To calculate auto path, TinyNet's configModels can not be empty");
		Class<?> from = findClassByTarget(net, path.getTarget());
		Class<?> to = findClassByTarget(net, path.autoPathTarget);
		TinyNetException.assureNotNull(from, "Can not find 'From' target when calculate auto path");
		TinyNetException.assureNotNull(from, "Can not find 'To' target when calculate auto path");
		LinkedHashSet<Class<?>> classChain = searchNodePath(models, from, to);
		for (Class<?> clz : classChain) {
			System.out.println(clz);
		}
		Path PathChain = classChainTOPathChain(net, classChain);
	}

	public static Path classChainTOPathChain(TinyNet net, LinkedHashSet<Class<?>> classChain) {
		Class<?> from = null;
		Path path = null;
		int i = 0;
		for (Class<?> clazz : classChain) {
			i++;
			if (i == 1) {
				from = clazz;
			} else {
				if (i == 2)
					path = buildPathByFromAndTo(net, from, clazz);
				if (i == classChain.size()) {
					path.type = StrUtils.replace(path.type, "-", "+");// last will select
				}
			}
		}
		return null;
	}
 
	static class ClassRelation{
		Boolean toIsFromsParent;//true, false, null
		
	}
	
 public static Path buildPathByFromAndTo(TinyNet net, Class<?> from, Class<?> to) {
//		TableModel fromModel=net.getConfigModels().get(from);//User
//		TableModel toModel=net.getConfigModels().get(to); //UserRole
//		List<FKeyModel> fkeyList = fromModel.getFkeyConstraints();
//		for (FKeyModel fKeyModel : fkeyList) {
//			String parentTableName = fKeyModel.getRefTableAndColumns()[0];
//			Class<?> p = findClassByTableName(net.getConfigModels(), parentTableName);
//			if (!checked.contains(c) && p != null && p.equals(last)) {
//				foundClass = c;
//				break;
//			} else if (!checked.contains(p) && c.equals(last)) {
//				foundClass = p;
//				break;
//			}
//			if (foundClass != null)
//				break;
//		}
//		return null;
//	
	 return null;
	}

	/** Find the target class by given target table name or target class */
	public static Class<?> findClassByTarget(TinyNet net, Object target) {
		if (target instanceof String) {
			String tbName = (String) target;
			for (Entry<Class<?>, TableModel> entry : net.getConfigModels().entrySet()) {
				TableModel mod = entry.getValue();
				if (mod != null && tbName.equalsIgnoreCase(mod.getTableName()))
					return entry.getKey();
			}
			throw new TinyNetException("Can not find target class for '" + target + "'");
		} else {
			if (!(target instanceof Class))
				throw new TinyNetException(
						"Target can only be table name string or entity class,  for '" + target + "'");
			return (Class<?>) target;
		}
	}

	/** Find the target class by given table name */
	public static Class<?> findClassByTableName(Map<Class<?>, TableModel> models, String tableName) {
		if (models == null || models.isEmpty())
			return null;
		for (Entry<Class<?>, TableModel> entry : models.entrySet()) {
			if (entry.getValue() != null && tableName.equalsIgnoreCase(entry.getValue().getTableName()))
				return entry.getKey();
		}
		return null;
	}

	/**
	 * @param models
	 * @param from
	 * @param to
	 * @return
	 */
	static LinkedHashSet<Class<?>> searchNodePath(Map<Class<?>, TableModel> models, Class<?> from, Class<?> to) {// NOSONAR
		Set<Class<?>> checked = new HashSet<Class<?>>();
		checked.add(from);
		Set<Class<?>> result = new LinkedHashSet<Class<?>>();
		result.add(from);

		List<Set<Class<?>>> paths = new ArrayList<Set<Class<?>>>();
		paths.add(result);
		int i = 0;
		LinkedHashSet<Class<?>> newPath = null;
		do {
			i++;
			Class<?> foundClass;
			do {
				foundClass = null;
				for (Set<Class<?>> subSet : paths) {
					if (subSet.size() == i) {// NOSONAR
						Class<?> last = getLastElement(subSet);
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
						newPath = new LinkedHashSet<Class<?>>(subSet);
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
		throw new TinyNetException("Not found availible auto path");
	}

	/** Get the last element from ordered Collection */
	static <E> E getLastElement(Collection<E> c) {
		E last = null;
		for (E e : c)
			last = e;
		return last;
	}
}
