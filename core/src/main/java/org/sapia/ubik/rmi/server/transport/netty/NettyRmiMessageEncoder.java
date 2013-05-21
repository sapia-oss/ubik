package org.sapia.ubik.rmi.server.transport.netty;

import java.io.ObjectOutputStream;

import org.sapia.ubik.net.netty.NettyResponse;
import org.sapia.ubik.net.netty.NettyResponseEncoder;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;

/**
 * Encodes a {@link NettyResponse} instance so that it can be sent over the wire.
 * 
 * @author yduchesne
 *
 */
public class NettyRmiMessageEncoder extends NettyResponseEncoder {
  
  public NettyRmiMessageEncoder(String loggerName) {
    super(loggerName);
  }
   
  public NettyRmiMessageEncoder() {
    super(NettyRmiMessageEncoder.class.getName());
  }
  
  @Override
  protected void setUp(ObjectOutputStream oos, VmId vmId, String transportType) {
    ((RmiObjectOutput) oos).setUp(vmId, transportType);
  }
}
