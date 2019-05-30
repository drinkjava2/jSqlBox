/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox.function.gtx;

import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardTable;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

/**
 * Atomikos Transaction test, H2 + jBeanBox + jSqlBox + Spring XA + Atomikos
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class BankAccount extends ActiveRecord<BankAccount> {

	@ShardDatabase({ "MOD", "3" })
	@ShardTable({ "MOD", "3" })
	@Id
	private Long bankId;
	private Long balance;

	public BankAccount() {
	}

	public BankAccount(Long id) {
		this.bankId = id;
	}

	public Long getBankId() {
		return bankId;
	}

	public void setBankId(Long bankId) {
		this.bankId = bankId;
	}

	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}

}