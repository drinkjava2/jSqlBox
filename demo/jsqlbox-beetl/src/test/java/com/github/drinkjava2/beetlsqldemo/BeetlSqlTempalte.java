package com.github.drinkjava2.beetlsqldemo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;

/**
 * This is a demo shows how to use BeetlSql as a template engine in jSqlBox
 * 
 * @author Yong Zhu
 */
public class BeetlSqlTempalte implements SqlTemplateEngine {
	private static final StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
	private static Configuration cfg;
	private static GroupTemplate gt;
	private static Template t;
	static {
		try {
			cfg = Configuration.defaultConfiguration();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cfg.setPlaceholderStart("#{");
		cfg.setPlaceholderEnd("}");
		cfg.setPlaceholderStart2("${");
		cfg.setPlaceholderEnd2("}");
		cfg.setEngine("com.github.drinkjava2.beetlsqldemo.SQLTemplateEngine");
		gt = new GroupTemplate(resourceLoader, cfg);
	}

	@Override
	public PreparedSQL render(String sqlOrId, Map<String, Object> paramMap, Object[] unbindParams) {
		t = gt.getTemplate(sqlOrId);
		for (Entry<String, Object> entry : paramMap.entrySet())
			t.binding(entry.getKey(), entry.getValue());
		PreparedSQL ps = new PreparedSQL();
		String result = t.render();
		ps.setSql(result);
		@SuppressWarnings("unchecked")
		List<Object> paras = (List<Object>) t.getCtx().getGlobal("__SqlParam");
		for (Object par : paras)
			ps.addParam(par);
		return ps;
	}

}