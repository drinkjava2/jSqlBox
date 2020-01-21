package com.example.jsqlboxinspringboot.services;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jsqlboxinspringboot.entity.Customer;

@Service
public class Services {

	@Transactional
	public void insertOne() {
		new Customer().putField("name", "Custome A").insert();
	}

	@Transactional
	public void errorInsert() {
		new Customer().putField("name", "Another B").insert();
		System.out.println("Now have " + eCountAll(Customer.class) + " records in database, but will roll back to "
				+ (eCountAll(Customer.class) - 1) + " records because Div/0 exception will happen.");
		System.out.println(1 / 0); // div 0!, show transaction roll back
	}
}