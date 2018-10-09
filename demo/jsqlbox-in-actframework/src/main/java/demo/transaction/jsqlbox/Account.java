package demo.transaction.jsqlbox;

import org.osgl.util.E;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;

/**
 * The account with a certain amount of money
 * 
 * @author Yong Zhu
 * @since 2.0.2
 */
public class Account implements ActiveEntity<Account> {
	@Id
	String id;

	Integer amount;

	public Account() {
	}

	public Account(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		E.illegalArgumentIf(amount < 0);
		this.amount = amount;
	}

	public void deposit(Integer amount) {
		E.illegalArgumentIf(amount < 0);
		this.amount += amount;
	}

	public void credit(Integer amount) {
		E.illegalArgumentIf(amount < 0);
		if (this.amount < amount) {
			throw new RuntimeException("Cannot credit on account[%s]: balance not enough");// NOSONAR
		}
		this.amount -= amount;
	}

	@MyTX
	public void transfer(Integer amount, String fromId, String toId) {
		Account to = new Account(toId).load();
		to.deposit(amount);
		to.update();
		Account from = new Account(fromId).load();
		from.credit(amount);
		from.update();
	}
}
