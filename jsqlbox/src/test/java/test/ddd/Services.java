package test.ddd;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;

public class Services {
	public void receivePartsFromPO(PODetail podetal, Part part, Integer receiveQTY) {

		POReceiving poReceiving = new POReceiving();
		poReceiving.setReceiveQTY(receiveQTY);
		poReceiving.insert();

		part.setTotalCurrentStock(part.getTotalCurrentStock() + receiveQTY);

		podetal.setReceived(podetal.getReceived() + receiveQTY);
	}

	public static void main(String[] args) {
		SqlBoxContext.getDefaultSqlBoxContext().close();
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));

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
		Part part = Part.create("part1");
		Part part2 = Dao.load(Part.class, "part1");
		System.out.println(part2.getPartID());
		//PODetail poDetail = PODetail.create("po1", part, 5);

		// // do test
		// Services service = new Services();
		// service.receivePartsFromPO(poDetail, part, 1);

	}
}