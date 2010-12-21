package org.sapia.ubik.rmi.server;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.jmx.JmxHelper;
import org.sapia.ubik.jmx.MBeanContainer;
import org.sapia.ubik.jmx.MBeanFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider;

/**
 * This class is a singleton that implements the <code>Server</code> interface.
 * It internally keeps a table of running servers, on a per-transport-type-to-server-instance
 * basis. There can only be a single server instance per transport type.
 * <p>
 * This class holds methods  pertaining to stub creations, etc. It and delegates the <code>Server</code> interface's
 * methods to the internal <coce>Server</code> implementation(s).
 * <p>
 * For example, calling shutdown on this singleton will trigger the shutdown of all internal server instances.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerTable implements Server {
  static final String DEFAULT_TRANSPORT_TYPE = SocketTransportProvider.TRANSPORT_TYPE;

  // A "cache" of class-to-interfaces mappings.
  private static Map<Class<?>, Class<?>[]> _interfaceCache = new ConcurrentHashMap<Class<?>, Class<?>[]>();
  Map<String, ServerRef> _serversByType = new ConcurrentHashMap<String, ServerRef>();

  /*////////////////////////////////////////////////////////////////////
                      Server interface methods
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() {
    //noop
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#close()
   */
  public void close() {
    Iterator<ServerRef>  itr = _serversByType.values().iterator();
    ServerRef ref;

    while (itr.hasNext()) {
      ref = itr.next();
      ref.server.close();
    }
  }

  /**
   * Returns the adress of the server corresponding to the default transport type.
   *
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()()
   */
  public ServerAddress getServerAddress() {
    return getServerRef(DEFAULT_TRANSPORT_TYPE).server.getServerAddress();
  }

  public ServerAddress getServerAddress(String transportType) {
    return getServerRef(transportType).server.getServerAddress();
  }

  /*////////////////////////////////////////////////////////////////////
                          Decorator methods
  ////////////////////////////////////////////////////////////////////*/

  /**
   * Returns the remote reference of this instance.
   *
   * @return a <code>RemoteRef</code>.
   */
  public RemoteRef getRemoteRef(String transportType) {
    return getServerRef(transportType).ref;
  }

  /**
   * Returns the unique object identifier of this instance.
   *
   * @return an <code>OID</code>.
   */
  public OID getOID(String transportType) {
    return getServerRef(transportType).oid;
  }

  /*////////////////////////////////////////////////////////////////////
                      Restricted Access Methods.
  ////////////////////////////////////////////////////////////////////*/
  boolean isInit(String transportType) {
    return _serversByType.get(transportType) != null;
  }

  ServerAddress init(Object remote, String transportType)
    throws RemoteException, IllegalStateException {
    Server s = TransportManager.getProviderFor(transportType).newDefaultServer();
    s.start();

    return doInit(remote, s, transportType);
  }

  ServerAddress init(Object remote)
    throws RemoteException, IllegalStateException {
    return init(remote, 0);
  }

  ServerAddress init(Object remote, int port)
    throws RemoteException, IllegalStateException {
    return doInit(remote, port, ServerTable.DEFAULT_TRANSPORT_TYPE);
  }

  ServerAddress init(Object remote, String transportType, Properties props)
    throws RemoteException, IllegalStateException {
    
    if(Log.isDebug()){
      Log.debug(getClass(), "Getting server for transport : " + transportType + 
        ": properties: " + props);
    }
    Server s = TransportManager.getProviderFor(transportType).newServer(props);

    s.start();

    return doInit(remote, s, transportType);
  }

  private synchronized ServerAddress doInit(Object remote, int port,
    String transportType) throws RemoteException, IllegalStateException {
    if (_serversByType.get(transportType) != null) {
      throw new IllegalStateException("server already created");
    }

    if (Log.isDebug()) {
      Log.debug(ServerTable.class, "initializing server");
    }

    Server s;

    try {
      s = TransportManager.getDefaultProvider().newServer(port);
    } catch (java.io.IOException e) {
      throw new java.rmi.RemoteException("could not create singleton server", e);
    }

    s.start();

    return doInit(remote, s, transportType);
  }

  private synchronized ServerAddress doInit(Object remote, Server s,
    String transportType) throws RemoteException, IllegalStateException {
    if (_serversByType.get(transportType) != null) {
      throw new IllegalStateException("server already created");
    }

    ServerRef serverRef = new ServerRef();
    serverRef.server = s;

    if (Log.isDebug()) {
      Log.debug(ServerTable.class,
        "server listening on: " + serverRef.server.getServerAddress());
    }

    if (remote != null) {
      serverRef.oid   = generateOID();

      serverRef.ref    = initRef(remote, serverRef.oid, serverRef);
      serverRef.stub   = (Stub) Hub.getStubFor(serverRef.ref, remote);
    }

    _serversByType.put(transportType, serverRef);
    
    if(s instanceof MBeanFactory){
      try{      
        MBeanContainer mbc = ((MBeanFactory)s).createMBean();
        JmxHelper.registerMBean(mbc.getName(), mbc.getMBean());
      }catch(Exception e){
        throw new RemoteException("Could not create MBean", e);
      }
    }

    return serverRef.server.getServerAddress();
  }

  final RemoteRef initStub(Object remote, String transportType) {
    return initRef(remote, generateOID(), getServerRef(transportType));
  }

  static Class<?>[] getInterfacesFor(Class<?> clazz) {
    Class<?>[] cachedInterfaces = _interfaceCache.get(clazz);

    if (cachedInterfaces == null) {
      Class<?>   current = clazz;
      Remote remoteAnno = current.getAnnotation(Remote.class);
      if(remoteAnno != null){
        Class<?>[] remoteInterfaces = remoteAnno.interfaces();
        HashSet<Class<?>> set     = new HashSet<Class<?>>();
        set.add(Stub.class);
        for(Class<?> remoteInterface: remoteInterfaces){
          set.add(remoteInterface);
        }
        cachedInterfaces = set.toArray(new Class[set.size()]);
      }
      else{
        HashSet<Class<?>> set     = new HashSet<Class<?>>();
        appendInterfaces(current, set);
        set.add(Stub.class);
        cachedInterfaces = set.toArray(new Class[set.size()]);
      }
      _interfaceCache.put(clazz, cachedInterfaces);
    }

    return cachedInterfaces;
  }

  static void appendInterfaces(Class<?> current, Set<Class<?>> interfaces) {
    Class<?>[] ifs = current.getInterfaces();

    for (int i = 0; i < ifs.length; i++) {
      appendInterfaces(ifs[i], interfaces);
      interfaces.add(ifs[i]);
    }

    current = current.getSuperclass();

    if (current != null) {
      appendInterfaces(current, interfaces);
    }
  }

  ServerRef getServerRef(String transportType) throws IllegalArgumentException {
    ServerRef ref = (ServerRef) _serversByType.get(transportType);

    if (ref == null) {
      throw new IllegalStateException("No server for type: " + transportType);
    }

    return ref;
  }

  static synchronized OID generateOID() {
    return new OID(UIDGenerator.createdUID());
  }

  private final RemoteRef initRef(Object remote, OID oid, ServerRef serverRef) {
    RemoteRefEx rmiHandler = new RemoteRefEx(oid,
        serverRef.server.getServerAddress());

    rmiHandler.setCallBack((System.getProperty(Consts.CALLBACK_ENABLED) != null) &&
      System.getProperty(Consts.CALLBACK_ENABLED).equalsIgnoreCase("true"));

    if (Log.isDebug()) {
      Log.debug(ServerTable.class,
        remote + " is call-back: " + rmiHandler.isCallBack());
    }

    //    Object  proxy = Hub.getStubFor(rmiHandler);
    Hub.serverRuntime.objectTable.register(oid, remote);

    return rmiHandler;
  }
}
