package org.sapia.ubik.rmi.server.transport.mina;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.util.MinaByteBufferInputStream;
import org.sapia.ubik.util.Props;

/**
 * A decoder for incoming Ubik client requests.
 * 
 * @author yduchesne
 * 
 */
public class MinaRequestDecoder extends CumulativeProtocolDecoder {

  // ==========================================================================
  // inner classes

  private enum DecodingStatus {
    INITIAL, HEADER_RECEIVED, PAYLOAD_RECEIVED, PROCESSED,
  }

  // --------------------------------------------------------------------------

  private static class DecoderState {
    private DecodingStatus status = DecodingStatus.INITIAL;
    private int payloadSize;
    private ByteBuffer incoming;
    private ObjectInputStream stream;

    private DecoderState() throws IOException {
      incoming = ByteBuffer.allocate(BUFSIZE);
      incoming.setAutoExpand(true);
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

    private ObjectInputStream getObjectInputStream() throws IOException {
      if (stream == null) {
        stream = MarshalStreamFactory.createInputStream(new MinaByteBufferInputStream(incoming));
      }
      return stream;
    }

  }

  // ==========================================================================
  // class variables

  private static int BUFSIZE = Props.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);

  private static final String DECODER_STATE = "DECODER_STATE";

  // ==========================================================================
  // methods

  protected boolean doDecode(IoSession sess, ByteBuffer buf, ProtocolDecoderOutput output) throws Exception {

    if (buf.prefixedDataAvailable(MinaCodecFactory.PREFIX_LEN)) {
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
          output.write(ds.getObjectInputStream().readObject());
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
      ds.incoming.release();
    }
  }

}
