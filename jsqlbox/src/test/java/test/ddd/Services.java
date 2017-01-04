package test.ddd;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;
import test.config.JBeanBoxConfig.TxInterceptorBox;

public class Services {

	public void tx_receivePartsFromPO(PODetail poDetail) {
		PODetail.receivePartsFromPO(poDetail, "part1", 1);
	}

	public static void main(String[] args) {
		SqlBoxContext.getDefaultSqlBoxContext().close();
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));
		BeanBox.defaultContext.setAOPAround("test.\\w*.\\w*", "tx_\\w*", new TxInterceptorBox(), "invoke");
		SqlBoxContext.getDefaultSqlBoxContext().setShowSql(true);

		// drop and recreate tables;
		Dao.executeQuiet("drop table part");
		Dao.execute(Part.CREATE_SQL);
		Dao.executeQuiet("drop table podetail");
		Dao.execute(PODetail.CREATE_SQL);
		Dao.executeQuiet("drop table poreceiving");
		Dao.execute(POReceiving.CREATE_SQL);
		Dao.executeQuiet("drop table logpart");
		Dao.execute(LogPart.CREATE_SQL);
		Dao.refreshMetaData();

		// fill test data
		Part part = Part.insert("part1", 20);
		PODetail poDetail = PODetail.insert("po1", part.getPartID(), 5);

		// do test
		Services service = new Services();
		service.tx_receivePartsFromPO(poDetail);
	}
}