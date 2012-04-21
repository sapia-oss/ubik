package org.sapia.ubik.log;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogNameFilterTest {
	
	private LogNameFilter filter;

	@Before
	public void setUp() throws Exception {
		filter = new LogNameFilter();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExclude() {
		filter.exclude("bar");
		assertTrue(filter.accepts("foo"));
		assertFalse(filter.accepts("bar"));
	}

	@Test
	public void testInclude() {
		filter.include("bar");
		assertFalse(filter.accepts("foo"));
		assertTrue(filter.accepts("bar"));
	}
	
	@Test
	public void testIncludeExclude() {
		filter.include("bar", "foo.bar").exclude("foo");
		assertFalse(filter.accepts("foo"));
		assertFalse(filter.accepts("foo.bar.sna.fu"));
		assertTrue(filter.accepts("bar"));
		assertFalse(filter.accepts("sna"));
	}

	@Test
	public void testIncludeAnyExcludeSpecific() {
		filter.include("*").exclude("foo");
		assertFalse(filter.accepts("foo"));
		assertFalse(filter.accepts("foo.bar.sna.fu"));
		assertTrue(filter.accepts("bar"));
		assertTrue(filter.accepts("sna"));
	}	

}
