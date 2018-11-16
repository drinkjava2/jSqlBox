package com.example.jsqlboxinspringboot.controller;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.*;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.nQueryForLongValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jsqlboxinspringboot.entity.Customer;
import com.example.jsqlboxinspringboot.services.Services;

@RestController
public class Controller {

	@Autowired
	Services services;

	@RequestMapping("/")
	public String home() {
		return toHtml("Now have " + eCountAll(Customer.class) + " records in database.");
	}

	@RequestMapping("/insert")
	public String insert() {
		services.insertOne();
		return toHtml("Now have " + new Customer().countAll() + " records in database.");
	}

	@RequestMapping("/tx")
	public String errorInsert() {
		try {
			services.errorInsert();
		} catch (Exception e) {
			System.out.println("Div 0 Exception found.");
		}
		return toHtml("Still have " + nQueryForLongValue("Select count(1) from Customer")
				+ " records in database, because transaction rolled back.");
	}

	@RequestMapping("/mix_insert")
	public String mybatis_insert() { 
		services.mixInsert();
		return toHtml("Now have " + new Customer().countAll() + " records in database.");
	}

	@RequestMapping("/mix_tx")
	public String mybatis_errorInsert() {
		try {
			services.mixErrorInsert();
		} catch (Exception e) {
			System.out.println("Div 0 Exception found.");
		}
		return toHtml(
				"Still have " + new Customer().countAll()
				+ " records in database, because transaction rolled back.");
	}

	private static String toHtml(String s) {
		return htmlTemplate.replaceFirst("THE_RESULT", s);
	}

	//@formatter:off
	static String htmlTemplate="<!DOCTYPE html>\n" + 
			"<html>\n" + 
			"<head>\n" + 
			"<title>Hello jSqlBox</title>\n" + 
			"</head>\n" + 
			"<body>\n" + 
			"<br/>\n" + 
			"THE_RESULT\n" + 
			"<br/>\n" + 
			"<br/>\n" + 
			"jSqlBox:<br/>\n" + 
			 "<a href=\"insert\">Insert by jSqlBox</a>  | <a href=\"tx\">Insert by jSqlBox but roll back</a>\n" + 
			"<br/>\n" + 
			"<br/>\n" + 
			"MyBatis:<br/>\n" + 
			"<a href=\"mix_insert\">Mix jSqlBox and Mybatis do insert</a>  | <a href=\"mix_tx\">Mix use jSqlBox and Mybatis do insert but roll back</a>\n" +
			"<br/>\n" + 
			"</body>\n" + 
			"</html>"  ;
}