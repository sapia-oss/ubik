package org.sapia.ubik.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringsTest {

  @Test
  public void testIsNullBlank() {
    assertTrue(Strings.isBlank(null));
  }

  @Test
  public void testIsWhiteSpaceStringBlank() {
    assertTrue(Strings.isBlank("        "));
  }

  @Test
  public void testIsEmptyStringBlank() {
    assertTrue(Strings.isBlank("        "));
  }

}
