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
  
  @Test
  public void testSplit() {
    String[] split = Strings.split("test | string |  ", '|', false);
    assertEquals("test ", split[0]);
    assertEquals(" string ", split[1]);
    assertEquals("  ", split[2]);
  }

  @Test
  public void testSplit_ignore_whitespace() {
    String[] split = Strings.split("test | string |  ", '|', true);
    assertEquals(2, split.length);
    assertEquals("test", split[0]);
    assertEquals("string", split[1]);
  }
  
  @Test
  public void testSplit_ignore_chars() {
    String[] split = Strings.split("[test,string]", ',', false, new char[] {'[', ']'});
    assertEquals(2, split.length);
    assertEquals("test", split[0]);
    assertEquals("string", split[1]);
  }
}
