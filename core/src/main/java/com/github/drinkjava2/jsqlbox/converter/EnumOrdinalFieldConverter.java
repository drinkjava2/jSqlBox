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
package com.github.drinkjava2.jsqlbox.converter;

import java.lang.reflect.Method;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * TailType has a tails() method return a map instance stored tail values
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class EnumOrdinalFieldConverter extends BaseFieldConverter {

	@Override
	public Object entityFieldToDbValue(ColumnModel col, Object entity) {
		Object value = SqlBoxContextUtils.doReadFromFieldOrTail(col, entity);
		return ((Enum<?>) value).ordinal();
	}

	@Override
	public void writeDbValueToEntityField(Object entityBean, ColumnModel col, Object value) {
		try {
			Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(entityBean.getClass(), col.getEntityField());
			Method method = writeMethod.getParameterTypes()[0].getMethod("values");
			Object[] enu = (Object[]) method.invoke(null);
			writeMethod.invoke(entityBean, enu[(Integer) value]);
		} catch (Exception e) {
			throw new SqlBoxException("Field '" + col.getEntityField() + "' can not write with value '" + value + "'",
					e);
		}
	}
}