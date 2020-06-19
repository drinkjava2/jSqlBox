package com.github.drinkjava2.jsqlbox.issues.IYDU7_orm_demo;

import java.util.List;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

public class Ademo extends ActiveRecord<Ademo> {
	@Id
	String aid;

	String bid;
	
	Bdemo bdemo;

	List<Cdemo> cdemoList;

	String atext;

	public String getAid() {
		return aid;
	}

	public void setAid(String aid) {
		this.aid = aid;
	}

	public String getBid() {
		return bid;
	}

	public void setBid(String bid) {
		this.bid = bid;
	}

	public Bdemo getBdemo() {
		return bdemo;
	}

	public void setBdemo(Bdemo bdemo) {
		this.bdemo = bdemo;
	}

	public List<Cdemo> getCdemoList() {
		return cdemoList;
	}

	public void setCdemoList(List<Cdemo> cdemoList) {
		this.cdemoList = cdemoList;
	}

	public String getAtext() {
		return atext;
	}

	public void setAtext(String atext) {
		this.atext = atext;
	}

}