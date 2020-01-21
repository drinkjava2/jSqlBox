/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jbeanbox;

/**
 * ValueTranslator translate a String to a real value, user can write other
 * ValueTranslator to replace the DefaultConstTranslator
 * 
 * @author Yong Zhu
 * @since 2.4.7
 *
 */
public interface ValueTranslator {

	public Object translate(String s, Class<?> type);

	public static class DefaultValueTranslator implements ValueTranslator {

		@Override
		public Object translate(String s, Class<?> type) {// NOSONAR
			if (type == null)
				BeanBoxException.throwEX("ParamTranslator can not translate to 'null' type");
			if (Integer.class.equals(type) || int.class.equals(type))
				return Integer.parseInt(s);
			if (Long.class.equals(type) || long.class.equals(type))
				return Long.parseLong(s);
			if (Character.class.equals(type) || char.class.equals(type))
				return s.charAt(0);
			if (Byte.class.equals(type) || byte.class.equals(type))
				return Byte.parseByte(s);
			if (Boolean.class.equals(type) || boolean.class.equals(type))
				return Boolean.parseBoolean(s);
			if (Double.class.equals(type) || double.class.equals(type))
				return Double.parseDouble(s);
			if (Float.class.equals(type) || float.class.equals(type))
				return Float.parseFloat(s);
			if (Short.class.equals(type) || short.class.equals(type))
				return Short.parseShort(s);
			if (String.class.equals(type))
				return s;
			return BeanBoxException.throwEX("Unsupported type:" + type);
		}
	}

}