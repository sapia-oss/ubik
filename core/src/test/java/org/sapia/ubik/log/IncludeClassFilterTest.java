package org.sapia.ubik.log;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class IncludeClassFilterTest {

  private IncludeClassFilter filter;

  @Before
  public void setUp() {
    filter = IncludeClassFilter.newInstance(String.class);
  }

  @Test
  public void testAccepts() {
    filter.addClass(Date.class);
    assertTrue(filter.accepts(String.class.getName()));
    assertTrue(filter.accepts(Date.class.getName()));
    assertFalse(filter.accepts(Integer.class.getName()));
  }

}
