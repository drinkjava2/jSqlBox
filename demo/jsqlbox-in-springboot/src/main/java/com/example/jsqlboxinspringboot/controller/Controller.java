package com.example.jsqlboxinspringboot.controller;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.gctx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jsqlboxinspringboot.entity.Customer;
import com.example.jsqlboxinspringboot.services.Services;

@RestController
public class Controller {

	@Autowired
	Services services;

	@RequestMapping("/insert")
	public String hello() {
		services.insertOne();
		return "Have " + gctx().iQueryForLongValue("select count(*) from Customer") + " records in database.";
	}

	@RequestMapping("/tx")
	public String errorInsert() {
		try {
			services.errorInsert();
		} catch (Exception e) {
			System.out.println("Div 0 Exception found.");
		}
		return "Still have " + new Customer().countAll() + " records in database.";
	}
}