package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Name;

import org.sapia.ubik.jmx.JmxHelper;
import org.sapia.ubik.jmx.MBeanContainer;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.NoSuchObjectException;
import org.sapia.ubik.rmi.PropUtil;
import org.sapia.ubik.rmi.server.gc.CommandRefer;
import org.sapia.ubik.rmi.server.perf.PerfAnalyzer;
import org.sapia.ubik.rmi.server.perf.Statistic;
import org.sapia.ubik.rmi.server.perf.StatsCollector;
import org.sapia.ubik.rmi.server.perf.Topic;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.taskman.TaskManager;
import org.sapia.ubik.taskman.TaskManagerFactory;

/**
 * This class is the single-entry point into Ubik RMI's API.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Hub {
  
  static class FreeMemStatistic extends Statistic{
    public FreeMemStatistic(){
      super("FreeMemory");
    }
    public double getStat() {
      return ((double)Runtime.getRuntime().freeMemory())/(1024*1024);
    }
  }
  static class MaxMemStatistic extends Statistic{
    public MaxMemStatistic(){
      super("MaxMemory");
    }
    public double getStat() {
      return ((double)Runtime.getRuntime().maxMemory())/(1024*1024);
    }
  }  
  static class TotalMemStatistic extends Statistic{
    public TotalMemStatistic(){
      super("TotalMemory");
    }
    public double getStat() {
      return ((double)Runtime.getRuntime().totalMemory())/(1024*1024);
    }
  }  
  
  /** The stats collector */
  public static final StatsCollector statsCollector = new StatsCollector();
  
  /** The hub's task manager */
  public static final TaskManager taskMan = TaskManagerFactory.createDefaulTaskManager();

  /** The object implementing client-side behavior */
  public static final ClientRuntime clientRuntime = new ClientRuntime(taskMan);

  /** The object implementing server-side behavior */
  public static final ServerRuntime serverRuntime = new ServerRuntime(taskMan);
  
  static final Perf                 _perf = new Perf();
  static boolean                    _callback;
  static boolean                    _shutdown;
  
  private static Statistic  _freeMemory  = new FreeMemStatistic();
  private static Statistic  _maxMemory   = new MaxMemStatistic();
  private static Statistic  _totalMemory = new TotalMemStatistic();  

  static {
    
    _callback = (System.getProperty(Consts.CALLBACK_ENABLED) != null) &&
      System.getProperty(Consts.CALLBACK_ENABLED).equalsIgnoreCase("true");
    
    boolean statsEnabled = PerfAnalyzer.getInstance().isEnabled();
    statsCollector.setEnabled(statsEnabled);
    statsCollector.addStat(_freeMemory).addStat(_maxMemory).addStat(_totalMemory);
    
    if(statsEnabled){
      
      PropUtil props = new PropUtil().addProperties(System.getProperties());
      long dumpInterval = props.getLongProperty(Consts.STATS_DUMP_INTERVAL, 0);
      if(dumpInterval > 0){
        dumpInterval = dumpInterval * 1000;
        Task task = new Task(){
          public void exec(TaskContext ctx) {
            statsCollector.dumpStats(System.out);
            for(Topic topic: PerfAnalyzer.getInstance().getTopics()){
              if(topic.isEnabled()){
                statsCollector.dumpStat(System.out, topic.getName(), new Double(topic.duration()));
              }
            }
          }
        };
        taskMan.addTask(new TaskContext("DumpStats", dumpInterval), task);
      }
    }
    
    try{
      registerMBeans();
    }catch(Exception e){
      Log.error(Hub.class, "Could not register MBeans", e);
    }
  }

  /**
   * Generates the stub for the object passed in and returns it.
   *
   * @return an <code>Object</code> which is the stub of the instance passed in.
   * @throws RemoteException if a problem occurs performing the connection.
   */
  public static Object toStub(Object o) throws RemoteException {
    if (o instanceof Stub) {
      return o;
    }

    if (!serverRuntime.server.isInit(ServerTable.DEFAULT_TRANSPORT_TYPE)) {
      serverRuntime.server.init(o, ServerTable.DEFAULT_TRANSPORT_TYPE);

      ServerRef ref = serverRuntime.server.getServerRef(ServerTable.DEFAULT_TRANSPORT_TYPE);

      return ref.stub;
    } else {
      return getStubFor(serverRuntime.server.initStub(o,
          ServerTable.DEFAULT_TRANSPORT_TYPE), o);
    }
  }

  /**
   * Returns a "reliable" stub for the given passed in stub; the dynamically generated
   * proxy (created with Java's reflection API) wraps an <code>InvocationHandler</code>
   * of the <code>StubHandlerReliable</code> class.
   * <p>
   * The object passed as a parameter is expected to be a stub whose invocation handler
   * is assumed to be of the <code>StubHandlerBasic</code> type. This method thus performs
   * a stub substitution.
   *
   * @see RemoteRefReliable
   * @see RemoteRefEx
   *
   * @return a "reliable" stub for the given stub.
   */
  public static Object toReliableStub(Object obj) {
    if (obj instanceof Stub && Proxy.isProxyClass(obj.getClass())) {
      RemoteRef         handler = (RemoteRef) Proxy.getInvocationHandler(obj);

      RemoteRefReliable rmiHandler = new RemoteRefReliable(handler.getOid(),
          handler.getServerAddress());
      rmiHandler.setCallBack(handler.isCallBack());

      ObjectTable.Ref ref = (ObjectTable.Ref) serverRuntime.objectTable.getRefFor(handler.getOid());

      Object proxy = Proxy.newProxyInstance(Thread.currentThread()
                                                  .getContextClassLoader(),
          ServerTable.getInterfacesFor(ref._obj.getClass()), rmiHandler);

      return proxy;
    } else {
      return obj;
    }
  }

  /**
   * Returns a stateless stub for the given object.
   *
   * @param name the name of the object for which a stateless stub should be returned.
   * @param domain the name of the domain to which the  object "belongs".
   * @param obj
   * @return a <code>Stateless</code> stub.
   *
   * @see Stateless
   */
  public static Object toStatelessStub(Name name, String domain, Object obj) {
    if (obj instanceof Stub && Proxy.isProxyClass(obj.getClass())) {
      RemoteRef          handler = (RemoteRef) Proxy.getInvocationHandler(obj);

      RemoteRefStateless rmiHandler;
      List<RemoteRef>               lst = new ArrayList<RemoteRef>(1);
      lst.add(handler);
      rmiHandler = RemoteRefStateless.fromRemoteRefs(name, domain, lst);

      ObjectTable.Ref ref = (ObjectTable.Ref) serverRuntime.objectTable.getRefFor(handler.getOid());

      if (ref == null) {
        throw new NoSuchObjectException("no object for: " + handler.getOid());
      }

      Object proxy = Proxy.newProxyInstance(Thread.currentThread()
                                                  .getContextClassLoader(),
          ServerTable.getInterfacesFor(ref._obj.getClass()), rmiHandler);

      return proxy;
    } else {
      return obj;
    }
  }

  /**
   * "Exports" the passed in object as a remote RMI server: this method
   * internally starts an RMI server that listens on a random port and implements
   * the interfaces of the passed in object. The stub is returned and can be bound
   * to the JNDI.
   *
   * @see #exportObject(Object, int)
   * @see #connect(String, int)
   * @return the stub corresponding to the exported object.
   * @throws RemoteException if a problem occurs performing the connection.
   */
  public static Object exportObject(Object o) throws RemoteException {
    if (!serverRuntime.server.isInit(ServerTable.DEFAULT_TRANSPORT_TYPE)) {
      serverRuntime.server.init(o, ServerTable.DEFAULT_TRANSPORT_TYPE);

      ServerRef ref = serverRuntime.server.getServerRef(ServerTable.DEFAULT_TRANSPORT_TYPE);

      return ref.stub;
    } else {
      return getStubFor(serverRuntime.server.initStub(o,
          ServerTable.DEFAULT_TRANSPORT_TYPE), o);
    }
  }

  /**
   * This method creates a server listening on the specified port.
   *
   * @see #exportObject(Object)
   * @throws RemoteException if a problem occurs performing the connection.
   * @return the stub for the given exported object.
   */
  public static Object exportObject(Object o, int port)
    throws RemoteException {
    if (!serverRuntime.server.isInit(ServerTable.DEFAULT_TRANSPORT_TYPE)) {
      serverRuntime.server.init(o, port);

      ServerRef ref = serverRuntime.server.getServerRef(ServerTable.DEFAULT_TRANSPORT_TYPE);

      return ref.stub;
    } else {
      return getStubFor(serverRuntime.server.initStub(o,
          ServerTable.DEFAULT_TRANSPORT_TYPE), o);
    }
  }

  /**
   * Exports the given object as a server (and creates a remote reference). The properties
   * passed in must contain the property identifying the desired "transport type"
   * (ubik.rmi.transport.type).
   * <p>
   * The method returns the stub for the given object.
   *
   * @return the stub of the exported server.
   * @see TransportManager
   * @see TransportManager#getProviderFor(String)
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider
   */
  public static Object exportObject(Object o, Properties props)
    throws RemoteException {
    String transportType = props.getProperty(Consts.TRANSPORT_TYPE);

    if (transportType == null) {
      transportType = System.getProperty(Consts.TRANSPORT_TYPE);
    }

    if (transportType == null) {
      throw new RemoteException(Consts.TRANSPORT_TYPE +
        " property not specified");
    }

    if (!serverRuntime.server.isInit(transportType)) {
      serverRuntime.server.init(o, transportType, props);

      ServerRef ref = serverRuntime.server.getServerRef(transportType);

      return ref.stub;
    } else {
      return getStubFor(serverRuntime.server.initStub(o, transportType), o);
    }
  }

  /**
   * Exports the given object as a remote object that will receive request through a server
   * that must already have been exported for the given transport type.
   * <p>
   * The method returns the stub for the given object.

   * @param o an <code>Object</code> to export.
   * @param transportType the identifier of the transport layer to which the given object
   * will be exported.
   * @return a stub.
   * @throws RemoteException if the object could not be exported.
   */
  public static Object exportObject(Object o, String transportType)
    throws RemoteException {
    if (!serverRuntime.server.isInit(transportType)) {
      throw new RemoteException("No server was exported for transport: " +
        transportType);
    } else {
      return getStubFor(serverRuntime.server.initStub(o, transportType), o);
    }
  }

  /**
   * This method "unexports" an object that was exported through one of this
   * class' <code>export()</code> methods. The unexported object will not receive
   * remote method calls anymore.
   * <p>
   * NOTE: this method does not stop the server through which the exported instance
   * is receiving remote method calls. To stop the servers that have been started by
   * the <code>Hub</code>, call the latter's <code>shutdown()</code> method.
   *
   * @see #shutdown(long)
   *
   * @param o the exported object that is to be unexported.
   */
  public static void unexport(Object o) {
    serverRuntime.objectTable.remove(o);
  }

  /**
   * This method "unexports" all objects whose class was loaded by the given <code>ClassLoader</code>.
   * <p>
   * This method can be useful in hot-deploy scenarios.
   * <p>
   * NOTE: this method does not stop the server through which the exported instances (that
   * correspond to the given classloader) are receiving remote method calls. To stop the servers that have been started by
   * the <code>Hub</code>, call the latter's <code>shutdown()</code> method.
   *
   * @see #shutdown(long)
   *
   * @param loader
   */
  public static void unexport(ClassLoader loader) {
    serverRuntime.objectTable.remove(loader);
  }

  /**
   * This method allows connecting to a RMI server listening to the given host
   * and port.
   *
   * @throws RemoteException if a problem occurs performing the connection.
   */
  public static Object connect(String host, int port) throws RemoteException {
    return connect(new TCPAddress(host, port));
  }

  /**
   * This method allows connecting to a RMI server listening on the given address.
   *
   * @param address the <code>ServerAddress</code> corresponding to the target server's
   * physical endpoint.
   *
   * @throws RemoteException if a problem occurs performing the connection.
   */
  public static Object connect(ServerAddress address) throws RemoteException {
    RmiConnection conn     = null;
    Object     toReturn;

    try {
      
      conn = TransportManager.getConnectionsFor(address).acquire();

      try {
        conn.send(new CommandConnect(address.getTransportType()));
      } catch (RemoteException e) {
        Connections conns = TransportManager.getConnectionsFor(address);
        conns.clear();
        conn = conns.acquire();
        conn.send(new CommandConnect(address.getTransportType()));
      }

      toReturn = conn.receive();
    } catch (ConnectException e){
      throw new RemoteException("No server at address: " + address, e);
    } catch (IOException e) {
      throw new RemoteException("Error connecting to remote server " + address, e);
    } catch (ClassNotFoundException e) {
      throw new RemoteException("Could not find class", e);
    } finally {
      if (conn != null) {
        TransportManager.getConnectionsFor(address).release(conn);
      }
    }

    if (toReturn instanceof Throwable) {
      if (toReturn instanceof RuntimeException) {
        throw (RuntimeException) toReturn;
      } else {
        throw new RemoteException("Problem connecting to remote server",
          (Throwable) toReturn);
      }
    }

    return toReturn;
  }
  
  /**
   * Forces the clearing of the connection pool corresponding to the given address.
   * 
   * @see Connections#clear()
   * @param address a {@link ServerAddress}
   */
  public static void refresh(ServerAddress address){
    try{
      TransportManager.getConnectionsFor(address).clear();
    }catch(RemoteException e){
      // noop
    }
  }

  /**
   * Returns the address of the server for the given transport type.
   * @param transportType the logical identifier of a "transport type".
   * @return a <code>ServerAddress</code>,
   */
  public static ServerAddress getServerAddressFor(String transportType) {
    return serverRuntime.server.getServerAddress(transportType);
  }

  /**
   * Returns a stub for the given object. This method is usually
   * not called by client application. It is meant for use by the different
   * transport layers.
   *
   * @param o an object.
   * @param caller the VM identifier of the client that triggered the creation
   * of the returned remote reference.
   * @param transportType the "transport type" for which to return a remote reference.
   * @return a stub.
   * @throws RemoteException
   *
   * @see #asRemoteRef(Object, VmId, String)
   */
  public static Object asRemote(Object o, VmId caller, String transportType)
    throws RemoteException {
    if (o instanceof Stub) {
      return o;
    }

    return getStubFor(asRemoteRef(o, caller, transportType), o);
  }

  /**
   * Returns a remote reference for the given object. This method is usually
   * not called by client application. It is meant for use by the different
   * transport layers.
   *
   * @param o an object.
   * @param caller the VM identifier of the client that triggered the creation
   * of the returned remote reference.
   * @param transportType the "transport type" for which to return a remote reference.
   * @return a <code>RemoteRef</code>.
   * @throws RemoteException
   */
  public static RemoteRef asRemoteRef(Object o, VmId caller,
    String transportType) throws RemoteException {
    if (_perf.createRemoteRef.isEnabled()) {
      _perf.createRemoteRef.start();
    }

    if (!serverRuntime.server.isInit(transportType)) {
      if(Log.isInfo()){
        Log.info(Hub.class, "Exporting default server for : " + caller);
      }
      serverRuntime.server.init(transportType);
    }

    OID oid = ServerTable.generateOID();

    if (_perf.registerRemoteRef.isEnabled()) {
      _perf.registerRemoteRef.start();
    }

    serverRuntime.gc.registerRef(caller, oid, o);

    if (_perf.registerRemoteRef.isEnabled()) {
      _perf.registerRemoteRef.end();
    }

    RemoteRef ref;

    if (_perf.instantianteRemoteRef.isEnabled()) {
      _perf.instantianteRemoteRef.start();
    }

    ref = new RemoteRefEx(oid,
        serverRuntime.server.getServerRef(transportType).server.getServerAddress());

    ref.setCallBack(_callback);

    _perf.instantianteRemoteRef.end();
    _perf.createRemoteRef.end();      

    return ref;
  }

  /**
   * Returns true if the Hub is shut down.
   *
   * @return <code>true</code> if the Hub is shut down.
   */
  public static boolean isShutdown() {
    return _shutdown;
  }

  /**
   * Shuts down this instance; some part of the shutdown can be asynchronous.
   * A timeout must be given in order not to risk the shutdown to last for too long.
   *
   * @param timeout a shutdown "timeout", in millis.
   * @throws InterruptedException
   */
  public static synchronized void shutdown(long timeout)
    throws InterruptedException {
    if (_shutdown) {
      return;
    }

    Log.warning(Hub.class, "Shutting down task manager");
    taskMan.shutdown();
    Log.warning(Hub.class, "Shutting down client runtime");
    clientRuntime.shutdown(timeout);
    Log.warning(Hub.class, "Shutting down server runtime");
    serverRuntime.shutdown(timeout);
    Log.warning(Hub.class, "Shutting down event channels");
    EventChannelSingleton.shutdown();
    Log.warning(Hub.class, "Shutting down transport manager");
    TransportManager.shutdown();
    Log.warning(Hub.class, "Shut down completed");
    _shutdown = true;
  }

  public static Object getStubFor(RemoteRef ref, Object remote) {
    
    Class<?>[] cachedInterfaces = ServerTable.getInterfacesFor(remote.getClass());

    if (_perf.createStub.isEnabled()) {
      _perf.createStub.start();
    }

    Object proxy = Proxy.newProxyInstance(Thread.currentThread()
                                                .getContextClassLoader(),
        cachedInterfaces, ref);

    if (_perf.createStub.isEnabled()) {
      _perf.createStub.end();
    }

    return proxy;
  }

  static void createReference(ServerAddress address, OID oid)
    throws RemoteException {
    Connections conns = TransportManager.getConnectionsFor(address);

    try {
      doSend(conns, oid);
    } catch (ClassNotFoundException e) {
      throw new RemoteException("could not refer to object: " + oid + "@" +
        address, e);
    } catch (RemoteException e) {
      conns.clear();

      try {
        doSend(conns, oid);
      } catch (Exception e2) {
        throw new RemoteException("could not refer to object: " + oid + "@" +
          address, e2);
      }
    } catch (IOException e) {
      throw new RemoteException("could not refer to object: " + oid + "@" +
        address, e);
    }
  }
  
  private static void registerMBeans() throws Exception{
    Log.info(Hub.class, "Registering MBeans");
    bind(clientRuntime.gc.createMBean());
    bind(serverRuntime.gc.createMBean());    
    bind(serverRuntime.objectTable.createMBean());    
    bind(PerfAnalyzer.getInstance().createMBean());    
  }
  
  private static void bind(MBeanContainer cont) throws Exception{
    JmxHelper.registerMBean(cont.getName(), cont.getMBean());
  }

  private static void doSend(Connections conns, OID oid)
    throws RemoteException, IOException, ClassNotFoundException {
    RmiConnection conn = null;

    try {
      conn = conns.acquire();
      conn.send(new CommandRefer(oid));
      conn.receive();
    } finally {
      if (conn != null) {
        conns.release(conn);
      }
    }
  }
  
  static class Perf{
    Topic createRemoteRef         = PerfAnalyzer.getInstance().getTopic(Hub.class.getName() + ".CreateRemoteRef");
    Topic instantianteRemoteRef   = PerfAnalyzer.getInstance().getTopic(Hub.class.getName() + ".InstantiateRemoteRef");
    Topic registerRemoteRef       = PerfAnalyzer.getInstance().getTopic(Hub.class.getName() + ".RegisterRemoteRef");
    Topic createStub              = PerfAnalyzer.getInstance().getTopic(Hub.class.getName() + ".CreateStub");    
  }
}
