/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects.id;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * java edition of Twitter <b>Snowflake</b>, a network service for generating
 * unique ID numbers at high scale with some simple guarantees.
 * 
 * https://github.com/twitter/snowflake
 * 
 * Usage example: long id= new SnowflakeCreator(5L,5L, 18, 31).nextId(); <br/>
 * 
 * P1 is datacenterIdBits, 1~9 bits<br/>
 * P2 is workerIdBits, 1~9 bits<br/>
 * P3 is real datacenterId, 0 to 511 <br/>
 * P4 is real workerId, 0 to 511
 * 
 * Should:  P1 + P2 = 10 (2^10=1024)
 * 
 * Should:  P3 + P4 < 1024
 * 
 * 
 * @author downgoon
 * @author Yong Z.
 */
@SuppressWarnings("all")
public class SnowflakeCreator {

	/*
	 * bits allocations
	 */

	private long datacenterIdBits = 5L;
	private long workerIdBits = 5L;
	private final long datacenterId;
	private final long workerId;

	/*
	 * max values of datacenterId and WorkerId
	 */
	private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits); // 2^5-1
	private long maxWorkerId = -1L ^ (-1L << workerIdBits); // 2^5-1

	private final long unusedBits = 1L;
	/**
	 * 'time stamp' here is defined as the number of millisecond that have elapsed
	 * since the {@link #epoch} given by users on {@link SnowflakeGenerator}
	 * instance initialization
	 */
	private final long timestampBits = 41L;
	private final long sequenceBits = 12L;

	/*
	 * max values of timeStamp and sequence
	 */

	private final long maxSequence = -1L ^ (-1L << sequenceBits); // 2^12-1

	/**
	 * left shift bits of timeStamp, workerId and datacenterId
	 */
	private final long timestampShift = sequenceBits + datacenterIdBits + workerIdBits;
	private final long datacenterIdShift = sequenceBits + workerIdBits;
	private final long workerIdShift = sequenceBits;

	/*
	 * object status variables
	 */

	/**
	 * reference material of 'time stamp' is '2016-01-01'. its value can't be
	 * modified after initialization.
	 */
	private final long epoch = 1451606400000L;

	/**
	 * the unique and incrementing sequence number scoped in only one period/unit
	 * (here is ONE millisecond). its value will be increased by 1 in the same
	 * specified period and then reset to 0 for next period.
	 * <p>
	 * max: 2^12-1 range: [0,4095]
	 */
	private long sequence = 0L;

	/** the time stamp last snowflake ID generated */
	private long lastTimestamp = -1L;

	/**
	 * generate an unique and incrementing id
	 *
	 * @return id
	 */
	public synchronized long nextId() {
		long currTimestamp = timestampGen();

		if (currTimestamp < lastTimestamp) {
			throw new IllegalStateException(
					String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
							lastTimestamp - currTimestamp));
		}

		if (currTimestamp == lastTimestamp) {
			sequence = (sequence + 1) & maxSequence;
			if (sequence == 0) { // overflow: greater than max sequence
				currTimestamp = waitNextMillis(currTimestamp);
			}

		} else { // reset to 0 for next period/millisecond
			sequence = 0L;
		}

		// track and memo the time stamp last snowflake ID generated
		lastTimestamp = currTimestamp;

		return ((currTimestamp - epoch) << timestampShift) | //
				(datacenterId << datacenterIdShift) | //
				(workerId << workerIdShift) | // new line for nice looking
				sequence;
	}

	/**
	 * @param datacenterId
	 *            data center number the process running on, value range: [0,31]
	 * @param workerId
	 *            machine or process number, value range: [0,31]
	 */
	public SnowflakeCreator(long datacenterIdBits, long workerIdBits, long datacenterId, long workerId) {
		this.datacenterIdBits = datacenterIdBits;
		this.workerIdBits = workerIdBits;

		maxDatacenterId = -1L ^ (-1L << datacenterIdBits); // 2^5-1
		maxWorkerId = -1L ^ (-1L << workerIdBits); // 2^5-1

		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(
					String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
		}
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(
					String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
		}

		this.datacenterId = datacenterId;
		this.workerId = workerId;
	}

	/**
	 * track the amount of calling {@link #waitNextMillis(long)} method
	 */
	private final AtomicLong waitCount = new AtomicLong(0);

	/**
	 * @return the amount of calling {@link #waitNextMillis(long)} method
	 */
	public long getWaitCount() {
		return waitCount.get();
	}

	/**
	 * running loop blocking until next millisecond
	 * 
	 * @param currTimestamp
	 *            current time stamp
	 * @return current time stamp in millisecond
	 */
	protected long waitNextMillis(long currTimestamp) {
		waitCount.incrementAndGet();
		while (currTimestamp <= lastTimestamp) {
			currTimestamp = timestampGen();
		}
		return currTimestamp;
	}

	/**
	 * get current time stamp
	 * 
	 * @return current time stamp in millisecond
	 */
	protected long timestampGen() {
		return System.currentTimeMillis();
	}

	/**
	 * show settings of Snowflake
	 */
	@Override
	public String toString() {
		return "Snowflake Settings [timestampBits=" + timestampBits + ", datacenterIdBits=" + datacenterIdBits
				+ ", workerIdBits=" + workerIdBits + ", sequenceBits=" + sequenceBits + ", epoch=" + epoch
				+ ", datacenterId=" + datacenterId + ", workerId=" + workerId + "]";
	}

	public long getEpoch() {
		return this.epoch;
	}

	/**
	 * extract time stamp, datacenterId, workerId and sequence number information
	 * from the given id
	 * 
	 * @param id
	 *            a snowflake id generated by this object
	 * @return an array containing time stamp, datacenterId, workerId and sequence
	 *         number
	 */
	public long[] parseId(long id) {
		long[] arr = new long[5];
		arr[4] = ((id & diode(unusedBits, timestampBits)) >> timestampShift);
		arr[0] = arr[4] + epoch;
		arr[1] = (id & diode(unusedBits + timestampBits, datacenterIdBits)) >> datacenterIdShift;
		arr[2] = (id & diode(unusedBits + timestampBits + datacenterIdBits, workerIdBits)) >> workerIdShift;
		arr[3] = (id & diode(unusedBits + timestampBits + datacenterIdBits + workerIdBits, sequenceBits));
		return arr;
	}

	/**
	 * extract and display time stamp, datacenterId, workerId and sequence number
	 * information from the given id in humanization format
	 * 
	 * @param id
	 *            snowflake id in Long format
	 * @return snowflake id in String format
	 */
	public String formatId(long id) {
		long[] arr = parseId(id);
		String tmf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(arr[0]));
		return String.format("%s, #%d, @(%d,%d)", tmf, arr[3], arr[1], arr[2]);
	}

	/**
	 * a diode is a long value whose left and right margin are ZERO, while middle
	 * bits are ONE in binary string layout. it looks like a diode in shape.
	 * 
	 * @param offset
	 *            left margin position
	 * @param length
	 *            offset+length is right margin position
	 * @return a long value
	 */
	private long diode(long offset, long length) {
		int lb = (int) (64 - offset);
		int rb = (int) (64 - (offset + length));
		return (-1L << lb) ^ (-1L << rb);
	}

}