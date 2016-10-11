package test.test2_mutiple_thread;

import com.github.drinkjava2.BeanBox;

import test.test1_basic_crud.Tester;

public class MultiTester {

	public static class MultiThread implements Runnable {
		public void run() {
			Tester tester = BeanBox.getBean(Tester.class);
			tester.tx_batchInsertDemo();
		}
	}

	public static void main(String[] args) {
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
		new Thread(m).start(); 
	}

}