package org.sapia.ubik.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements an {@link OutputStream} over a ByteVector. The 
 * underlying vector can be reused by calling its <code>clear()</code> method 
 * (which will bring the vector back to its post-creation state).
 * <p>
 * <b>WARNING: THIS CLASS IS NOT THREAD-SAFE</b>.
 *
 * @author Yanick Duchesne
 */
public class ByteVectorOutputStream extends OutputStream {
  
  private ByteVector bytes;
  
  public ByteVectorOutputStream(ByteVector bytes){
    this.bytes = bytes;
  }

  /**
   * @return the underlying {@link ByteVector} to which this instance writes.
   */
  public ByteVector getByteVector(){
    return bytes;
  }
  
  /**
   * @see java.io.OutputStream#close()
   */
  public void close() throws IOException {
  }
  
  /**
   * @see java.io.OutputStream#flush()
   */
  public void flush() throws IOException {
  }
  
  /**
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  public void write(byte[] b, int off, int len) throws IOException {
    bytes.write(b, off, len);
  }
  
  /**
   * @see java.io.OutputStream#write(byte[])
   */
  public void write(byte[] b) throws IOException {
    bytes.write(b);
  }
  
  /**
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException {
    bytes.write(b);
  }
}
