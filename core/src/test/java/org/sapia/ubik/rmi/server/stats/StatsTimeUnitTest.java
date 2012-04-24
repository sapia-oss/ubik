package org.sapia.ubik.rmi.server.stats;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StatsTimeUnitTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFromJdkNanos() {
		assertEquals(StatsTimeUnit.NANOSECONDS, StatsTimeUnit.fromJdkTimeUnit(TimeUnit.NANOSECONDS));
	}
	
	@Test
	public void testFromJdkMillis() {
		assertEquals(StatsTimeUnit.MILLISECONDS, StatsTimeUnit.fromJdkTimeUnit(TimeUnit.MILLISECONDS));
	}
	
	@Test
	public void testFromJdkSeconds() {
		assertEquals(StatsTimeUnit.SECONDS, StatsTimeUnit.fromJdkTimeUnit(TimeUnit.SECONDS));
	}
	
	@Test
	public void testFromJdkMinutes() {
		assertEquals(StatsTimeUnit.MINUTES, StatsTimeUnit.fromJdkTimeUnit(TimeUnit.MINUTES));
	}
	
	@Test
	public void testFromJdkHours() {
		assertEquals(StatsTimeUnit.HOURS, StatsTimeUnit.fromJdkTimeUnit(TimeUnit.HOURS));
	}
	
	@Test
	public void testFromJdkDays() {
		assertEquals(StatsTimeUnit.DAYS, StatsTimeUnit.fromJdkTimeUnit(TimeUnit.DAYS));
	}		

	@Test
	public void testConvertToNanos() {
		assertEquals(1000000d, StatsTimeUnit.NANOSECONDS.convertFrom(1, StatsTimeUnit.MILLISECONDS), 0);
		assertEquals(1000000d * 1000, StatsTimeUnit.NANOSECONDS.convertFrom(1, StatsTimeUnit.SECONDS), 0);		
		assertEquals(1000000d * 1000 * 60, StatsTimeUnit.NANOSECONDS.convertFrom(1, StatsTimeUnit.MINUTES), 0);
		assertEquals(1000000d * 1000 * 60 * 60, StatsTimeUnit.NANOSECONDS.convertFrom(1, StatsTimeUnit.HOURS), 0);
		assertEquals(1000000d * 1000 * 60 * 60 * 24, StatsTimeUnit.NANOSECONDS.convertFrom(1, StatsTimeUnit.DAYS), 0);
	}
	
	@Test
	public void testConvertToMillis() {
		assertEquals(1, StatsTimeUnit.MILLISECONDS.convertFrom(1000000, StatsTimeUnit.NANOSECONDS), 0);
		assertEquals(0.000001d, StatsTimeUnit.MILLISECONDS.convertFrom(1, StatsTimeUnit.NANOSECONDS), 0);
		assertEquals(1000, StatsTimeUnit.MILLISECONDS.convertFrom(1, StatsTimeUnit.SECONDS), 0);		
		assertEquals(1000d * 60, StatsTimeUnit.MILLISECONDS.convertFrom(1, StatsTimeUnit.MINUTES), 0);
		assertEquals(1000d * 60 * 60, StatsTimeUnit.MILLISECONDS.convertFrom(1, StatsTimeUnit.HOURS), 0);
		assertEquals(1000d * 60 * 60 * 24, StatsTimeUnit.MILLISECONDS.convertFrom(1, StatsTimeUnit.DAYS), 0);
	}
	
	@Test
	public void testConvertToSeconds() {
		assertEquals(0.001, StatsTimeUnit.SECONDS.convertFrom(1000000, StatsTimeUnit.NANOSECONDS), 0);
		assertEquals(0.001, StatsTimeUnit.SECONDS.convertFrom(1, StatsTimeUnit.MILLISECONDS), 0);
		assertEquals(60, StatsTimeUnit.SECONDS.convertFrom(1, StatsTimeUnit.MINUTES), 0);
		assertEquals(60 * 60, StatsTimeUnit.SECONDS.convertFrom(1, StatsTimeUnit.HOURS), 0);
		assertEquals(60 * 60 * 24, StatsTimeUnit.SECONDS.convertFrom(1, StatsTimeUnit.DAYS), 0);
	}
	
	@Test
	public void testConvertToMinutes() {
		assertEquals(0.001, StatsTimeUnit.MINUTES.convertFrom(1000000d * 60, StatsTimeUnit.NANOSECONDS), 0);
		assertEquals(1d/60d, StatsTimeUnit.MINUTES.convertFrom(1000, StatsTimeUnit.MILLISECONDS), 0);
		assertEquals(0.5, StatsTimeUnit.MINUTES.convertFrom(30, StatsTimeUnit.SECONDS), 0);
		assertEquals(60, StatsTimeUnit.MINUTES.convertFrom(1, StatsTimeUnit.HOURS), 0);
		assertEquals(60 * 24, StatsTimeUnit.MINUTES.convertFrom(1, StatsTimeUnit.DAYS), 0);
	}			
	
	@Test
	public void testConvertToHours() {
		assertEquals(0.001, StatsTimeUnit.HOURS.convertFrom(1000000d * 60 * 60, StatsTimeUnit.NANOSECONDS), 0);
		assertEquals(1d/60d, StatsTimeUnit.HOURS.convertFrom(1000 * 60, StatsTimeUnit.MILLISECONDS), 0);
		assertEquals(1d/60d, StatsTimeUnit.HOURS.convertFrom(60, StatsTimeUnit.SECONDS), 0);
		assertEquals(0.5, StatsTimeUnit.HOURS.convertFrom(30, StatsTimeUnit.MINUTES), 0);
		assertEquals(12, StatsTimeUnit.HOURS.convertFrom(0.5, StatsTimeUnit.DAYS), 0);
	}
	
	@Test
	public void testConvertToDays() {
		assertEquals(1d/24d, StatsTimeUnit.DAYS.convertFrom(60 * 60, StatsTimeUnit.SECONDS), 0);
		assertEquals(1d/24d, StatsTimeUnit.DAYS.convertFrom(60, StatsTimeUnit.MINUTES), 0);
		assertEquals(0.5, StatsTimeUnit.DAYS.convertFrom(12, StatsTimeUnit.HOURS), 0);
	}	

}
