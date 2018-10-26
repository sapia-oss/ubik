package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringsTest {

  @Test
  public void testIsBlank_false() {
    assertFalse(Strings.isBlank("test"));
  }

  @Test
  public void testIsBlank_with_null() {
    assertTrue(Strings.isBlank(null));
  }

  @Test
  public void testIsBlank_with_whitespace_string() {
    assertTrue(Strings.isBlank("        "));
  }

  @Test
  public void testIsBlank_with_empty_string() {
    assertTrue(Strings.isBlank(""));
  }

  @Test
  public void testIsNotBlank() {
    assertTrue(Strings.isNotBlank("test"));
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
    String[] split = Strings.split("/test/string", '/', true);
    assertEquals(2, split.length);
    assertEquals("test", split[0]);
    assertEquals("string", split[1]);
  }

  @Test
  public void testSplit_with_empty_parts() {
    String[] split = Strings.split("/test/string", '/', false);
    assertEquals(3, split.length);
    assertEquals("", split[0]);
    assertEquals("test", split[1]);
    assertEquals("string", split[2]);
  }
  
  @Test
  public void testSplit_ignore_chars() {
    String[] split = Strings.split("[test,string]", ',', false, new char[] {'[', ']'});
    assertEquals(2, split.length);
    assertEquals("test", split[0]);
    assertEquals("string", split[1]);
  }

  @Test
  public void testJoin() {
    assertEquals("a/b/c", Strings.join('/', "a", "b", "c"));
  }

  @Test
  public void testJoinAt() {
    assertEquals("a/b/c", Strings.joinAt('/', 0, 3, "a", "b", "c"));
    assertEquals("b/c", Strings.joinAt('/', 1, 3, "a", "b", "c"));
    assertEquals("a/b", Strings.joinAt('/', 0, 2, "a", "b", "c"));
  }
}
