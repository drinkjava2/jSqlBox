package com.github.drinkjava2.jsqlbox.issues.IYDU7_orm_demo;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

public class Bdemo extends ActiveRecord<Bdemo> {
	@Id
	String bid;
	String btext;

	public String getBid() {
		return bid;
	}

	public void setBid(String bid) {
		this.bid = bid;
	}

	public String getBtext() {
		return btext;
	}

	public void setBtext(String btext) {
		this.btext = btext;
	}

}