package com.github.drinkjava2.beetlsqldemo;

import org.beetl.core.engine.GrammarCreator;
import org.beetl.core.statement.Expression;
import org.beetl.core.statement.FormatExpression;
import org.beetl.core.statement.PlaceholderST;

public class SQLGrammarCreator extends GrammarCreator {

	public PlaceholderST createTextOutputSt(Expression exp, FormatExpression format) {
		check("TextOutputSt");
		return new SQLPlaceholderST(exp, format, null);
	}

	public PlaceholderST createTextOutputSt2(Expression exp, FormatExpression format) {
		return new PlaceholderST(exp, format, null);
	}
}
