package com.example.jsqlboxinspringboot.services;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.entityCountAll;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jsqlboxinspringboot.entity.Customer;
import com.example.jsqlboxinspringboot.entity.CustomerMapper;

@Service
public class Services {
	@Autowired
	private CustomerMapper customerMapper;

	@Transactional
	public void insertOne() {
		new Customer().put("name", "Custome A").insert();
	}

	@Transactional
	public void errorInsert() {
		new Customer().put("name", "Another B").insert();
		System.out.println("Now have " + entityCountAll(Customer.class) + " records in database, but will roll back to "
				+ (entityCountAll(Customer.class) - 1) + " records because Div/0 exception will happen.");
		System.out.println(1 / 0); // div 0!, show transaction roll back
	}

	@Transactional
	public void mixInsert() {
		new Customer().put("name", "Custome A").insert();
		customerMapper.insert(UUID.randomUUID().toString(), "Cust");
	}

	@Transactional
	public void mixErrorInsert() {
		new Customer().put("name", "Another B").insert();
		customerMapper.insert(UUID.randomUUID().toString(), "Cust");
		List<Customer> customers = customerMapper.findAllCustomers();
		System.out.println("Now have " + customers.size() + " records in database, but will roll back to "
				+ (customers.size() - 2) + " records because Div/0 exception will happen.");
		System.out.println(1 / 0); // div 0!, show transaction roll back
	}
}