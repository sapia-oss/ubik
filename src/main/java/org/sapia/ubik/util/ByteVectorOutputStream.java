package org.sapia.ubik.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements an <code>OutputStream</code> over a ByteVector. The 
 * underlying vector can be reused by calling its <code>clear()</code> method 
 * (which will bring the vector back to its post-creation state).
 * <p>
 * <b>WARNING: THIS CLASS IS NOT THREAD-SAFE</b>.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ByteVectorOutputStream extends OutputStream {
  
  private ByteVector _bytes;
  
  public ByteVectorOutputStream(ByteVector bytes){
    _bytes = bytes;
  }

  /**
   * @return the underlying <code>ByteVector</code> to which this instance writes.
   */
  public ByteVector getByteVector(){
    return _bytes;
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
    _bytes.write(b, off, len);
  }
  
  /**
   * @see java.io.OutputStream#write(byte[])
   */
  public void write(byte[] b) throws IOException {
    _bytes.write(b);
  }
  
  /**
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException {
    _bytes.write(b);
  }
}
