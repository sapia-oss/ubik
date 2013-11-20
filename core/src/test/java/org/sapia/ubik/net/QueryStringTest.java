package org.sapia.ubik.net;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Yanick Duchesne
 */
public class QueryStringTest {

  @Test
  public void testParse() throws Exception {
    String s = "/some/path?name1=value1&name2=value2";
    QueryString qs = QueryString.parse(s);
    assertEquals("/some/path", qs.getPath());
    assertEquals("value1", qs.getParameter("name1"));
    assertEquals("value2", qs.getParameter("name2"));
  }

  @Test
  public void testToString() throws Exception {
    String s = "/some/path?name1=value1&name2=value2";
    QueryString qs = QueryString.parse(s);
    qs = QueryString.parse(qs.toString());
    assertEquals("/some/path", qs.getPath());
    assertEquals("value1", qs.getParameter("name1"));
    assertEquals("value2", qs.getParameter("name2"));
  }

  @Test
  public void testInstantiate() throws Exception {
    QueryString qs = new QueryString("/some/path");
    qs.addParameter("name1", "value1");
    qs.addParameter("name2", "value2");
    assertEquals("/some/path", qs.getPath());
    assertEquals("value1", qs.getParameter("name1"));
    assertEquals("value2", qs.getParameter("name2"));
  }
}
