package org.sapia.ubik.rmi.server.transport.http;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Uri;

import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;


/**
 * An instance of this class handles incoming requests and delegates them to
 * {@link HttpRmiServerThread}s internally kept in a pool.
 *
 * @author Yanick Duchesne
 */
public class UbikHttpHandler implements ProtocolHandler{
  private HttpAddress             addr;
  private HttpRmiServerThreadPool pool;

  public UbikHttpHandler(Uri localHostUri, int maxThreads) {
    addr   = new HttpAddress(localHostUri);
    pool   = new HttpRmiServerThreadPool(true, maxThreads);
  }

  
  @Override
  public void handle(Request req, Response res) {
    HttpRmiServerConnection conn = new HttpRmiServerConnection(addr, req, res);
    try{
      pool.submit(new org.sapia.ubik.net.Request(conn, addr));
    }catch(Exception e){
      Log.error(getClass(), "Error handling request", e);
    }
  }
}
