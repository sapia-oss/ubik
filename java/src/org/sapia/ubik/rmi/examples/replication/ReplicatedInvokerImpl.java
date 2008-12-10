package org.sapia.ubik.rmi.examples.replication;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.replication.ReplicatedCommand;
import org.sapia.ubik.rmi.replication.ReplicatedInvoker;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportManager;

import java.io.IOException;

import java.lang.reflect.Method;

import java.rmi.RemoteException;

import java.util.Set;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ReplicatedInvokerImpl implements ReplicatedInvoker {
  private transient Foo _server;
  private transient Set _siblings;

  public ReplicatedInvokerImpl() {
  }

  /**
   * @see org.sapia.ubik.replication.ReplicatedInvoker#invoke(java.lang.String, java.lang.Class[], java.lang.Object[])
   */
  public Object invoke(String methodName, Class[] sig, Object[] params)
    throws Throwable {
    System.out.println("Invoking: " + methodName);

    Method m = _server.getClass().getMethod(methodName, sig);

    return m.invoke(_server, params);
  }

  /**
   * @see org.sapia.ubik.replication.ReplicatedInvoker#getSiblings()
   */
  public Set getSiblings() {
    return _siblings;
  }

  void setTargetInstance(Foo server) {
    _server = server;
  }

  void setSiblings(Set siblings) {
    _siblings = siblings;
  }

  /**
   * @see org.sapia.ubik.replication.ReplicationContext#dispatch(org.sapia.ubik.replication.ReplicatedCommand, org.sapia.ubik.net.ServerAddress)
   */
  public void dispatch(ReplicatedCommand cmd, ServerAddress addr)
    throws RemoteException {
    System.out.println("Dispatching replicated command");

    Connections conns    = TransportManager.getConnectionsFor(addr);
    Connection  conn     = conns.acquire();
    Object      toReturn;

    try {
      conn.send(cmd);
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

    if (toReturn instanceof Throwable) {
      if (toReturn instanceof RemoteException) {
        throw (RemoteException) toReturn;
      }

      throw new RemoteException("Exception caught dispatching command",
        (Throwable) toReturn);
    }
  }
}
