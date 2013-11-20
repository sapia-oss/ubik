package org.sapia.ubik.mcast.control;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.mcast.control.Purgatory.DownNode;
import org.sapia.ubik.util.Collections2;

public class PurgatoryTest {

  private Purgatory purgatory;

  @Before
  public void setUp() {
    purgatory = new Purgatory();
  }

  @Test
  public void testRemove() {
    purgatory.add("test");
    purgatory.remove("test");

    assertFalse("Node should have been removed", purgatory.contains("test"));
  }

  @Test
  public void testAdd() {
    purgatory.add("test");

    assertTrue("Node should have been added", purgatory.contains("test"));
  }

  @Test
  public void testAddAll() {
    purgatory.addAll(Collections2.arrayToSet("n1", "n2"));
    assertTrue("Node should have been added", purgatory.contains("n1"));
    assertTrue("Node should have been added", purgatory.contains("n2"));
  }

  @Test
  public void testGetDownNodes() {
    purgatory.add("test");
    DownNode dn = purgatory.getDownNodes().get(0);
    assertEquals("test", dn.getNode());
  }

  @Test
  public void testClear() {
    purgatory.add("test");
    DownNode dn = purgatory.getDownNodes().get(0);
    dn.attempt();
    dn.attempt();
    purgatory.clear(1);
    assertEquals(0, purgatory.size());
  }

  @Test
  public void testClearMaxAttemptsNotReached() {
    purgatory.add("test");
    DownNode dn = purgatory.getDownNodes().get(0);
    dn.attempt();
    dn.attempt();
    purgatory.clear(2);
    assertEquals(1, purgatory.size());
  }

}
