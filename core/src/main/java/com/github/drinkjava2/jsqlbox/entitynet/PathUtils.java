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
 * @author Yong Zhu 
 * @since 1.0.0
 */
public class PathUtils {

	/**
	 * Calculate path's Ref Columns and join it to one String
	 */
	static String calculateRefColumns(Path path) {
		TableModel childOrParentModelOrSelf = path.targetModel;
		if ("P".equalsIgnoreCase(path.getType().substring(0, 1)))
			childOrParentModelOrSelf = path.fatherPath.targetModel;
		// compare tableModel to determine refs are field names or column names
		StringBuilder sb = new StringBuilder();
		for (String ref : path.refs) {
			boolean found = false;
			for (ColumnModel col : childOrParentModelOrSelf.getColumns()) {
				if (ref != null && ref.equalsIgnoreCase(col.getEntityField())) {
					if (found)
						throw new EntityNetException("Can't judge '" + ref + "' is a field or a column name.");
					found = true;
					if (sb.length() > 0)
						sb.append(EntityNet.COMPOUND_COLUMNNAME_SEPARATOR);
					sb.append(col.getColumnName());
				} else if (ref != null && ref.equalsIgnoreCase(col.getColumnName())) {
					if (ref.equalsIgnoreCase(col.getEntityField())) {
						if (found)
							throw new EntityNetException("Can't judge '" + ref + "' is a field or a column name.");
						found = true;
						if (sb.length() > 0)
							sb.append(EntityNet.COMPOUND_COLUMNNAME_SEPARATOR);
						sb.append(ref);
					}
				}
			}
			if (!found)
				throw new EntityNetException("Can't find reference column name for '" + ref + "'  ");
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
	static void calculateAutoPath(EntityNet net, Path path) {
		if (path.getAutoPathTarget() == null)
			return;
		if (net == null)
			throw new EntityNetException("To calculate auto path, TinyNet instance can not be null");
		Map<Class<?>, TableModel> models = net.getConfigModels();
		if (models == null || models.isEmpty())
			throw new EntityNetException("To calculate auto path, TinyNet's configModels can not be empty");
		Class<?> from = findClassByTarget(net, path.getTarget());
		Class<?> to = findClassByTarget(net, path.autoPathTarget);
		EntityNetException.assureNotNull(from, "Can not find 'From' target when calculate auto path");
		EntityNetException.assureNotNull(from, "Can not find 'To' target when calculate auto path");
		Set<Class<?>> classChain = searchNodePath(models, from, to);
		Path pathChain = classChainTOPathChain(net, classChain);
		Path oldNextPath = path.getNextPath();
		path.setNextPath(pathChain);
		if (oldNextPath != null)
			path.getBottomPath().setNextPath(oldNextPath);
	}

	static Path classChainTOPathChain(EntityNet net, Set<Class<?>> classChain) {
		Class<?> from = null;
		Path path = null;
		int i = 0;
		for (Class<?> to : classChain) {
			i++;
			if (i >= 2) {
				Path thisPath = buildPathByFromAndTo(net, from, to);
				if (i == classChain.size())
					thisPath.type = StrUtils.replaceFirst(thisPath.type, "-", "+");// last
				if (i == 2)
					path = thisPath;
				else {
					path.setNextPath(thisPath);// NOSONAR
					path = thisPath;
				}

			}
			from = to;
		}
		return path.getTopPath();// NOSONAR
	}

	static Path buildPathByFromAndTo(EntityNet net, Class<?> from, Class<?> to) {
		Object[] r = getRelationShip(net, from, to);
		return new Path((String) r[0], r[1], (String[]) r[2]);
	}

	/** The the relationship between from and to */
	static Object[] getRelationShip(EntityNet net, Class<?> from, Class<?> to) {
		TableModel fromModel = net.getConfigModels().get(from);
		String fromTable = fromModel.getTableName();
		TableModel toModel = net.getConfigModels().get(to);
		String toTable = toModel.getTableName();
		Object[] result = null;
		// assume from is child, like from is RoleUser, to is UserPOJO
		for (FKeyModel fKeyModel : fromModel.getFkeyConstraints()) {
			String parentTableName = fKeyModel.getRefTableAndColumns()[0];
			if (toTable.equalsIgnoreCase(parentTableName)) {
				if (result != null)
					throw new EntityNetException(
							"Auto path can not determined, multiple relationships found between class " + from + " and "
									+ to);
				result = new Object[3];
				result[0] = "P-";
				result[1] = to;
				String[] refs = fKeyModel.getColumnNames().toArray(new String[fKeyModel.getColumnNames().size()]);
				result[2] = refs;
			}
		}
		// assume to is child, like from is UserPOJO, to is UserRole
		for (FKeyModel fKeyModel : toModel.getFkeyConstraints()) {
			String parentTableName = fKeyModel.getRefTableAndColumns()[0];
			if (fromTable.equalsIgnoreCase(parentTableName)) {
				if (result != null)
					throw new EntityNetException(
							"Auto path can not determined, multiple relationships found between class " + from + " and "
									+ to);
				result = new Object[3];
				result[0] = "C-";
				result[1] = to;
				String[] refs = fKeyModel.getColumnNames().toArray(new String[fKeyModel.getColumnNames().size()]);
				result[2] = refs;
			}
		}
		if (result != null)
			return result;
		throw new EntityNetException(
				"Auto path can not determined, no relationship found between class " + from + " and " + to);
	}

	/** Find the target class by given target table name or target class */
	static Class<?> findClassByTarget(EntityNet net, Object target) {
		if (target instanceof String) {
			String tbName = (String) target;
			for (Entry<Class<?>, TableModel> entry : net.getConfigModels().entrySet()) {
				TableModel mod = entry.getValue();
				if (mod != null && tbName.equalsIgnoreCase(mod.getTableName()))
					return entry.getKey();
			}
			throw new EntityNetException("Can not find target class for '" + target + "'");
		} else {
			if (!(target instanceof Class))
				throw new EntityNetException(
						"Target can only be table name string or entity class,  for '" + target + "'");
			return (Class<?>) target;
		}
	}

	/** Find the target class by given table name */
	static Class<?> findClassByTableName(Map<Class<?>, TableModel> models, String tableName) {
		if (models == null || models.isEmpty())
			return null;
		for (Entry<Class<?>, TableModel> entry : models.entrySet()) {
			if (entry.getValue() != null && tableName.equalsIgnoreCase(entry.getValue().getTableName()))
				return entry.getKey();
		}
		return null;
	}

	/** Search Node path from from to to */
	@SuppressWarnings("all")
	static Set<Class<?>> searchNodePath(Map<Class<?>, TableModel> models, Class<?> from, Class<?> to) {
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
					if (subSet.size() == i) {
						Class<?> last = getLastElement(subSet);
						for (Entry<Class<?>, TableModel> entry : models.entrySet()) {
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
					if (foundClass != null) {
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
		} while (i < 200);
		throw new EntityNetException("Not found availible auto path");
	}

	/** Get the last element from ordered Collection */
	static <E> E getLastElement(Collection<E> c) {
		E last = null;
		for (E e : c)
			last = e;
		return last;
	}
}
