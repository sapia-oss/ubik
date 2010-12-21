package org.sapia.ubik.rmi.replication;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportManager;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class SendHelper implements Runnable {
  private Object        _toSend;
  private ServerAddress _addr;
  private boolean       _sync;

  SendHelper(Object toSend, ServerAddress addr, boolean sync) {
    _toSend   = toSend;
    _addr     = addr;
    _sync     = sync;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run() {
    try {
      doSend();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  Object send() throws Throwable {
    if (_sync) {
      return doSend();
    }

    Thread t = new Thread(this);
    t.setName("ubik.rmi.SendHelper");
    t.setDaemon(true);
    t.start();

    return null;
  }

  private Object doSend() throws Throwable {
    Connections  conns     = TransportManager.getConnectionsFor(_addr);
    RmiConnection conn     = conns.acquire();
    Object      toReturn;

    try {
      conn.send(_toSend);
      toReturn = conn.receive();
    } catch (RemoteException e) {
      conn.close();
      throw e;
    } catch (IOException e) {
      RemoteException re = new RemoteException("Could not send replicated command",
          e);
      conn.close();
      throw re;
    } catch (ClassNotFoundException e) {
      RemoteException re = new RemoteException("Could not receive response for replicated command",
          e);
      conn.close();
      throw re;
    }

    conns.release(conn);

    return toReturn;
  }
}
