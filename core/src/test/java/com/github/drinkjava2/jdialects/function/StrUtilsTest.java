/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.StrUtils;

/**
 * StrUtils Unit Test
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class StrUtilsTest {
	@Test
	public void testContainsWhitespace() throws Exception {
		assertFalse(StrUtils.containsWhitespace(null));
		assertFalse(StrUtils.containsWhitespace(""));
		assertFalse(StrUtils.containsWhitespace("a"));
		assertFalse(StrUtils.containsWhitespace("abc"));
		assertTrue(StrUtils.containsWhitespace(" "));
		assertTrue(StrUtils.containsWhitespace(" a"));
		assertTrue(StrUtils.containsWhitespace("abc "));
		assertTrue(StrUtils.containsWhitespace("a b"));
		assertTrue(StrUtils.containsWhitespace("a  b"));
	}

	@Test
	public void testTrimWhitespace() throws Exception {
		assertEquals(null, StrUtils.trimWhitespace(null));
		assertEquals("", StrUtils.trimWhitespace(""));
		assertEquals("", StrUtils.trimWhitespace(" "));
		assertEquals("", StrUtils.trimWhitespace("\t"));
		assertEquals("a", StrUtils.trimWhitespace(" a"));
		assertEquals("a", StrUtils.trimWhitespace("a "));
		assertEquals("a", StrUtils.trimWhitespace(" a "));
		assertEquals("a b", StrUtils.trimWhitespace(" a b "));
		assertEquals("a b  c", StrUtils.trimWhitespace(" a b  c "));
	}

	@Test
	public void testTrimAllWhitespace() throws Exception {
		assertEquals("", StrUtils.trimAllWhitespace(""));
		assertEquals("", StrUtils.trimAllWhitespace(" "));
		assertEquals("", StrUtils.trimAllWhitespace("\t"));
		assertEquals("a", StrUtils.trimAllWhitespace(" a"));
		assertEquals("a", StrUtils.trimAllWhitespace("a "));
		assertEquals("a", StrUtils.trimAllWhitespace(" a "));
		assertEquals("ab", StrUtils.trimAllWhitespace(" a b "));
		assertEquals("abc", StrUtils.trimAllWhitespace(" a b  c "));
	}

	@Test
	public void testTrimLeadingWhitespace() throws Exception {
		assertEquals(null, StrUtils.trimLeadingWhitespace(null));
		assertEquals("", StrUtils.trimLeadingWhitespace(""));
		assertEquals("", StrUtils.trimLeadingWhitespace(" "));
		assertEquals("", StrUtils.trimLeadingWhitespace("\t"));
		assertEquals("a", StrUtils.trimLeadingWhitespace(" a"));
		assertEquals("a ", StrUtils.trimLeadingWhitespace("a "));
		assertEquals("a ", StrUtils.trimLeadingWhitespace(" a "));
		assertEquals("a b ", StrUtils.trimLeadingWhitespace(" a b "));
		assertEquals("a b  c ", StrUtils.trimLeadingWhitespace(" a b  c "));
	}

	@Test
	public void testTrimTrailingWhitespace() throws Exception {
		assertEquals(null, StrUtils.trimTrailingWhitespace(null));
		assertEquals("", StrUtils.trimTrailingWhitespace(""));
		assertEquals("", StrUtils.trimTrailingWhitespace(" "));
		assertEquals("", StrUtils.trimTrailingWhitespace("\t"));
		assertEquals("a", StrUtils.trimTrailingWhitespace("a "));
		assertEquals(" a", StrUtils.trimTrailingWhitespace(" a"));
		assertEquals(" a", StrUtils.trimTrailingWhitespace(" a "));
		assertEquals(" a b", StrUtils.trimTrailingWhitespace(" a b "));
		assertEquals(" a b  c", StrUtils.trimTrailingWhitespace(" a b  c "));
	}

	@Test
	public void testTrimLeadingCharacter() throws Exception {
		assertEquals(null, StrUtils.trimLeadingCharacter(null, ' '));
		assertEquals("", StrUtils.trimLeadingCharacter("", ' '));
		assertEquals("", StrUtils.trimLeadingCharacter(" ", ' '));
		assertEquals("\t", StrUtils.trimLeadingCharacter("\t", ' '));
		assertEquals("a", StrUtils.trimLeadingCharacter(" a", ' '));
		assertEquals("a ", StrUtils.trimLeadingCharacter("a ", ' '));
		assertEquals("a ", StrUtils.trimLeadingCharacter(" a ", ' '));
		assertEquals("a b ", StrUtils.trimLeadingCharacter(" a b ", ' '));
		assertEquals("a b  c ", StrUtils.trimLeadingCharacter(" a b  c ", ' '));
	}

	@Test
	public void testTrimTrailingCharacter() throws Exception {
		assertEquals(null, StrUtils.trimTrailingCharacter(null, ' '));
		assertEquals("", StrUtils.trimTrailingCharacter("", ' '));
		assertEquals("", StrUtils.trimTrailingCharacter(" ", ' '));
		assertEquals("\t", StrUtils.trimTrailingCharacter("\t", ' '));
		assertEquals("a", StrUtils.trimTrailingCharacter("a ", ' '));
		assertEquals(" a", StrUtils.trimTrailingCharacter(" a", ' '));
		assertEquals(" a", StrUtils.trimTrailingCharacter(" a ", ' '));
		assertEquals(" a b", StrUtils.trimTrailingCharacter(" a b ", ' '));
		assertEquals(" a b  c", StrUtils.trimTrailingCharacter(" a b  c ", ' '));
	}

	@Test
	public void testReplace() throws Exception {
		String inString = "a6AazAaa77abaa";
		String oldPattern = "aa";
		String newPattern = "foo";

		// Simple replace
		String s = StrUtils.replace(inString, oldPattern, newPattern);
		assertTrue("Replace 1 worked", s.equals("a6AazAfoo77abfoo"));

		// Non match: no change
		s = StrUtils.replace(inString, "qwoeiruqopwieurpoqwieur", newPattern);
		assertTrue("Replace non matched is equal", s.equals(inString));

		// Null new pattern: should ignore
		s = StrUtils.replace(inString, oldPattern, null);
		assertTrue("Replace non matched is equal", s.equals(inString));

		// Null old pattern: should ignore
		s = StrUtils.replace(inString, null, newPattern);
		assertTrue("Replace non matched is equal", s.equals(inString));
	}

	@Test
	public void testStartsWithIgnoreCase() {
		Assert.assertTrue(StrUtils.startsWithIgnoreCase("A", "A"));
		Assert.assertTrue(StrUtils.startsWithIgnoreCase("A", "a"));
		Assert.assertTrue(StrUtils.startsWithIgnoreCase("a", "A"));
		Assert.assertTrue(StrUtils.startsWithIgnoreCase("AA", "A"));
		Assert.assertTrue(StrUtils.startsWithIgnoreCase("AB", "a"));
		Assert.assertTrue(StrUtils.startsWithIgnoreCase(" A", " "));
		Assert.assertTrue(StrUtils.startsWithIgnoreCase(" a", ""));
		Assert.assertFalse(StrUtils.startsWithIgnoreCase(null, "A"));
		Assert.assertFalse(StrUtils.startsWithIgnoreCase(null, null));
		Assert.assertFalse(StrUtils.startsWithIgnoreCase("a", null));
		Assert.assertFalse(StrUtils.startsWithIgnoreCase(" a", "A"));
		Assert.assertFalse(StrUtils.startsWithIgnoreCase(null, ""));
	}

	@Test
	public void testIndexOfIgnoreCase() {
		assertEquals(StrUtils.indexOfIgnoreCase("A", "A"), 0);
		assertEquals(StrUtils.indexOfIgnoreCase("a", "A"), 0);
		assertEquals(StrUtils.indexOfIgnoreCase("A", "a"), 0);
		assertEquals(StrUtils.indexOfIgnoreCase("a", "a"), 0);
		assertEquals(StrUtils.indexOfIgnoreCase("a", "ba"), -1);
		assertEquals(StrUtils.indexOfIgnoreCase("ba", "a"), 1);
		assertEquals(StrUtils.indexOfIgnoreCase("Royal Blue", " Royal Blue"), -1);
		assertEquals(StrUtils.indexOfIgnoreCase(" Royal Blue", "Royal Blue"), 1);
		assertEquals(StrUtils.indexOfIgnoreCase("Royal Blue", "royal"), 0);
		assertEquals(StrUtils.indexOfIgnoreCase("Royal Blue", "oyal"), 1);
		assertEquals(StrUtils.indexOfIgnoreCase("Royal Blue", "al"), 3);
		assertEquals(StrUtils.indexOfIgnoreCase("", "royal"), -1);
		assertEquals(StrUtils.indexOfIgnoreCase("Royal Blue", ""), 0);
		assertEquals(StrUtils.indexOfIgnoreCase("Royal Blue", "BLUE"), 6);
		assertEquals(StrUtils.indexOfIgnoreCase("Royal Blue", "BIGLONGSTRING"), -1);
		assertEquals(StrUtils.indexOfIgnoreCase("Royal Blue", "Royal Blue LONGSTRING"), -1);
	}

	@Test
	public void testSubStringBetween() {
		assertTrue(StrUtils.substringBetween("wx[b]yz", "[", "]").equals("b"));
		assertTrue(StrUtils.substringBetween(null, "", "") == null);
		assertTrue(StrUtils.substringBetween("", null, "") == null);
		assertTrue(StrUtils.substringBetween("", "", null) == null);
		assertTrue(StrUtils.substringBetween("", "", "").equals(""));
		assertTrue(StrUtils.substringBetween("", "", "]") == null);
		assertTrue(StrUtils.substringBetween("", "[", "]") == null);
		assertTrue(StrUtils.substringBetween("yabcz", "", "").equals(""));
		assertTrue(StrUtils.substringBetween("yabcz", "y", "z").equals("abc"));
		assertTrue(StrUtils.substringBetween("yabczyabcz", "y", "z").equals("abc"));
	}

}
