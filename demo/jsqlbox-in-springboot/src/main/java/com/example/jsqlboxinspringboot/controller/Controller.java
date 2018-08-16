package com.example.jsqlboxinspringboot.controller;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.entityCountAll;
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

	@RequestMapping("/insert")
	public String hello() {
		services.insertOne();
		return "Have " + nQueryForLongValue("select count(1) from Customer") + " records in database.";
	}

	@RequestMapping("/tx")
	public String errorInsert() {
		try {
			services.errorInsert();
		} catch (Exception e) {
			System.out.println("Div 0 Exception found.");
		}
		return "Still have " + entityCountAll(Customer.class) + " records in database, because transaction rolled back.";
	}
}