package org.sapia.ubik.rmi.server.transport.netty;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.netty.NettyAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.netty.NettyConsts;
import org.sapia.ubik.rmi.server.transport.netty.NettyTransportProvider;
import org.sapia.ubik.util.Localhost;

public class NettyServerTest implements NettyService {

  @Before
  public void setUp() {
    Hub.shutdown();
    System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "false");
  }
  
  @After
  public void tearDown() {
    Hub.shutdown();
    System.clearProperty(Consts.COLOCATED_CALLS_ENABLED);
  }
  
  @Test
  public void test() throws Exception {
    Properties props = new Properties();
    props.setProperty(Consts.TRANSPORT_TYPE, NettyTransportProvider.TRANSPORT_TYPE);
    props.setProperty(NettyConsts.SERVER_PORT_KEY, "8000");
    Hub.exportObject(this, props);
    
    Hub.connect(new NettyAddress(Localhost.getAnyLocalAddress().getHostAddress(), 8000));
    Hub.shutdown();
  }
  
  
  @Override
  public String getMessage() {
    return "test";
  }
  
}
