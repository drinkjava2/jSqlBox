

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.CtxBox;

/**
 * This file should automatically created by a code generator tool
 * 
 */
public class SqlBoxConfig {
	public static SqlBoxContext getSqlBoxContext() {
		return BeanBox.getBean(CtxBox.class);
	}
}