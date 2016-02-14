package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Test;

public class LocalhostTest {

  @After
  public void tearDown() {
    Localhost.unsetAddressPattern();
  }

  @Test
  public void testDoSelectForIpPatternSetNoMatch() throws Exception {
    Localhost.setAddressPattern("\\d{2}\\.\\d{2}\\.\\d+\\.\\d+");
    InetAddress addr = Localhost.doGetPreferredLocalAddress(Collects.arrayToSet(InetAddress.getByName("127.0.0.1"),
        InetAddress.getByName("192.168.1.1")));
    assertFalse("Expected non-loopback address, got:" + addr.getHostAddress(), addr.getHostAddress().startsWith("127.0"));
  }

  @Test
  public void testDoSelectForLoopbackPatternMatch() throws Exception {
    Localhost.setAddressPattern("127\\.\\d+\\.\\d+\\.\\d+");
    InetAddress addr = Localhost.doGetPreferredLocalAddress(Collects.arrayToSet(InetAddress.getByName("127.0.0.1"),
        InetAddress.getByName("192.168.1.1")));
    assertTrue("Expected loopback address, got:" + addr.getHostAddress(), addr.getHostAddress().startsWith("127.0"));
  }

  @Test
  public void testDoSelectForIpPatternSetMatch() throws Exception {
    Localhost.setAddressPattern("\\d{3}\\.\\d{3}\\.\\d+\\.\\d+");
    InetAddress addr = Localhost.doGetPreferredLocalAddress(Collects.arrayToSet(InetAddress.getByName("192.168.1.1")));
    assertEquals("192.168.1.1", addr.getHostAddress());
  }

  @Test
  public void testDoSelectForIpPatternNotSetWithSingleLocalAddress() throws Exception {
    InetAddress addr = Localhost.doGetPreferredLocalAddress(Collects.arrayToSet(InetAddress.getByName("192.168.1.1")));
    assertEquals("192.168.1.1", addr.getHostAddress());
  }

  @Test
  public void testDoSelectForIpPatternNotSetWithMultiLocalAddress() throws Exception {
    InetAddress addr = Localhost.doGetPreferredLocalAddress(Collects.arrayToSet(InetAddress.getByName("192.168.1.1"),
        InetAddress.getByName("192.168.1.2")));
    assertFalse("Expected non-loopback address, got: " + addr.getHostAddress(), addr.getHostAddress().startsWith("127.0"));
  }

}
