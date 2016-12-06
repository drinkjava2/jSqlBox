package test.tinyjdbc;

import javax.sql.DataSource;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyDbMetaData;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyJdbc;

import test.config.JBeanBoxConfig.MySqlDataSourceBox;

/**
 * This is to test TinyJDBC get meta data
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyJdbcGetMetaData {

	public static void main(String[] args) {
		DataSource ds = BeanBox.getBean(MySqlDataSourceBox.class);
		TinyDbMetaData meta = TinyJdbc.getMetaData(ds);
		System.out.println(meta.getDebugInfo());
	}
}