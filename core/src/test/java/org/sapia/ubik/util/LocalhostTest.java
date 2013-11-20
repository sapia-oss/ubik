package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Test;

public class LocalhostTest {

  @After
  public void tearDown() {
    Localhost.unsetAddressPattern();
  }

  @Test
  public void testGetLocalAddress() throws Exception {
    String addr = Localhost.getAnyLocalAddress().getHostAddress();
    assertTrue(!addr.equals("localhost"));
    assertTrue(!addr.equals("127.0.0.1"));
    assertTrue(!addr.equals("0.0.0.0"));
  }

  @Test
  public void testIsLocalAddress() {
    Pattern p = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d+\\.\\d+");
    assertTrue(!Localhost.isLocalAddress(p, "127.0.0.1"));
    assertTrue(!Localhost.isLocalAddress(p, "10.10.10.1"));
    assertTrue(!Localhost.isLocalAddress(p, "localhost"));
    assertTrue(Localhost.isLocalAddress(p, "192.168.0.10"));
  }

  @Test
  public void testDoSelectForIpPatternSetNoMatch() throws Exception {
    Localhost.setAddressPattern("\\d{2}\\.\\d{2}\\.\\d+\\.\\d+");
    InetAddress addr = Localhost.doGetAnyLocalAddress(Collections2.arrayToSet(InetAddress.getByName("127.0.0.1"),
        InetAddress.getByName("192.168.1.1")));
    assertTrue("Expected loopback address", addr.getHostAddress().startsWith("127.0"));
  }

  @Test
  public void testDoSelectForIpPatternSetMatch() throws Exception {
    Localhost.setAddressPattern("\\d{3}\\.\\d{3}\\.\\d+\\.\\d+");
    InetAddress addr = Localhost.doGetAnyLocalAddress(Collections2.arrayToSet(InetAddress.getByName("192.168.1.1")));
    assertEquals("192.168.1.1", addr.getHostAddress());
  }

  @Test
  public void testDoSelectForIpPatternNotSetWithSingleLocalAddress() throws Exception {
    InetAddress addr = Localhost.doGetAnyLocalAddress(Collections2.arrayToSet(InetAddress.getByName("192.168.1.1")));
    assertEquals("192.168.1.1", addr.getHostAddress());
  }

  @Test
  public void testDoSelectForIpPatternNotSetWithMultiLocalAddress() throws Exception {
    InetAddress addr = Localhost.doGetAnyLocalAddress(Collections2.arrayToSet(InetAddress.getByName("192.168.1.1"),
        InetAddress.getByName("192.168.1.2")));
    assertTrue("Expected loopback address", addr.getHostAddress().startsWith("127.0"));
  }

}
