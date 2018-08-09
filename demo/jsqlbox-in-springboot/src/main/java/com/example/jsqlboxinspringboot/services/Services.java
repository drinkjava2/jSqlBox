package com.example.jsqlboxinspringboot.services;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.gctx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jsqlboxinspringboot.entity.Customer;

@Service // Or @Component
public class Services {

	@Transactional
	public void insertOne() {
		new Customer().put("name", "Custome A").insert();
	}

	@Transactional
	public void errorInsert() {
		new Customer().put("name", "Another B").insert();
		new Customer().put("name", "Another C").insert();
		System.out.println(
				"Current have " + gctx().entityCountAll(Customer.class) + " records in database, but will roll back to "
						+ (gctx().entityCountAll(Customer.class) - 2) + " because Div/0");
		System.out.println(1 / 0); // div 0!, force transaction roll back
	}
}