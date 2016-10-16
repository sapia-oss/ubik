package org.sapia.ubik.rmi.server.transport.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * An instance of this class instantiates {@link MinaRequestDecoder}s and
 * {@link MinaResponseEncoder}s.
 * 
 * @author yduchesne
 * 
 */
public class MinaCodecFactory implements ProtocolCodecFactory {

  public static final int PREFIX_LEN = 4;

  public ProtocolDecoder getDecoder(IoSession session) throws Exception {
    return new MinaRequestDecoder();
  }

  public ProtocolEncoder getEncoder(IoSession session) throws Exception {
    return new MinaResponseEncoder();
  }

}
