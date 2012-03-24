package org.sapia.ubik.net;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TCPAddressTest {
  
  @Test
  public void testEquals() {
    TCPAddress addr1 = new TCPAddress("test", "localhost", 2222);
    TCPAddress addr2 = new TCPAddress("test", "localhost", 2223);

    assertTrue(addr1.equals(addr1));
    assertTrue(!addr1.equals(addr2));
  }

  @Test
  public void testSet() {
    TCPAddress addr1 = new TCPAddress("test", "localhost", 2222);
    TCPAddress addr2 = new TCPAddress("test", "localhost", 2223);
    Set<TCPAddress> set1  = new HashSet<TCPAddress>();
    Set<TCPAddress> set2  = new HashSet<TCPAddress>();

    set1.add(addr1);
    set2.add(addr2);

    set1.removeAll(set2);
    assertTrue(set1.contains(addr1));
    set1.retainAll(set2);
    assertTrue(!set1.contains(addr1));
    set1.add(addr1);
    set1.removeAll(set1);
    assertTrue(!set1.contains(addr1));
  }
}
