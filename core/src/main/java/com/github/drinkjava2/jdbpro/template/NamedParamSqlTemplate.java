/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro.template;

import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdbpro.inline.PreparedSQL;

/**
 * NamedParamSqlTemplate is an implementation of SqlTemplateEngine. It allow use
 * :xxxx or #{xxxx} format parameters in SQL template
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class NamedParamSqlTemplate implements SqlTemplateEngine {
	/** A lazy initialization singleton pattern */
	private static class InnerNamedParamSqlTemplate {
		private InnerNamedParamSqlTemplate() {
		}

		private static final NamedParamSqlTemplate INSTANCE = new NamedParamSqlTemplate();
	}

	/** @return A singleton instance of BasicSqlTemplate */
	public static NamedParamSqlTemplate instance() {
		return InnerNamedParamSqlTemplate.INSTANCE;
	}

	private static boolean isParamChars(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '.';
	}

	private String toBasicSqlTemplate(String sql) {
		StringBuilder sb = new StringBuilder();
		int status = 0;// status 0:normal, 1:in parameter
		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			if (status == 0 && c != ':')
				sb.append(c);
			else if (status == 0 && c == ':') {
				sb.append("#{");
				status = 1;
			} else if (status == 1 && isParamChars(c))
				sb.append(c);
			else {
				sb.append("}").append(c);
				status = 0;
			}
		}
		if (status == 1)
			sb.append("}");
		return sb.toString();
	}

	public PreparedSQL render(String sqlTemplate, Map<String, Object> paramMap, Set<String> directReplaceNamesSet) {
		return BasicSqlTemplate.instance().render(toBasicSqlTemplate(sqlTemplate), paramMap, directReplaceNamesSet);
	}

}
