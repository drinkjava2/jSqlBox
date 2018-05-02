package com.github.drinkjava2.functionstest.jtransactions;

import java.sql.Connection;

import com.github.drinkjava2.config.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;

/**
 * A TinyTx configuration
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTxBox extends BeanBox {
	{
		this.setConstructor(TinyTx.class, BeanBox.getBean(DataSourceBox.class), Connection.TRANSACTION_READ_COMMITTED);
	}
}