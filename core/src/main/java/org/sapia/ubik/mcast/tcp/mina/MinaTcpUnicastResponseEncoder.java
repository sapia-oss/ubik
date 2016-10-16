package org.sapia.ubik.mcast.tcp.mina;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.serialization.SerializationStreams;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.MinaByteBufferOutputStream;

/**
 * An encoder of unicast responses.
 * 
 * @author yduchesne
 * 
 */
public class MinaTcpUnicastResponseEncoder implements ProtocolEncoder {

  private static final int BUFSIZE = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);

  private static final String ENCODER_STATE = "ENCODER_STATE";

  private static final int BYTES_PER_INT = 4;

  // --------------------------------------------------------------------------

  private static class EncoderState {

    private IoBuffer outgoing;

    private EncoderState() throws IOException {
      outgoing = IoBuffer.allocate(BUFSIZE);
      outgoing.setAutoExpand(true);
      outgoing.setAutoShrink(true);
    }

  }

  // --------------------------------------------------------------------------

  public void encode(IoSession sess, Object toEncode, ProtocolEncoderOutput output) throws Exception {

    EncoderState es = (EncoderState) sess.getAttribute(ENCODER_STATE);
    if (es == null) {
      es = new EncoderState();
      sess.setAttribute(ENCODER_STATE, es);
    }
    es.outgoing.clear();
    es.outgoing.putInt(0); // reserve space for length header
    doEncode(toEncode, es.outgoing, output);
  }

  void doEncode(Object toEncode, IoBuffer outputBuffer, ProtocolEncoderOutput output) throws Exception {
    ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(new MinaByteBufferOutputStream(outputBuffer));
    oos.writeObject(toEncode);
    oos.flush();
    oos.close();
    // setting length at reserved space
    outputBuffer.putInt(0, outputBuffer.position() - BYTES_PER_INT);

    outputBuffer.flip();
    output.write(outputBuffer);
  }

  public void dispose(IoSession sess) throws Exception {
    EncoderState es = (EncoderState) sess.getAttribute(ENCODER_STATE);
    if (es != null) {
      es.outgoing.free();
    }
  }
}
