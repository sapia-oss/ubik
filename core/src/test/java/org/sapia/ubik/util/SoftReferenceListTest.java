package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SoftReferenceListTest {
  
  private SoftReferenceList<Integer> items;
  
  @Before
  public void setUp() {
    items = new SoftReferenceList<Integer>();
  }

  @Test
  public void testAdd() {
    Integer i = new Integer(1);
    items.add(i);
    assertTrue(items.contains(i));
    assertTrue(items.contains(new Integer(1)));
  }

  @Test
  public void testRemove() {
    Integer i = new Integer(1);
    items.add(i);
    items.remove(i);
    assertFalse(items.contains(i));
    assertFalse(items.contains(new Integer(1)));    
  }

  @Test
  public void testIterator() {
    for (int i = 0; i < 10; i++) {
      items.add(new Integer(i));
    }
    int count = 0;
    
    for (Integer item : items) {
      count++;
    }
    assertEquals(count, 10);
  }
  
  
  @Test
  public void testIteratorWithAllNulls() {
    for (int i = 0; i < 10; i++) {
      items.add(null);
    }
    int count = 0;
    
    for (Integer item : items) {
      count++;
    }
    assertEquals(0, count);
  }
  
  @Test
  public void testIteratorWithSomeNulls() {
    for (int i = 0; i < 10; i++) {
      if (i % 2 == 0) {
        items.add(null);        
      } else {
        items.add(new Integer(i));
      }

    }
    int count = 0;
    
    for (Integer item : items) {
      count++;
    }
    assertEquals(5, count);
  }  

}
