package org.sapia.ubik.rmi.server.transport.http;

import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.Log;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;


/**
 * An instance of this class handles incoming requests and delegates them to
 * <code>HttpRmiServerThread</code>s internally kept in a pool.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class UbikHttpHandler implements Container{
  private HttpAddress             _addr;
  private HttpRmiServerThreadPool _pool;

  public UbikHttpHandler(Uri localHostUri, int maxThreads) {
    _addr   = new HttpAddress(localHostUri);
    _pool   = new HttpRmiServerThreadPool(true, maxThreads);
  }

  @Override
  public void handle(Request req, Response res) {
    HttpRmiServerConnection conn = new HttpRmiServerConnection(_addr, req, res);
    try{
      PooledThread            th = (PooledThread) _pool.acquire();
      th.exec(new org.sapia.ubik.net.Request(conn, _addr));
    }catch(Exception e){
      Log.error(getClass(), "Error handling request", e);
    }
  }
}
