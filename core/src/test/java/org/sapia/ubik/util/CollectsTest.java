package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class CollectsTest {

  @Test
  public void testIterableForEach() {
    final AtomicInteger counter = new AtomicInteger();
    Collects.forEach(Collects.arrayToList("1", "2", "3"), new Condition<String>() {
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
    Collects.forEach(new String[] { "1", "2", "3" }, new Condition<String>() {
      public boolean apply(String item) {
        counter.incrementAndGet();
        return true;
      }
    });
    assertEquals(3, counter.get());
  }

  @Test
  public void testFilterAsTest() {
    List<String> toFilter = Collects.arrayToList("1", "2", "3");
    Set<String> filtered = Collects.filterAsSet(toFilter, new Condition<String>() {
      @Override
      public boolean apply(String item) {
        return item.equals("1");
      }
    });

    assertEquals(1, filtered.size());
    assertTrue("Set does not contain expected string", filtered.contains("1"));
  }

  @Test
  public void testConvertAsList() {
    List<Integer> integers = Collects.convertAsList(Collects.arrayToList("1", "2", "3"), new Func<Integer, String>() {
      @Override
      public Integer call(String arg) {
        return Integer.parseInt(arg);
      }
    });
    assertEquals(3, integers.size());
  }

  @Test
  public void testConvertAsSet() {
    Set<Integer> integers = Collects.convertAsSet(Collects.arrayToList("1", "2", "3"), new Func<Integer, String>() {
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
    Integer[] integers = Collects.convertAsArray(Collects.arrayToList("1", "2", "3"), Integer.class, new Func<Integer, String>() {
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
    for (int i = 0; i < 100; i++) {
      toSplit.add(i);
    }
    List<Set<Integer>> sets = Collects.splitAsSets(toSplit, 5);
    assertEquals(20, sets.size());
    for (Set<Integer> s : sets) {
      assertEquals(5, s.size());
    }
  }

  @Test
  public void testSplitAsSetsUneven() {
    Set<Integer> toSplit = new HashSet<Integer>();
    for (int i = 0; i < 23; i++) {
      toSplit.add(i);
    }
    List<Set<Integer>> sets = Collects.splitAsSets(toSplit, 5);
    assertEquals(5, sets.size());
    int count = 0;
    for (Set<Integer> s : sets) {
      if (count < 4) {
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
    for (int i = 0; i < 100; i++) {
      toSplit.add(i);
    }
    List<List<Integer>> lists = Collects.splitAsLists(toSplit, 5);
    assertEquals(20, lists.size());
    for (List<Integer> lst : lists) {
      assertEquals(5, lst.size());
    }
  }

  @Test
  public void testSplitAsListsUneven() {
    List<Integer> toSplit = new ArrayList<Integer>();
    for (int i = 0; i < 23; i++) {
      toSplit.add(i);
    }
    List<List<Integer>> lists = Collects.splitAsLists(toSplit, 5);
    assertEquals(5, lists.size());
    int count = 0;
    for (List<Integer> s : lists) {
      if (count < 4) {
        assertEquals(5, s.size());
      } else {
        assertEquals(3, s.size());
      }
      count++;
    }
  }

  @Test
  public void testDivideAsSets() {
    Set<String> toSplit = Collects.arrayToSet("1", "2", "3", "4", "5", "6");
    List<Set<String>> splits = Collects.divideAsSets(toSplit, 3);
    assertEquals(3, splits.size());
    assertEquals(2, splits.get(0).size());
    assertEquals(2, splits.get(1).size());
    assertEquals(2, splits.get(2).size());

  }

  @Test
  public void testDivideAsSetsWithZeroDivisor() {
    Set<String> toSplit = Collects.arrayToSet("1", "2", "3", "4", "5", "6");
    List<Set<String>> splits = Collects.divideAsSets(toSplit, 0);
    assertEquals(1, splits.size());
    assertEquals(6, splits.get(0).size());
  }

  @Test
  public void testDivideAsSetsDivisorSameAsSize() {
    Set<String> toSplit = Collects.arrayToSet("1", "2", "3", "4", "5", "6");
    List<Set<String>> splits = Collects.divideAsSets(toSplit, 6);
    assertEquals(6, splits.size());
    assertEquals(1, splits.get(0).size());
  }

  @Test
  public void testDivideAsLists() {
    List<String> toSplit = Collects.arrayToList("1", "2", "3", "4", "5", "6");
    List<List<String>> splits = Collects.divideAsLists(toSplit, 3);
    assertEquals(3, splits.size());
    assertEquals(2, splits.get(0).size());
    assertEquals(2, splits.get(1).size());
    assertEquals(2, splits.get(1).size());
  }

  @Test
  public void testDivideAsListsWithZeroDivisor() {
    List<String> toSplit = Collects.arrayToList("1", "2", "3", "4", "5", "6");
    List<List<String>> splits = Collects.divideAsLists(toSplit, 0);
    assertEquals(1, splits.size());
    assertEquals(6, splits.get(0).size());
  }

  @Test
  public void testDivideAsListsDivisorSameAsSize() {
    List<String> toSplit = Collects.arrayToList("1", "2", "3", "4", "5", "6");
    List<List<String>> splits = Collects.divideAsLists(toSplit, 6);
    assertEquals(6, splits.size());
    assertEquals(1, splits.get(0).size());
  }

}
