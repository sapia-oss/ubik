package org.sapia.ubik.rmi.naming.remote;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class JndiSyncRequestTest {

  private Map<String, Integer> countsByNames;


  @Before
  public void setUp() throws Exception {
    countsByNames = new HashMap<String, Integer>();
    countsByNames.put("n0", 1);
    countsByNames.put("n1", 1);
    countsByNames.put("n2", 2);
    countsByNames.put("n3", 3);
  }

  @Test
  public void testAsMap() {
    Map<String, Integer> map = JndiSyncRequest.newInstance(countsByNames).asMap();
    assertEquals(new Integer(1), map.get("n0"));
    assertEquals(new Integer(1), map.get("n1"));
    assertEquals(new Integer(2), map.get("n2"));
    assertEquals(new Integer(3), map.get("n3"));
  }

  @Test
  public void testDiff() {
    HashMap<String, Integer> other = new HashMap<String, Integer>();
    other.put("n1", 2);
    other.put("n2", 1);
    other.put("n3", 3);
    other.put("n4", 4);

    Map<String, Integer> diff =  JndiSyncRequest.newInstance(countsByNames).diff(other);

    assertEquals(new Integer(1), diff.get("n0"));
    assertEquals(new Integer(-1), diff.get("n1"));
    assertEquals(new Integer(1), diff.get("n2"));
    assertEquals(new Integer(0), diff.get("n3"));
    assertEquals(new Integer(-4), diff.get("n4"));
  }

  @Test
  public void testNewInstance() {
    //fail("Not yet implemented");
  }

}
