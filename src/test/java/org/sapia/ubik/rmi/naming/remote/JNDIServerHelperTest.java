package org.sapia.ubik.rmi.naming.remote;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class JNDIServerHelperTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testParseDefaultArgs() {
    System.getProperties().remove(Consts.MCAST_ADDR_KEY);
    System.getProperties().remove(Consts.MCAST_PORT_KEY);
    JNDIServerHelper.Args args  = JNDIServerHelper.parseArgs(new String[]{});
    assertEquals(Consts.DEFAULT_PORT, args.port);
    assertEquals(Consts.DEFAULT_DOMAIN, args.domain);
    assertEquals(Consts.DEFAULT_MCAST_ADDR, args.mcastAddress);
    assertEquals(Consts.DEFAULT_MCAST_PORT, args.mcastPort);
  }
  
  @Test
  public void testParseCustomArgs() {
    System.getProperties().setProperty(Consts.MCAST_ADDR_KEY, "1.2.3.4");
    System.getProperties().setProperty(Consts.MCAST_PORT_KEY, "1234");
    
    JNDIServerHelper.Args args  = JNDIServerHelper.parseArgs(new String[]{"8787", "test"});
    assertEquals(8787, args.port);
    assertEquals("test", args.domain);
    assertEquals("1.2.3.4", args.mcastAddress);
    assertEquals(1234, args.mcastPort);
  }

}
