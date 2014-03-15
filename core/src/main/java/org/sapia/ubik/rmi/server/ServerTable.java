package org.sapia.ubik.rmi.server;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Callback;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.gc.ServerGC;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.Stub;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Props;
import org.sapia.ubik.util.TypeCache;

/**
 * An instance of this class internally keeps a table of running servers, on a
 * per-transport-type-to-server-instance basis. There can only be a single
 * {@link Server} instance per transport type.
 * <p>
 * This class holds methods pertaining to stub creations, etc.
 * <p>
 *
 * @author Yanick Duchesne
 */
public class ServerTable implements Module {

  private Category log = Log.createCategory(ServerTable.class);
  private TypeCache typeCache = new TypeCache();
  private Map<String, ServerRef> serversByType = new ConcurrentHashMap<String, ServerRef>();
  private ObjectTable objectTable;
  private ServerGC gc;
  private TransportManager transport;
  private StubProcessor stubProcessor;
  private boolean callbackEnabled;
  private Object serverCreationLock = new Object();

  private Stopwatch remoteObjectCreation = Stats.createStopwatch(getClass(), "RemoteObjectCreation", "Remote object creation time");

  @Override
  public void init(ModuleContext context) {
    callbackEnabled = Props.getSystemProperties().getBooleanProperty(Consts.CALLBACK_ENABLED, true);
  }

  @Override
  public void start(ModuleContext context) {
    objectTable = context.lookup(ObjectTable.class);
    gc = context.lookup(ServerGC.class);
    transport = context.lookup(TransportManager.class);
    stubProcessor = context.lookup(StubProcessor.class);
  }

  @Override
  public void stop() {
    for (ServerRef ref : serversByType.values()) {
      ref.getServer().close();
    }
    typeCache.clear();
    serversByType.clear();
  }

  /**
   * Returns the address of the server corresponding to the given transport
   * type.
   *
   * @param transportType
   *          the identifier of a transport type.
   * @return a {@link ServerAddress}.
   */
  public ServerAddress getServerAddress(String transportType) {
    return getServerRef(transportType).getServer().getServerAddress();
  }

  /**
   * @return this instance's {@link ServerGC}.
   */
  public ServerGC getGc() {
    return gc;
  }

  /**
   * Returns the unique object identifier of this instance.
   *
   * @return an {@link DefaultOID}.
   */
  public OID getOID(String transportType) {
    return getServerRef(transportType).getOid();
  }

  /**
   * @return the {@link TypeCache}, which holds the cached interfaces for
   *         classes of remote objects.
   */
  public TypeCache getTypeCache() {
    return typeCache;
  }

  /**
   * @param transportType
   *          a transport type identifier.
   * @return the {@link Server} corresponding to the given type.
   */
  public Server getServerFor(String transportType) {
    ServerRef ref = serversByType.get(transportType);
    if (ref == null) {
      throw new IllegalArgumentException("No server for type: " + transportType);
    }
    return ref.getServer();
  }

  /**
   * @return the number of servers currently active and registered with this instance.
   */
  public int getServerCount() {
    return serversByType.size();
  }

  /**
   * @return the {@link Set} of transport types for which a server exists within this instance.
   */
  public Set<String> getServerTypes() {
    return Collections.unmodifiableSet(serversByType.keySet());
  }

  // --------------------------------------------------------------------------
  // exportObject()

  /**
   * @param toExport
   *          the object to export as a remote object.
   * @param properties
   *          the transport configuration properties.
   * @return the stub that was created.
   * @throws RemoteException
   *           if a problem occurs attempting to export the given object.
   */
  public Object exportObject(Object toExport, Properties properties) throws RemoteException {
    if (toExport instanceof Stub) {
      return toExport;
    }

    String transportType = properties.getProperty(Consts.TRANSPORT_TYPE);
    if (transportType == null) {
      throw new RemoteException(String.format("%s property not specified", Consts.TRANSPORT_TYPE));
    }

    ServerRef serverRef = serversByType.get(transportType);
    if (serverRef == null) {
      synchronized (serverCreationLock) {
        serverRef = serversByType.get(transportType);
        if (serverRef == null) {
          return doExportObjectAndCreateServer(toExport, transportType, properties);
        } else {
          return doExportObject(toExport, transportType, serverRef.getServer().getServerAddress());
        }
      }
    } else {
      return doExportObject(toExport, transportType, serverRef.getServer().getServerAddress());
    }
  }

