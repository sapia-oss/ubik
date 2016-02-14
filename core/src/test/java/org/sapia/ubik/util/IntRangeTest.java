package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class IntRangeTest {

  @Test
  public void testGetMin() {
    IntRange range = new IntRange(5, 10);
 
    assertEquals(new Integer(5), range.getMin());
  }

  @Test
  public void testGetMax() {
    IntRange range = new IntRange(5, 10);

    assertEquals(new Integer(10), range.getMax());
  }

  @Test
  public void testWithMin() {
    IntRange range = new IntRange(0, 10);

    assertEquals(new Integer(5), range.withMin(5).getMin());
  }

  @Test
  public void testWithMax() {
    IntRange range = new IntRange(0, 5);

    assertEquals(new Integer(10), range.withMax(10).getMax());
  }

  @Test
  public void testIterator() {
    IntRange range = new IntRange(0, 5);

    int counter = 0;
    
    for (Integer i : range) {
      assertEquals(new Integer(counter), i);
      counter++;
    }
    
    assertEquals(6, counter);
  }

  @Test
  public void testTransform() {
    List<String> values = new IntRange().withMax(5).transform(new Func<String, Integer>() {
      @Override
      public String call(Integer i) {
        return i.toString();
      }
    });
    
    for (int i = 0; i < 6; i++) {
      assertEquals(Integer.toString(i), values.get(i));
    }
  }

}
