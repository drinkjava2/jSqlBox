package com.github.drinkjava2.jsqlbox.benchmark;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.drinkjava2.common.Systemout;

/**
 * A small tool to debug method execute time, see:
 * https://my.oschina.net/drinkjava2/blog/1622179
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class Debuger {// NOSONAR
	public static long multiple = 1;
	public static String lastMark = "start";
	private static long lastTime = System.nanoTime();
	private static final Map<String, Long> timeMap = new LinkedHashMap<String, Long>();
	private static final Map<String, Long> timeHappenCount = new LinkedHashMap<String, Long>();

	public static void set(int mark) {
		set("" + mark);
	};

	public static void set(String mark) {
		long thisTime = System.nanoTime();
		String key = lastMark + "->" + mark;
		Long lastSummary = timeMap.get(key);
		if (lastSummary == null)
			lastSummary = 0l;

		timeMap.put(key, System.nanoTime() - lastTime + lastSummary);
		Long lastCount = timeHappenCount.get(key);
		if (lastCount == null)
			lastCount = 0L;

		timeHappenCount.put(key, ++lastCount);
		lastTime = thisTime;
		lastMark = mark;
	};

	public static void print() {
		for (Entry<String, Long> entry : timeMap.entrySet()) {
			Systemout.println(// NOSONAR
					String.format(
							"%40s,    Total times(ns):%13s,      Repeat times:%8s,     Avg times(ns):%13s     times/s:%8s",
							entry.getKey(), //
							entry.getValue(), //
							multiple * timeHappenCount.get(entry.getKey()), //
							entry.getValue() / timeHappenCount.get(entry.getKey()), //
							Math.round(1000000000.0 * timeHappenCount.get(entry.getKey()) / entry.getValue() * multiple)

					));
		}
	}

}