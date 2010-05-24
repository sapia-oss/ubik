package org.sapia.ubik.util;

import java.io.IOException;
import java.io.InputStream;


/**
 * This class implements an <code>InputStream</code> over the <code>ByteVector</code>
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
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ByteVectorInputStream extends InputStream{
  
  private ByteVector _bytes;
  
  public ByteVectorInputStream(ByteVector bytes){
    _bytes = bytes;
  }
  public ByteVector getByteVector(){
    return _bytes;
  }
  
  /**
   * @see java.io.InputStream#available()
   */
  public int available() throws IOException {
    return _bytes.remaining();
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
    _bytes.mark(readlimit);
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
    return _bytes.read();
  }
  
  /**
   * @see java.io.InputStream#read(byte[], int, int)
   */
  public int read(byte[] b, int off, int len) throws IOException {
    int read = _bytes.read(b, off, len);
    return read == 0 ? -1 : read;
  }
  
  /**
   * @see java.io.InputStream#read(byte[])
   */
  public int read(byte[] b) throws IOException {
    int read = _bytes.read(b);
    return read == 0 ? -1 : read;    
  }
  
  /**
   * @see java.io.InputStream#reset()
   */
  public synchronized void reset() throws IOException {
    _bytes.reset();
  }
  
  /**
   * @see java.io.InputStream#skip(long)
   */
  public long skip(long n) throws IOException {
    return _bytes.skip(n);
  }
}
