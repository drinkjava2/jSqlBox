package com.github.drinkjava2.jdialects.id;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;

/**
 * java edition of Twitter <b>Snowflake</b>, a network service for generating
 * unique ID numbers at high scale with some simple guarantees.
 * 
 * https://github.com/twitter/snowflake
 * 
 * Usage: long id= new SnowflakeGenerator(3, 16).nextId(); <br/>
 * First parameter is datacenterId, 0~31 <br/>
 * Second parameter is workerId, 0~31
 * 
 * @author downgoon
 */
@SuppressWarnings("all")
public class SnowflakeGenerator implements IdGenerator {

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect, Type dataType) {
		return null;
	}

	@Override
	public GenerationType getGenerationType() { 
		return GenerationType.SNOWFLAKE;
	}

	@Override
	public String getIdGenName() { 
		return "SNOWFLAKE";
	}

	@Override
	public IdGenerator newCopy() {
		return new SnowflakeGenerator();
	}

	@Override
	public Boolean dependOnAutoIdGenerator() { 
		return false;
	}
	
}