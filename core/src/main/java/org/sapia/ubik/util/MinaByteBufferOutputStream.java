package org.sapia.ubik.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.mina.common.ByteBuffer;

/**
 * An {@link OutputStream} implementation that writes to an internal
 * {@link ByteBuffer}.
 * 
 * @author yduchesne
 * 
 */
public class MinaByteBufferOutputStream extends OutputStream {

  private ByteBuffer buf;

  public MinaByteBufferOutputStream(ByteBuffer buf) {
    this.buf = buf;
  }

  public synchronized void write(int b) throws IOException {
    buf.put((byte) b);
  }

  public synchronized void write(byte[] bytes, int off, int len) throws IOException {
    buf.put(bytes, off, len);
  }

}
