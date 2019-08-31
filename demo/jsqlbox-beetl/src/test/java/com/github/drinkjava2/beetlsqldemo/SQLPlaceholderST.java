package com.github.drinkjava2.beetlsqldemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.beetl.core.Context;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.statement.Expression;
import org.beetl.core.statement.FormatExpression;
import org.beetl.core.statement.GrammarToken;
import org.beetl.core.statement.PlaceholderST;

public class SQLPlaceholderST extends PlaceholderST {
	private static final long serialVersionUID = 1L;

	public SQLPlaceholderST(Expression exp, FormatExpression format, GrammarToken token) {
		super(exp, format, token);
	}

	@SuppressWarnings("all")
	@Override
	public final void execute(Context ctx) {
		try {
			Object value = expression.evaluate(ctx);
			if (format != null)
				value = format.evaluateValue(value, ctx);
			ctx.byteWriter.writeString("?");
			List list = (List) ctx.getGlobal("__SqlParam");
			if (list == null) {
				list = new ArrayList<Object>();
				ctx.set("__SqlParam", list);
			}
			list.add(value);
		} catch (IOException e) {
			BeetlException be = new BeetlException(BeetlException.CLIENT_IO_ERROR_ERROR, e.getMessage(), e);
			be.pushToken(this.token);
			throw be;
		}

	}

}