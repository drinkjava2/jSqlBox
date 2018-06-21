package com.github.drinkjava2.beetlsqldemo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLResult;
import org.beetl.sql.core.SQLScript;
import org.beetl.sql.core.engine.SQLParameter;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * This is a demo shows how to use BeetlSql as a template engine in jSqlBox
 * 
 * @author Yong Zhu
 */
public class BeetlSqlTempalte implements SqlTemplateEngine {
	private SQLManager sm;

	public BeetlSqlTempalte(SQLManager sm) {
		this.sm = sm;
	}

	@Override
	public PreparedSQL render(String sqlId, Map<String, Object> paramMap, Object[] unbindParams) {
		return doRender(sm, sqlId, paramMap);
	}

	private static PreparedSQL doRender(SQLManager sm, String sqlId, Object paras) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("_root", paras);
		SQLScript script = sm.getScript(sqlId);
		SQLResult result = null;
		try {
			// run method in SQLScript is protected, have to make it accessible
			Method method = SQLScript.class.getDeclaredMethod("run", Map.class);
			ReflectionUtils.makeAccessible(method);
			result = (SQLResult) method.invoke(script, param);
		} catch (Exception e) {
			throw new SqlBoxException("Can not access method 'run' in class 'org.beetl.sql.core.SQLScript'");
		}
		PreparedSQL sp = new PreparedSQL();
		sp.setSql(result.jdbcSql);
		List<SQLParameter> sqlparam = result.jdbcPara;

		Object[] params = new Object[sqlparam.size()];
		for (int i = 0; i < sqlparam.size(); i++) {
			params[i] = sqlparam.get(i).value;
		}
		sp.setParams(params);
		return sp;
	}

}