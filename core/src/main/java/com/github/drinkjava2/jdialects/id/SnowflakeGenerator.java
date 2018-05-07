package com.github.drinkjava2.jdialects.id;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;

/**
 * SnowflakeGenerator is a special generator, only mark this column is a
 * snowflake type column, but getNextID() method does not work, because
 * snowflake value should generated by outside program, it depends on real
 * machine setting
 */
@SuppressWarnings("all")
public class SnowflakeGenerator implements IdGenerator {
	public final static SnowflakeGenerator INSTANCE = new SnowflakeGenerator();

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect, Type dataType) {
		throw new DialectException("Snowflake type column value should generated by outside program.");
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
		return INSTANCE;
	}

	@Override
	public Boolean dependOnAutoIdGenerator() {
		return false;
	}

}