package org.sapia.ubik.net.udp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ObjectStreamFactory {
  public static final ObjectStreamFactory DEFAULT_FACTORY = new DefaultObjectStreamFactory();

  public ObjectOutputStream toOutput(java.io.OutputStream os)
    throws IOException;

  public static final class DefaultObjectStreamFactory
    implements ObjectStreamFactory {
    /**
     * @see org.sapia.ubik.net.udp.ObjectStreamFactory#toOutput(OutputStream)
     */
    public ObjectOutputStream toOutput(OutputStream os)
      throws IOException {
      return new ObjectOutputStream(os);
    }
  }
}
