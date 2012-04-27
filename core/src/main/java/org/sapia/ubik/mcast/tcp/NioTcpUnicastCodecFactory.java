package org.sapia.ubik.mcast.tcp;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * An instance of this class instantiates {@link NioTcpUnicastRequestDecoder}s and
 * {@link NioTcpUnicastResponseEncoder}s.
 * 
 * @author yduchesne
 *
 */
public class NioTcpUnicastCodecFactory implements ProtocolCodecFactory{
  
  public static final int PREFIX_LEN = 4;
  
  public ProtocolDecoder getDecoder() throws Exception {
    return new NioTcpUnicastRequestDecoder();
  }
  
  public ProtocolEncoder getEncoder() throws Exception {
    return new NioTcpUnicastResponseEncoder();
  }
  
}
