package com.github.drinkjava2.jdialects.annotation.jdia;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Mark a Long type column value build by using SnowFlake algorithm from twitter
 * <br/>
 * 
 * In jDialects SnowFlake algorithm source code originated from:
 * https://github.com/downgoon/snowflake
 * 
 * The SnowFlake algorithm follows below basic rule:<br/>
 * 1 bit const=0 <br/>
 * 41 bits Timestamp based on machine<br/>
 * 10 bits Confighured by user, used as machine ID, an example is assign 5 bits
 * for dataCenterID + 5 bits for workerID<br/>
 * 12 bits Sequence number<br/>
 * 
 * 
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Snowflake {
}
