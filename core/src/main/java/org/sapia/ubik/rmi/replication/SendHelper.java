package org.sapia.ubik.rmi.replication;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * @author Yanick Duchesne
 */
class SendHelper implements Runnable {
  private Object        toSend;
  private ServerAddress addr;
  private boolean       sync;

  SendHelper(Object toSend, ServerAddress addr, boolean sync) {
    this.toSend   = toSend;
    this.addr     = addr;
    this.sync     = sync;
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
    if (sync) {
      return doSend();
    }

    Thread t = new Thread(this);
    t.setName("ubik.rmi.SendHelper");
    t.setDaemon(true);
    t.start();

    return null;
  }

  private Object doSend() throws Throwable {
    Connections  conns     = Hub.getModules().getTransportManager().getConnectionsFor(addr);
    RmiConnection conn     = conns.acquire();
    Object      toReturn;

    try {
      conn.send(toSend);
      toReturn = conn.receive();
    } catch (RemoteException e) {
    	conns.invalidate(conn);
    	conns.clear();
      throw e;
    } catch (IOException e) {
      RemoteException re = new RemoteException("Could not send replicated command",
          e);
    	conns.invalidate(conn);
      throw re;
    } catch (ClassNotFoundException e) {
      RemoteException re = new RemoteException("Could not receive response for replicated command",
          e);
    	conns.invalidate(conn);
      throw re;
    }

    conns.release(conn);

    return toReturn;
  }
}
