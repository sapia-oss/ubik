package org.sapia.ubik.rmi.server.transport.http;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.Uri;


/**
 * Models an HTTP address.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpAddress extends TCPAddress {
  private String _uri;

  /** Do not call; used for externalization only. */
  public HttpAddress() {
  }

  public HttpAddress(Uri uri) {
    super(uri.getHost(), uri.getPort());
    _transportType   = HttpConsts.DEFAULT_HTTP_TRANSPORT_TYPE;
    _uri             = uri.toString();
  }

  protected HttpAddress(String transportType, Uri uri) {
    this(uri);
    _transportType = transportType;
  }

  /**
   * Returns the string corresponding to this address' URL (of the form http://someHost:somePort).
   */
  public String toString() {
    return _uri;
  }

  /**
   * @see org.sapia.ubik.net.TCPAddress#readExternal(java.io.ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    _uri = in.readUTF();
  }

  /**
   * @see org.sapia.ubik.net.TCPAddress#writeExternal(java.io.ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeUTF(_uri);
  }
}
