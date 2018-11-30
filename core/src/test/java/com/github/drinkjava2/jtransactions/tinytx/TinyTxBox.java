package com.github.drinkjava2.jtransactions.tinytx;

import java.sql.Connection;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;

/**
 * A TinyTx configuration
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTxBox extends BeanBox {
	{
		this.injectConstruct(TinyTx.class, BeanBox.getBean(DataSourceBox.class), Connection.TRANSACTION_READ_COMMITTED);
	}
}