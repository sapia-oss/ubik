package org.sapia.ubik.mcast.tcp.mina;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.serialization.SerializationStreams;
import org.sapia.ubik.util.Conf;

/**
 * A decoder for incoming unicast requests.
 * 
 * @author yduchesne
 * 
 */
public class MinaTcpUnicastRequestDecoder extends CumulativeProtocolDecoder {

  // ==========================================================================
  // inner classes

  private enum DecodingStatus {
    INITIAL, HEADER_RECEIVED, PAYLOAD_RECEIVED, PROCESSED,
  }

  // --------------------------------------------------------------------------

  private static class DecoderState {
    private DecodingStatus status = DecodingStatus.INITIAL;
    private int payloadSize;
    private IoBuffer incoming;

    private DecoderState() throws IOException {
      incoming = IoBuffer.allocate(BUFSIZE);
      incoming.setAutoExpand(true);
      incoming.setAutoShrink(true);
    }

    private void reset() {
      status = DecodingStatus.INITIAL;
      incoming.clear();
    }

    private void headerReceived(int payloadSize) {
      this.payloadSize = payloadSize;
      status = DecodingStatus.HEADER_RECEIVED;
    }

    private void payloadReceived() {
      status = DecodingStatus.PAYLOAD_RECEIVED;
    }

    private void processed() {
      status = DecodingStatus.PROCESSED;
    }

  }

  // ==========================================================================
  // class variables

  private static int BUFSIZE = Conf.getSystemProperties().getIntProperty(Consts.MCAST_BUFSIZE_KEY, Consts.DEFAULT_MARSHALLING_BUFSIZE);

  private static final String DECODER_STATE = "DECODER_STATE";

  // ==========================================================================
  // methods

  protected boolean doDecode(IoSession sess, IoBuffer buf, ProtocolDecoderOutput output) throws Exception {

    if (buf.prefixedDataAvailable(MinaTcpUnicastCodecFactory.PREFIX_LEN)) {
      DecoderState ds = (DecoderState) sess.getAttribute(DECODER_STATE);
      if (ds == null) {
        ds = new DecoderState();
        sess.setAttribute(DECODER_STATE, ds);
      }

      switch (ds.status) {
      case PROCESSED:
        ds.reset();
      case INITIAL:
        ds.headerReceived(buf.getInt());
      case HEADER_RECEIVED:
        if (buf.remaining() >= ds.payloadSize) {
          byte[] payload = new byte[ds.payloadSize];
          buf.get(payload);
          ds.payloadReceived();
          ds.incoming.put(payload);
          ds.incoming.flip();
          ObjectInputStream ois = SerializationStreams.createObjectInputStream(new ByteArrayInputStream(payload));
          output.write(ois.readObject());
          ds.processed();
          return true;
        }
      default: // can only be PAYLOAD_RECEIVED
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public void dispose(IoSession sess) throws Exception {
    super.dispose(sess);
    DecoderState ds = (DecoderState) sess.getAttribute(DECODER_STATE);
    if (ds != null) {
      ds.incoming.free();
    }
  }

}
