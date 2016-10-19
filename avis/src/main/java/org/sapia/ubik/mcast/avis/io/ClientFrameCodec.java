package org.sapia.ubik.mcast.avis.io;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.sapia.ubik.mcast.avis.io.messages.ConfConn;
import org.sapia.ubik.mcast.avis.io.messages.ConnRply;
import org.sapia.ubik.mcast.avis.io.messages.ConnRqst;
import org.sapia.ubik.mcast.avis.io.messages.Disconn;
import org.sapia.ubik.mcast.avis.io.messages.DisconnRply;
import org.sapia.ubik.mcast.avis.io.messages.DisconnRqst;
import org.sapia.ubik.mcast.avis.io.messages.DropWarn;
import org.sapia.ubik.mcast.avis.io.messages.Message;
import org.sapia.ubik.mcast.avis.io.messages.Nack;
import org.sapia.ubik.mcast.avis.io.messages.NotifyDeliver;
import org.sapia.ubik.mcast.avis.io.messages.NotifyEmit;
import org.sapia.ubik.mcast.avis.io.messages.QuenchPlaceHolder;
import org.sapia.ubik.mcast.avis.io.messages.SecRply;
import org.sapia.ubik.mcast.avis.io.messages.SecRqst;
import org.sapia.ubik.mcast.avis.io.messages.SubAddRqst;
import org.sapia.ubik.mcast.avis.io.messages.SubDelRqst;
import org.sapia.ubik.mcast.avis.io.messages.SubModRqst;
import org.sapia.ubik.mcast.avis.io.messages.SubRply;
import org.sapia.ubik.mcast.avis.io.messages.TestConn;
import org.sapia.ubik.mcast.avis.io.messages.UNotify;

/**
 * Codec for Elvin client protocol message frames.
 * 
 * @author Matthew Phillips
 */
public class ClientFrameCodec
  extends FrameCodec implements ProtocolCodecFactory
{
  public static final ClientFrameCodec INSTANCE = new ClientFrameCodec ();

  public static final IoFilter FILTER = new ProtocolCodecFilter (INSTANCE);
  
  public ProtocolEncoder getEncoder (IoSession session)
    throws Exception
  {
    return INSTANCE;
  }
  
  public ProtocolDecoder getDecoder (IoSession session)
    throws Exception
  {
    return INSTANCE;
  }
  
  @Override
  protected Message newMessage (int messageType, int frameSize)
    throws ProtocolCodecException
  {
    switch (messageType)
    {
      case ConnRqst.ID:
        return new ConnRqst ();
      case ConnRply.ID:
        return new ConnRply ();
      case DisconnRqst.ID:
        return new DisconnRqst ();
      case DisconnRply.ID:
        return new DisconnRply ();
      case Disconn.ID:
        return new Disconn ();
      case SubAddRqst.ID:
        return new SubAddRqst ();
      case SubRply.ID:
        return new SubRply ();
      case SubModRqst.ID:
        return new SubModRqst ();
      case SubDelRqst.ID:
        return new SubDelRqst ();
      case Nack.ID:
        return new Nack ();
      case NotifyDeliver.ID:
        return new NotifyDeliver ();
      case NotifyEmit.ID:
        return new NotifyEmit ();
      case TestConn.ID:
        return TestConn.INSTANCE;
      case ConfConn.ID:
        return ConfConn.INSTANCE;
      case SecRqst.ID:
        return new SecRqst ();
      case SecRply.ID:
        return new SecRply ();
      case UNotify.ID:
        return new UNotify ();
      case DropWarn.ID:
        return new DropWarn ();
      case QuenchPlaceHolder.ADD:
      case QuenchPlaceHolder.MODIFY:
      case QuenchPlaceHolder.DELETE:
        return new QuenchPlaceHolder (messageType, frameSize - 4);
      default:
        throw new ProtocolCodecException
          ("Unknown message type: ID = " + messageType);
    }
  }
}
