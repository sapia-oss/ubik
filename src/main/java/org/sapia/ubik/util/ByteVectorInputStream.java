package org.sapia.ubik.util;

import java.io.IOException;
import java.io.InputStream;


/**
 * This class implements an {@link InputStream} over the {@link ByteVector}
 * class.
 * <p>
 * The <code>close()</code> method has no effect on an instance of this class. The
 * underlying ByteVector can be read from repetitively. If it must be re-read from
 * the beginning, then the vector's <code>reset()</code> method must be called.
 * <p>
 * <b>WARNING: THIS CLASS IS NOT THREAD-SAFE</b>.
 * 
 * @see ByteVectorOutputStream
 *
 * @author Yanick Duchesne
 *
 */
public class ByteVectorInputStream extends InputStream{
  
  private ByteVector bytes;
  
  public ByteVectorInputStream(ByteVector bytes){
    this.bytes = bytes;
  }
  public ByteVector getByteVector(){
    return bytes;
  }
  
  /**
   * @see java.io.InputStream#available()
   */
  public int available() throws IOException {
    return bytes.remaining();
  }
  
  /**
   * @see java.io.InputStream#close()
   */
  public void close() throws IOException {
  }
  
  /**
   * @see java.io.InputStream#mark(int)
   */
  public synchronized void mark(int readlimit) {
    bytes.mark(readlimit);
  }
  
  /**
   * @see java.io.InputStream#markSupported()
   */
  public boolean markSupported() {
    return true;
  }
  
  /**
   * @see java.io.InputStream#read()
   */
  public int read() throws IOException {
    return bytes.read();
  }
  
  /**
   * @see java.io.InputStream#read(byte[], int, int)
   */
  public int read(byte[] b, int off, int len) throws IOException {
    int read = bytes.read(b, off, len);
    return read == 0 ? -1 : read;
  }
  
  /**
   * @see java.io.InputStream#read(byte[])
   */
  public int read(byte[] b) throws IOException {
    int read = bytes.read(b);
    return read == 0 ? -1 : read;    
  }
  
  /**
   * @see java.io.InputStream#reset()
   */
  public synchronized void reset() throws IOException {
    bytes.reset();
  }
  
  /**
   * @see java.io.InputStream#skip(long)
   */
  public long skip(long n) throws IOException {
    return bytes.skip(n);
  }
}
