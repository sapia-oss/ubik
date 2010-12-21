package org.sapia.ubik.rmi.server;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.Name;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.invocation.CallBackInvokeCommand;
import org.sapia.ubik.rmi.server.invocation.InvokeCommand;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportManager;


/**
 * A stub handler that manages reconnecting to another server instance
 * provided a method call fails.
 * <p>
 * Note that this class does NOT inherit from <code>RemoteRef</code> - despite what
 * the name mignt suggest. This is because this class, by definition, does not
 * correspond to a single server endpoint, but to multiple server endpoints.
 * <p>
 * Indeed, an instance of this class corresponds to all servers that were bound
 * under a given name.
 * <p>
 * This design might review eventually, to provide a more consistent class hierarchy.
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RemoteRefStateless implements StubInvocationHandler,
  /*AsyncEventListener,*/ Externalizable, HealthCheck {
  static final long serialVersionUID = 1L;
  //private static List _statelessTubs = new Collections.synchronizedList(new ArrayList());
  protected Name    _name;
  protected String  _domain;
  protected String  _mcastAddress;
  protected int     _mcastPort;
  protected OID     _oid          = new OID(UIDGenerator.createdUID());
  protected transient boolean _isRegistered;
  protected List<ServiceInfo>    _serviceInfos = Collections.synchronizedList(new LinkedList<ServiceInfo>());
  
  /**
   * Constructor for RemoteRefStateless.
   */
  public RemoteRefStateless() {
    super();
  }

  /**
   * Creates an instance of this class
   */
  public RemoteRefStateless(Name name, String domain) {
    _name     = name;
    _domain   = domain;
  }
  
  /**
   * @see StubInvocationHandler#getOID()
   */
  public OID getOID(){
    return _oid;
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
   */
  public Object invoke(Object obj, Method toCall, Object[] params)
    throws Throwable {
    Object      toReturn = null;

    ServiceInfo info = acquire();

    try {
      toReturn = doInvoke(info, obj, toCall, params);
    } catch (ShutdownException e) {
      toReturn = handleError(info, obj, toCall, params, e);
    } catch (RemoteException e) {
      toReturn = handleError(info, obj, toCall, params, e);
    }

    return toReturn;
  }

  /**
   * @see org.sapia.ubik.rmi.server.HealthCheck#isValid()
   */
  public boolean isValid() {
    try {
      return clean();//((Boolean) sendCommand(new CommandPing())).booleanValue();
    } catch (Throwable t) {
      Log.error(getClass(), "Stub not valid", t);

      return false;
    }
  }

  /**
   * @see StubInvocationHandler#toStubContainer(Object)
   */
  public StubContainer toStubContainer(Object proxy) {
    Set<Class<?>> interfaces = new HashSet<Class<?>>();
    ServerTable.appendInterfaces(proxy.getClass(), interfaces);

    String[] names = new String[interfaces.size()];
    int      count = 0;

    for(Class<?> intf : interfaces){
      names[count++] = intf.getName();
    }

    return new StubContainerBase(names, this);
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  @SuppressWarnings(value="unchecked")
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _name           = (Name) in.readObject();
    _domain         = in.readUTF();
    _mcastAddress   = in.readUTF();
    _mcastPort      = in.readInt();
    _oid            = (OID) in.readObject();
    _serviceInfos   = (List<ServiceInfo>) in.readObject();

    ServiceInfo info;

    for (int i = 0; i < _serviceInfos.size(); i++) {
      info = (ServiceInfo) _serviceInfos.get(i);
      processServiceInfo(info);
    }

    if (Log.isInfo()) {
      Log.info(getClass(), "Deserializing stateless stub; endpoints: " + _serviceInfos);
    }
    
    StatelessStubTable.registerStatelessRef(this);
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(_name);
    out.writeUTF(_domain);
    out.writeUTF(_mcastAddress);
    out.writeInt(_mcastPort);
    out.writeObject(_oid);
    out.writeObject(_serviceInfos);
  }

  /**
   * Returns a stateless remote reference.
   *
   * @return a <code>RemoteRefStateless</code> instance.
   * @param remoteRefs a list of <code>RemoteRef</code>
   * @param name the name of the service to which the returned object corresponds.
   * @param domain the name of the domain to which the service belongs.
   */
  public static RemoteRefStateless fromRemoteRefs(Name name, String domain,
    List<RemoteRef> remoteRefs) {
    RemoteRefStateless ref     = new RemoteRefStateless(name, domain);
    RemoteRef          current;
    ServiceInfo        info;

    String             mcastAddress = Consts.DEFAULT_MCAST_ADDR;
    int                mcastPort    = Consts.DEFAULT_MCAST_PORT;

    try {
      if (System.getProperty(org.sapia.ubik.rmi.Consts.MCAST_PORT_KEY) != null) {
        mcastPort = Integer.parseInt(System.getProperty(
              org.sapia.ubik.rmi.Consts.MCAST_PORT_KEY));
      }
    } catch (NumberFormatException e) {
      // noop: using default
    }

    if (System.getProperty(org.sapia.ubik.rmi.Consts.MCAST_ADDR_KEY) != null) {
      mcastAddress = System.getProperty(org.sapia.ubik.rmi.Consts.MCAST_ADDR_KEY);
    }

    for (int i = 0; i < remoteRefs.size(); i++) {
      current = (RemoteRef) remoteRefs.get(i);

      if (!ref._serviceInfos.contains(current)) {
        info = new ServiceInfo(current._serverAddress, current._oid,
            current._callBack, current._vmId, current._isFirstVoyage);
        ref._serviceInfos.add(info);
        ref._mcastAddress   = mcastAddress;
        ref._mcastPort      = mcastPort;
      }
    }

    return ref;
  }

  /**
   * Adds another ref's information to this instance.
   *
   * @param other a <code>RemoteRefStateless</code>.
   */
  public void addSibling(RemoteRefStateless other) {
    if (other._oid.equals(_oid)) {
      return;
    }

    synchronized (_serviceInfos) {    
      for (int i = 0; i < other._serviceInfos.size(); i++) {
        ServiceInfo toAdd = (ServiceInfo) other._serviceInfos.get(i);
        if (!_serviceInfos.contains(toAdd)) {
          Log.info(getClass(),
            "Remote server " + other + "added to stateless stub: " +
            toString() + " for name:  " + other._name);
          _serviceInfos.add(toAdd);
        }
      }
      if(Log.isInfo())
        Log.info(getClass(), "Got " + _serviceInfos.size() + " endpoints : " + _serviceInfos);
    }
  }

  /**
   * Returns this instance's underlying connection information.
   *
   * @return the {@link List} of {@link ServiceInfo} instances that this instance holds.
   */
  List<ServiceInfo> getInfos() {
    return _serviceInfos;
  }

  public int hashCode() {
    return _oid.hashCode();
  }

  public boolean equals(Object o) {
    try {
      return (this == o) || ((RemoteRefStateless) o)._oid.equals(_oid);
    } catch (ClassCastException e) {
      return false;
    }
  }

  public String toString() {
    return _serviceInfos.toString();
  }
  
  protected boolean clean(){
    synchronized(_serviceInfos){
      for(int i = 0; i < _serviceInfos.size(); i++){
        ServiceInfo info     = (ServiceInfo)_serviceInfos.get(i);
        Connections pool     = null;
        Connection  conn     = null;        
        try{
          pool = TransportManager.getConnectionsFor(info.address);          
          conn = pool.acquire();
          conn.send(new CommandPing());
          conn.receive();
          pool.release(conn);
        }catch(RemoteException e){
          _serviceInfos.remove(i--);          
          Log.info(getClass(), "Cleaning up invalid endpoint: " + info.address);
          if(pool != null){
            pool.clear();
          }
          if(conn != null){
            conn.close();
          }
        }catch(Exception e){
          Log.error(getClass(), e);
          if(conn != null){
            conn.close();
          }
        }
      }
    }
    return _serviceInfos.size() > 0; 
  }
  
  protected Object sendCommand(RMICommand cmd) throws Throwable {
    ServiceInfo info     = acquire();
    Connections pool     = TransportManager.getConnectionsFor(info.address);
    Connection  conn     = null;
    Object      toReturn = null;

    try {
      while (_serviceInfos.size() > 0) {
        try {
          conn = pool.acquire();
          conn.send(cmd);
          toReturn = conn.receive();

          if (!(toReturn instanceof ShutdownException)) {
            break;
          } else {
            pool.clear();
            info   = removeAcquire(info);
            pool   = TransportManager.getConnectionsFor(info.address);
            //conn   = pool.acquire();
          }
        } catch (RemoteException e) {
          pool.clear();

          try {
            conn = pool.acquire();

            // insuring that it works
            conn.send(new CommandPing());
            conn.receive();
          } catch (RemoteException e2) {
            if (Log.isInfo()) {
              Log.info(getClass(),
                "Invalid endpoint for stateless stub: " + info);
              Log.info(getClass(), "Got " + _serviceInfos.size() + " remaining endpoints: " + _serviceInfos);
            }

            pool.clear();
            info   = removeAcquire(info);
            pool   = TransportManager.getConnectionsFor(info.address);
          }
        }
      }

      if ((_serviceInfos.size() == 0) || (conn == null)) {
        throw new RemoteException("No connection available");
      }

      if (toReturn == null) {
        return toReturn;
      } else if (toReturn instanceof Throwable) {
        Throwable err = (Throwable) toReturn;
        err.fillInStackTrace();
        throw err;
      }

      return toReturn;
    } finally {
      if (conn != null) {
        pool.release(conn);
      }
    }
  }

  protected Object doInvoke(ServiceInfo info, Object obj, Method toCall,
    Object[] params) throws Throwable {
    Object toReturn = null;

    if (info.callback &&
          Hub.clientRuntime.isCallback(info.address.getTransportType())) {
      if (Log.isDebug()) {
        Log.debug(getClass(), "invoking (callback): " + toCall);
      }

      Connections pool = TransportManager.getConnectionsFor(info.address);
      toReturn = ClientRuntime.invoker.dispatchInvocation(info.vmId, pool,
          new CallBackInvokeCommand(info.oid, toCall.getName(), params,
            toCall.getParameterTypes(), info.address.getTransportType()));
    } else {
      if (Log.isDebug()) {
        Log.debug(getClass(), "invoking (no callback): " + toCall);
      }

      Connections pool = TransportManager.getConnectionsFor(info.address);

      toReturn = ClientRuntime.invoker.dispatchInvocation(info.vmId, pool,
          new InvokeCommand(info.oid, toCall.getName(), params,
            toCall.getParameterTypes(), info.address.getTransportType()));
    }

    if (toReturn == null) {
      return toReturn;
    } else if (toReturn instanceof Throwable) {
      Throwable err = (Throwable) toReturn;
      err.fillInStackTrace();
      throw err;
    }

    return toReturn;
  }

  protected ServiceInfo acquire() throws RemoteException {
    if (_serviceInfos.size() == 0) {
      throw new RemoteException("No connection available");
    }

    ServiceInfo toReturn;

    synchronized (_serviceInfos) {
      toReturn = (ServiceInfo) _serviceInfos.remove(0);
      _serviceInfos.add(toReturn);
    }

    return toReturn;
  }

  protected ServiceInfo removeAcquire(ServiceInfo toRemove)
    throws RemoteException {
    synchronized (_serviceInfos) {
      if(Log.isInfo())
        Log.info(getClass(), "Removing invalid instance: " + toRemove.address);      
      _serviceInfos.remove(toRemove);
      if(Log.isInfo())
        Log.info(getClass(), "Remaining: " + _serviceInfos);            
    }

    return acquire();
  }
  
  protected Object handleError(ServiceInfo info, Object obj, Method toCall,
    Object[] params, Throwable err) throws Throwable {
    do {
      info = removeAcquire(info);

      try {
        return doInvoke(info, obj, toCall, params);
      } catch (Throwable t) {
        if (t instanceof RemoteException || t instanceof ShutdownException) {
          err = t;
        }
      }
    } while ((err instanceof ShutdownException ||
          err instanceof RemoteException) && (_serviceInfos.size() > 0));

    throw err;
  }  

  private void processServiceInfo(ServiceInfo info) throws IOException {
    Hub.clientRuntime.gc.register(info.address, info.oid, this);

    if (info.isFirstVoyage) {
      info.isFirstVoyage = false;
    } else {
      try {
        Hub.createReference(info.address, info.oid);
      } catch (RemoteException e) {
        _serviceInfos.remove(info);
      }
    }
  }

  /*////////////////////////////////////////////////////////////////////////////////////
                                      INNER CLASSES
  ////////////////////////////////////////////////////////////////////////////////////*/
  public static class ServiceInfo implements java.io.Serializable {
    
    static final long serialVersionUID = 1L;
    
    ServerAddress address;
    OID           oid;
    boolean       callback;
    boolean       isFirstVoyage;
    VmId          vmId;

    public ServiceInfo(ServerAddress addr, OID id, boolean callback, VmId vmId,
      boolean isFirstVoyage) {
      this.address         = addr;
      this.oid             = id;
      this.callback        = callback;
      this.vmId            = vmId;
      this.isFirstVoyage   = isFirstVoyage;
    }

    public int hashCode() {
      return oid.hashCode();
    }

    public boolean equals(Object other) {
      try {
        ServiceInfo otherInfo = (ServiceInfo) other;

        return otherInfo.oid.equals(oid);
      } catch (ClassCastException e) {
        return false;
      }
    }

    public String toString() {
      return "[ oid=" + oid + ", address=" + address + " ]";
    }
  }
}
