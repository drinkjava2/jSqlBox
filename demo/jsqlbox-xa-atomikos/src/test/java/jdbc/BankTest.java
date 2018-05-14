package jdbc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:jdbc/config.xml")
public class BankTest {


	@Autowired
	@Qualifier("bank")
	Bank bank;

	@Test
	public void testWithdraw() throws SQLException, Exception{
		bank.checkTables();
        int account = 10;
		long balance = bank.getBalance ( account );
        System.out.println ( "Balance of account "+account+" is: " + balance );
        int amount = 100;
        System.out.println ( "Withdrawing "+amount+" of account "+account+"..." );

		bank.withdraw ( account , amount );
		long  balance2 = bank.getBalance ( account );
        System.out.println ( "New balance of account "+account+" is: " + balance );
        assertEquals(balance2,balance-amount );

	}
}
