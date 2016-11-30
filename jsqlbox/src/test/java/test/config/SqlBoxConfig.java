package test.config;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.CtxBox;

/**
 * This is global configuration file for jSqlBox, it's necessary
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlBoxConfig {
	public static SqlBoxContext getSqlBoxContext() {
		return BeanBox.getBean(CtxBox.class);
	}
}