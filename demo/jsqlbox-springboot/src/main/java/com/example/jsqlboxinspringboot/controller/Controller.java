package com.example.jsqlboxinspringboot.controller;
 
import static com.github.drinkjava2.jsqlbox.JSQLBOX.*;

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
	public String hello() {
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

	private static String toHtml(String s) {
		return htmlTemplate.replaceFirst("HTMLBODY", s);
	}

	//@formatter:off
	static String htmlTemplate="<!DOCTYPE html>\n" + 
			"<html>\n" + 
			"<head>\n" + 
			"<title>Hello jSqlBox</title>\n" + 
			"</head>\n" + 
			"<body>\n" + 
			"HTMLBODY\n" + 
			"<br/>\n" + 
			"<a href=\"insert\">Insert</a>  | <a href=\"tx\">Insert but roll back</a>\n" + 
			"</body>\n" + 
			"</html>"  ;
}