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
 */
public class HttpAddress extends TCPAddress {

  private String uri;

  /** Do not call; used for externalization only. */
  public HttpAddress() {
  }

  /**
   * @param uri
   *          the {@link Uri} for which to create an instance of this class.
   */
  public HttpAddress(Uri uri) {
    this(HttpConsts.HTTP_TRANSPORT_TYPE, uri);
  }

  /**
   * Returns an address of the form <code>http://host:port/ubik</code>.
   * 
   * @param host
   *          the HTTP host to connect to
   * @param port
   *          the HTTP port of the host to connect to.
   * @return a new {@link HttpAddress}.
   */
  public static HttpAddress newDefaultInstance(String host, int port) {
    return new HttpAddress(Uri.parse("http://" + host + ":" + port + HttpConsts.CONTEXT_PATH));
  }

  protected HttpAddress(String transportType, Uri uri) {
    super(transportType, uri.getHost(), uri.getPort());
    this.uri = uri.toString();
    this.transportType = transportType;
  }

  /**
   * Returns the string corresponding to this address' URL (of the form
   * http://someHost:somePort).
   */
  public String toString() {
    return uri;
  }

  /**
   * @see org.sapia.ubik.net.TCPAddress#readExternal(java.io.ObjectInput)
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    uri = in.readUTF();
  }

  /**
   * @see org.sapia.ubik.net.TCPAddress#writeExternal(java.io.ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeUTF(uri);
  }
}
