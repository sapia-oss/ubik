package org.sapia.ubik.rmi.server.transport.http;

import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Uri;

import simple.http.Request;
import simple.http.Response;
import simple.http.load.ActiveService;
import simple.http.serve.Context;


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
public class UbikHttpHandler extends ActiveService {
  private HttpAddress             _addr;
  private HttpRmiServerThreadPool _pool;

  public UbikHttpHandler(Uri localHostUri, Context ctx, int maxThreads) {
    super(ctx);
    _addr   = new HttpAddress(localHostUri);
    _pool   = new HttpRmiServerThreadPool(true, maxThreads);
  }

  /**
   * @see simple.http.serve.BasicResource#process(simple.http.Request, simple.http.Response)
   */
  protected void process(Request req, Response res) throws Exception {
    HttpRmiServerConnection conn = new HttpRmiServerConnection(_addr, req, res);
    PooledThread            th = (PooledThread) _pool.acquire();
    th.exec(new org.sapia.ubik.net.Request(conn, _addr));
  }
}
