package org.sapia.ubik.rmi.server.stats;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StatisticKeyTest {
	
	private StatisticKey aa, ab, ba;

	@Before
	public void setUp() throws Exception {
		aa = new StatisticKey("a", "a");
		ab = new StatisticKey("a", "b");
		ba = new StatisticKey("b", "a");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEquals() {
		assertEquals(aa, aa);
	}
	
	@Test
	public void testNotEquals() {
		assertNotSame(aa, ab);
	}	

	@Test
	public void testCompareToBefore() {
		assertTrue(aa.compareTo(ab) < 0);
	}
	
	@Test
	public void testCompareToEqual() {
		assertEquals(0, aa.compareTo(aa));
	}	
	@Test
	public void testCompareToAfter() {
		assertTrue(ba.compareTo(aa) > 0);
	}	

}
