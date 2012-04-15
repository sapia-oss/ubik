package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
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
	
	@Test
	public void testSplitAsSets() {
	  Set<Integer> toSplit = new HashSet<Integer>();
		for(int i = 0; i < 100; i++) {
			toSplit.add(i);
		}
		List<Set<Integer>> sets = Collections2.splitAsSets(toSplit, 5);
		assertEquals(20, sets.size());
		for(Set<Integer> s : sets) {
			assertEquals(5, s.size());
		}
	}
	
	@Test
	public void testSplitAsSetsUneven() {
	  Set<Integer> toSplit = new HashSet<Integer>();
		for(int i = 0; i < 23; i++) {
			toSplit.add(i);
		}
		List<Set<Integer>> sets = Collections2.splitAsSets(toSplit, 5);
		assertEquals(5, sets.size());
		int count = 0;
		for(Set<Integer> s : sets) {
			if(count < 4) {
				assertEquals(5, s.size());				
			} else {
				assertEquals(3, s.size());
			}
			count++;
		}
	}	
	
	@Test
	public void testSplitAsLists() {
	  List<Integer> toSplit = new ArrayList<Integer>();
		for(int i = 0; i < 100; i++) {
			toSplit.add(i);
		}
		List<List<Integer>> lists = Collections2.splitAsLists(toSplit, 5);
		assertEquals(20, lists.size());
		for(List<Integer> lst : lists) {
			assertEquals(5, lst.size());
		}
	}
	
	@Test
	public void testSplitAsListsUneven() {
	  List<Integer> toSplit = new ArrayList<Integer>();
		for(int i = 0; i < 23; i++) {
			toSplit.add(i);
		}
		List<List<Integer>> lists = Collections2.splitAsLists(toSplit, 5);
		assertEquals(5, lists.size());
		int count = 0;
		for(List<Integer> s : lists) {
			if(count < 4) {
				assertEquals(5, s.size());				
			} else {
				assertEquals(3, s.size());
			}
			count++;
		}
	}	
	

}
