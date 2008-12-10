package org.sapia.ubik.rmi.server;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportManager;


/**
 * This class implements the basic behavior of dynamic proxies that implement the
 * logic for performing RPC on remote objects.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class RemoteRef implements StubInvocationHandler,
  Externalizable, HealthCheck {
  static final long               serialVersionUID = 1L;
  protected boolean               _callBack;
  protected OID                   _oid;
  protected VmId                  _vmId          = VmId.getInstance();
  protected ServerAddress         _serverAddress;
  protected boolean               _isFirstVoyage = true;
  protected transient Connections _pool;
  protected transient Object      _lock          = new Object();

  public RemoteRef() {
  }

  /**
   * Creates an instance of this class, with the given object and host
   * identifiers.
   *
   * @param oid an <code>OID</code>
   * @param serverAddress a <code>ServerAddress</code>
   *
   */
  public RemoteRef(OID oid, ServerAddress serverAddress) {
    this();
    _oid             = oid;
    _serverAddress   = serverAddress;
  }

  /**
   * Returns <code>true</code> if this stub is in call-back mode.
   *
   * @return <code>true</code> if this stub is in call-back mode.
   */
  public boolean isCallBack() {
    return _callBack;
  }
  
  /**
   * Returns the identifier of the remote object to which this instance
   * corresponds.
   *
   * @return an <code>OID</code>.
   */
  public OID getOID(){
    return _oid;
  }
  
  /**
   * Sets this stub's call-back mode.
   *
   * @param callBack must be <code>true</code> if this stub should
   * be in call-back mode.
   */
  protected void setCallBack(boolean callBack) {
    _callBack = callBack;
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
   */
  public abstract Object invoke(Object obj, Method toCall, Object[] params)
    throws Throwable;

  /**
   * Returns the address of the server to which this stub "belongs".
   *
   * @return a <code>ServerAddress</code>
   */
  public ServerAddress getServerAddress() {
    return _serverAddress;
  }

  /**
   * Returns this instance's object identifier.
   *
   * @return a <code>OID</code>
   */
  public OID getOid() {
    return _oid;
  }

  /**
   * Tests the connection between this handler and its server; returns false
   * if connection is invalid.
   *
   * @return <code>false</code> if connection is invalid.
   */
  public boolean isValid() {
    try {
      return ((Boolean) sendCommand(new CommandPing())).booleanValue();
    } catch (Throwable t) {
      return false;
    }
  }

  /**
   * @see StubInvocationHandler#toStubContainer(Object)
   */
  public StubContainer toStubContainer(Object proxy) {
    Set interfaces = new HashSet();
    ServerTable.appendInterfaces(proxy.getClass(), interfaces);

    String[] names = new String[interfaces.size()];
    int      count = 0;

    for (Iterator iter = interfaces.iterator(); iter.hasNext();) {
      names[count++] = ((Class) iter.next()).getName();
    }

    return new StubContainerBase(names, this);
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _callBack        = in.readBoolean();
    _oid             = (OID) in.readObject();
    _vmId            = (VmId) in.readObject();
    _serverAddress   = (ServerAddress) in.readObject();
    _isFirstVoyage   = in.readBoolean();
    _lock            = new Object();
    Hub.clientRuntime.gc.register(_serverAddress, _oid, this);

    if (_isFirstVoyage) {
      _isFirstVoyage = false;
    } else {
      Hub.createReference(_serverAddress, _oid);
    }
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeBoolean(_callBack);
    out.writeObject(_oid);
    out.writeObject(_vmId);
    out.writeObject(_serverAddress);
    out.writeBoolean(_isFirstVoyage);
  }

  protected Object sendCommand(RMICommand cmd) throws Throwable {
    synchronized (_lock) {
      if (_pool == null) {
        initPool(false);
      }
    }

    Connection conn = _pool.acquire();

    try {
      try {
        conn.send(cmd);
      } catch (RemoteException e) {
        synchronized (_lock) {
          _pool.clear();
        }

        conn = _pool.acquire();
        conn.send(cmd);
      }

      Object toReturn = conn.receive();

      if (toReturn == null) {
        return toReturn;
      } else if (toReturn instanceof Throwable) {
        Throwable err = (Throwable) toReturn;
        err.fillInStackTrace();
        throw err;
      }

      return toReturn;
    } finally {
      _pool.release(conn);
    }
  }

  protected synchronized void initPool(boolean force)
    throws java.rmi.RemoteException {
    if (_pool == null) {
      _pool = TransportManager.getConnectionsFor(_serverAddress);
    } else if (force) {
      _pool.clear();
      _pool = TransportManager.getConnectionsFor(_serverAddress);
    }
  }
}