  private Object doExportObjectAndCreateServer(Object toExport, String transportType, Properties properties) throws RemoteException {
    log.info("Starting new server for transport %s, and exporting object %s", transportType, toExport);
    OID oid = stubProcessor.createOID(toExport);
    Server server;
    if (properties == null) {
      server = transport.getProviderFor(transportType).newDefaultServer();
    } else {
      server = transport.getProviderFor(transportType).newServer(properties);
    }
    RemoteRefContext refContext = new RemoteRefContext(oid, server.getServerAddress());
    refContext.setCallback(callbackEnabled && typeCache.getAnnotationsFor(toExport.getClass()).contains(Callback.class));

    StubInvocationHandler handler = stubProcessor.createInvocationHandlerFor(toExport, refContext);
    Stub stub = (Stub) stubProcessor.createStubFor(toExport, handler);
    ServerRef serverRef = new ServerRef(server, toExport, handler, stub, oid);
    server.start();
    serversByType.put(transportType, serverRef);
    objectTable.register(oid, toExport);
    return stub;
  }

  private Object doExportObject(Object toExport, String transportType, ServerAddress address) throws RemoteException {

    log.info("Server already bound for transport %s, exporting object %s", transportType, toExport);

    OID oid = stubProcessor.createOID(toExport);
    RemoteRefContext refContext = new RemoteRefContext(oid, address);
    refContext.setCallback(callbackEnabled && typeCache.getAnnotationsFor(toExport.getClass()).contains(Callback.class));

    StubInvocationHandler handler = stubProcessor.createInvocationHandlerFor(toExport, refContext);
    Stub stub = (Stub) stubProcessor.createStubFor(toExport, handler);
    objectTable.register(oid, toExport);
    return stub;
  }

  /**
   * @param transport
   *          a transport type.
   * @return <code>true</code> if this instance has a server for the given
   *         transport type.
   */
  public boolean hasServerFor(String transportType) {
    return serversByType.containsKey(transportType);
  }

  // --------------------------------------------------------------------------
  // Remote object creation

  /**
   * Returns a stub for the given object. This method is usually not called by
   * client applications. It is meant for use by the different transport layers.
   *
   * @param toRemote
   *          an object.
   * @param caller
   *          the JVM identifier of the remote client that triggered the
   *          creation of the returned remote reference.
   * @param transportType
   *          the "transport type" for which to return a remote reference.
   * @return a stub.
   * @throws RemoteException
   *
   * @see #asRemoteRef(Object, VmId, String)
   */
  public Object createRemoteObject(Object toRemote, VmId caller, String transportType) throws RemoteException {
    if (toRemote instanceof Stub) {
      return toRemote;
    }

    ServerRef serverRef = serversByType.get(transportType);
    if (serverRef == null) {
      synchronized (serverCreationLock) {
        serverRef = serversByType.get(transportType);
        if (serverRef == null) {
          return doCreateRemoteObjectAndServer(toRemote, caller, transportType);
        } else {
          return doCreateRemoteObject(toRemote, caller, transportType, serverRef.getServer().getServerAddress());
        }
      }
    } else {
      return doCreateRemoteObject(toRemote, caller, transportType, serverRef.getServer().getServerAddress());
    }

  }

  private Object doCreateRemoteObjectAndServer(Object toExport, VmId caller, String transportType) throws RemoteException {
    Split split = remoteObjectCreation.start();
    log.info("Creating server and remote object (transport %s) : %s", transportType, toExport);
    OID oid = stubProcessor.createOID(toExport);
    Server server = transport.getProviderFor(transportType).newDefaultServer();
    RemoteRefContext refContext = new RemoteRefContext(oid, server.getServerAddress());
    refContext.setCallback(callbackEnabled && typeCache.getAnnotationsFor(toExport.getClass()).contains(Callback.class));

    StubInvocationHandler handler = stubProcessor.createInvocationHandlerFor(toExport, refContext);
    Stub stub = (Stub) stubProcessor.createStubFor(toExport, handler);
    ServerRef serverRef = new ServerRef(server, toExport, handler, stub, oid);
    server.start();
    serversByType.put(transportType, serverRef);
    gc.registerRef(caller, oid, toExport);
    split.stop();
    return stub;
  }

  private Object doCreateRemoteObject(Object toExport, VmId caller, String transportType, ServerAddress address) throws RemoteException {
    Split split = remoteObjectCreation.start();
    log.debug("Creating remote object (transport %s): %s", transportType, toExport);
    OID oid = stubProcessor.createOID(toExport);
    RemoteRefContext refContext = new RemoteRefContext(oid, address);
    refContext.setCallback(callbackEnabled && typeCache.getAnnotationsFor(toExport.getClass()).contains(Callback.class));

    StubInvocationHandler handler = stubProcessor.createInvocationHandlerFor(toExport, refContext);
    Stub stub = (Stub) stubProcessor.createStubFor(toExport, handler);
    gc.registerRef(caller, oid, toExport);
    split.stop();
    return stub;
  }

  ServerRef getServerRef(String transportType) throws IllegalArgumentException {
    ServerRef ref = serversByType.get(transportType);
    Assertions.illegalState(ref == null, "No server for type: %s", transportType);
    return ref;
  }

}
