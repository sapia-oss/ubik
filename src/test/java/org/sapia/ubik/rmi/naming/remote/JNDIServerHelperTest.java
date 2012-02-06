package org.sapia.ubik.rmi.naming.remote;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class JNDIServerHelperTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testParseDefaultArgs() {
    System.getProperties().remove(JndiConsts.MCAST_ADDR_KEY);
    System.getProperties().remove(JndiConsts.MCAST_PORT_KEY);
    JNDIServerHelper.Args args  = JNDIServerHelper.parseArgs(new String[]{});
    assertEquals(JndiConsts.DEFAULT_PORT, args.port);
    assertEquals(JndiConsts.DEFAULT_DOMAIN, args.domain);
    assertEquals(JndiConsts.DEFAULT_MCAST_ADDR, args.mcastAddress);
    assertEquals(JndiConsts.DEFAULT_MCAST_PORT, args.mcastPort);
  }
  
  @Test
  public void testParseCustomArgs() {
    System.getProperties().setProperty(JndiConsts.MCAST_ADDR_KEY, "1.2.3.4");
    System.getProperties().setProperty(JndiConsts.MCAST_PORT_KEY, "1234");
    
    JNDIServerHelper.Args args  = JNDIServerHelper.parseArgs(new String[]{"8787", "test"});
    assertEquals(8787, args.port);
    assertEquals("test", args.domain);
    assertEquals("1.2.3.4", args.mcastAddress);
    assertEquals(1234, args.mcastPort);
  }

}
