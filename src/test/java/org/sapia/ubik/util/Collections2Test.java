package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class Collections2Test {

	@Test
	public void testIterableForEach() {
		final AtomicInteger counter = new AtomicInteger();
		Collections2.forEach(Collections2.arrayToList("1", "2", "3"), new Condition<String>() {
			public boolean apply(String item) {
				counter.incrementAndGet();
				return true;
			}
		});
		
		assertEquals(3, counter.get());
	}

	@Test
	public void testArrayForEach() {
		final AtomicInteger counter = new AtomicInteger();
		Collections2.forEach(new String[]{"1", "2", "3"}, new Condition<String>() {
			public boolean apply(String item) {
				counter.incrementAndGet();
				return true;
			}
		});
		assertEquals(3, counter.get());
	}

	@Test
	public void testConvertAsList() {
		List<Integer> integers = Collections2.convertAsList(Collections2.arrayToList("1", "2", "3"), new Function<Integer, String>() {
			@Override
			public Integer call(String arg) {
			  return Integer.parseInt(arg);
			}
		});
		assertEquals(3, integers.size());
	}

	@Test
	public void testConvertAsSet() {
		Set<Integer> integers = Collections2.convertAsSet(Collections2.arrayToList("1", "2", "3"), new Function<Integer, String>() {
			@Override
			public Integer call(String arg) {
			  return Integer.parseInt(arg);
			}
		});		
		assertEquals(3, integers.size());		
		assertTrue(integers.contains(new Integer(1)));
		assertTrue(integers.contains(new Integer(2)));
		assertTrue(integers.contains(new Integer(3)));
	}

	@Test
	public void testConvertAsArray() {
		Integer[] integers = Collections2.convertAsArray(Collections2.arrayToList("1", "2", "3"), Integer.class, new Function<Integer, String>() {
			@Override
			public Integer call(String arg) {
			  return Integer.parseInt(arg);
			}
		});
		assertEquals(3, integers.length);
	}

}
