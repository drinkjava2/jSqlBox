package test.mutiple_thread;

import com.github.drinkjava2.BeanBox;

import test.config.InitializeDatabase;
import test.jdbc.BatchInsertTest;

public class BatchInsertTester {

	public static class MultiThread implements Runnable {
		public void run() {
			BatchInsertTest t = BeanBox.getBean(BatchInsertTest.class);
			t.tx_BatchInsertDemo();
		}
	}

	public static void main(String[] args) {// jUnit cann't do multiple thread test
		InitializeDatabase.dropAndRecreateTables();
		MultiThread m = new MultiThread();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
		new Thread(m).start();
	}

}