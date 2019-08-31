package com.github.drinkjava2.beetlsqldemo;

import org.beetl.core.GroupTemplate;
import org.beetl.core.engine.DefaultTemplateEngine;
import org.beetl.core.engine.GrammarCreator;

public class SQLTemplateEngine extends DefaultTemplateEngine {

	@Override
	protected GrammarCreator getGrammerCreator(GroupTemplate gt) {
		return new SQLGrammarCreator();
	}

}
