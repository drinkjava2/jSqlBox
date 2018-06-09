package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdbpro.IocTool;

/**
 * JBeanBoxIocTool is the implements of IocTool, use jBeanBox.
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class JBeanBoxIocTool implements IocTool {
	public static final JBeanBoxIocTool instance = new JBeanBoxIocTool();

	@Override
	public <T> T getBean(Class<?> configClass) {
		return BeanBox.getBean(configClass);
	}
}