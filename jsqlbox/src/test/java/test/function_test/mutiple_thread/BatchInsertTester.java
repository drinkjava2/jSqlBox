package test.function_test.mutiple_thread;

import java.util.ArrayList;

import com.github.drinkjava2.jbeanbox.BeanBox;

import test.config.PrepareTestContext;
import test.config.po.User;
import test.function_test.jdbc.BatchInsertTest;

/**
 * jUnit does not support multiple thread test, test this unit by hand
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class BatchInsertTester {

	public static class MultiThread implements Runnable {
		public void run() {
			BatchInsertTest t = BeanBox.getBean(BatchInsertTest.class);
			t.tx_BatchInsertDemo();
		}
	}

	public static class MultiThread2 implements Runnable {
		public void run() {
			ArrayList<User> l = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				User u = new User();
				u.box().buildRealColumns();
				l.add(u);
			}
			try {
				Thread.sleep(6000);// NOSONAR
			} catch (InterruptedException e) {
			}
			System.out.print("0");
		}
	}

	public static void main(String[] args) {
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();

		MultiThread m = new MultiThread();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();

		MultiThread2 m2 = new MultiThread2();
		for (int i = 0; i < 200; i++) {
			new Thread(m2).start();
		}
		System.out.println("done");
		// PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
		// do not close dataSource because threads are running
	}
}