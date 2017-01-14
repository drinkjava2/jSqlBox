package test.mutiple_thread;

import java.util.ArrayList;

import com.github.drinkjava2.BeanBox;

import test.config.TestPrepare;
import test.jdbc.BatchInsertTest;
import test.po.User;

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

	public static void main(String[] args) {// jUnit cann't do multiple thread test
		TestPrepare.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		try {
			Thread.sleep(5000);// NOSONAR
		} catch (InterruptedException e) { 
			e.printStackTrace();
		}
		// MultiThread m = new MultiThread();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();
		// new Thread(m).start();

		MultiThread2 m2 = new MultiThread2();
		for (int i = 0; i < 200; i++) {
			new Thread(m2).start();
		}
		System.out.println("done");
		TestPrepare.closeDatasource_closeDefaultSqlBoxConetxt();
	}

}