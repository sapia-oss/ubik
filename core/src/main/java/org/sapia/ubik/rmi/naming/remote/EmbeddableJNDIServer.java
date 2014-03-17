package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.concurrent.ThreadStartup;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelRef;
import org.sapia.ubik.mcast.EventChannelStateListener;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.archie.UbikRemoteContext;
import org.sapia.ubik.rmi.naming.remote.archie.UbikRemoteContext.BindListener;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.stub.RemoteRefStateless;
import org.sapia.ubik.rmi.server.stub.StubContainer;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.Stubs;
import org.sapia.ubik.rmi.server.stub.enrichment.StubEnrichmentStrategy.JndiBindingInfo;
import org.sapia.ubik.rmi.server.transport.mina.MinaAddress;
import org.sapia.ubik.rmi.server.transport.mina.MinaTransportProvider;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Conf;

/**
 * This class implements an embeddable JNDI server.
 * <p>
 * Usage:
 *
 * <pre>
 * EmbeddableJNDIServer jndi = new EmbeddableJNDIServer();
 * jndi.start(true);
 * // use it...
 * jndi.stop();
 * </pre>
 *
 * @author Yanick Duchesne
 *
 */
public class EmbeddableJNDIServer implements RemoteContextProvider,
  AsyncEventListener,
  EventChannelStateListener {

  private Category log = Log.createCategory(getClass());
  private String domain;
  private int port;
  private Thread serverThread;
  private EventChannelRef channel;
  private Context root;
  private ThreadStartup startBarrier = new ThreadStartup();

  /**
   * Used this constructor when you want this instance NOT to manage the start/close of the {@link EventChannel}
   * that it uses. In such a case, pass in an {@link EventChannelRef} that you obtain from the
   * {@link EventChannel#getManagedReference()} method.
   *
   * @param ref the {@link EventChannelRef} pointing to the {@link EventChannel} to use..
   * @param port the port to which to bind this server.
   */
  public EmbeddableJNDIServer(EventChannelRef ref, int port) {
    this.channel = ref;
    this.domain = ref.get().getDomainName().toString();
    this.port = port;
  }

  /**
   * Creates an instance of this class that will listen on the default (1099)
   * port, on the "default" domain. Also internally binds its multicast event
   * channel to the default multicast address and port.
   *
   * @see JNDIConsts#DEFAULT_MCAST_ADDR
   * @see JNDIConsts#DEFAULT_MCAST_PORT
   */
  public EmbeddableJNDIServer() {
    this(
        JNDIConsts.DEFAULT_DOMAIN,
        JNDIConsts.DEFAULT_PORT,
        org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_ADDR,
        org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_PORT
    );
  }

  /**
   * Creates an instance of this class that will listen on the given port and
   * domain. Also internally binds its multicast event channel to the default
   * multicast address and port.
   *
   * @see JNDIConsts#DEFAULT_MCAST_ADDR
   * @see JNDIConsts#DEFAULT_MCAST_PORT
   */
  public EmbeddableJNDIServer(String domain, int port) {
    this(domain, port, org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_ADDR, org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_PORT);
  }

  /**
   * Creates an instance of this class that will listen on the given port and
   * domain. Also internally binds its multicast event channel to the given
   * multicast address and port.
   */
  public EmbeddableJNDIServer(String domain, int port, String mcastAddress, int mcastPort) {
    this.domain = domain;
    this.port = port;
    Properties props = new Properties();
    props.setProperty(JNDIConsts.MCAST_ADDR_KEY, mcastAddress);
    props.setProperty(JNDIConsts.MCAST_PORT_KEY, Integer.toString(mcastPort));
    try {
      channel = new EventChannel(domain, new Conf().addProperties(props).addSystemProperties()).getReference();
    } catch (IOException e) {
      throw new IllegalStateException("Could not build event channel", e);
    }
  }

  /**
   * @param ec
   *          the {@link EventChannel} that this instance should wrap.
   * @param port
   *          the port to which to bind the JNDI server.
   */
  public EmbeddableJNDIServer(EventChannel ec, int port) {
    this(ec.getReference(), port);
  }

  /**
   * Returns the event channel that this instance uses to broacast events and
   * subscribe to notifications.
   *
   * @return this instance's {@link EventChannel}.
   */
  public EventChannel getEventChannel() {
    Assertions.illegalState(channel == null, "Event channel not initialized");

    return channel.get();
  }

  /**
   * Handles {@link JNDIConsts#JNDI_CLIENT_PUBLISH} remote events. Dispatches
   * {@link JNDIConsts#JNDI_SERVER_DISCO} remote events in response.
   */
  @Override
  public void onAsyncEvent(RemoteEvent evt) {
    if (evt.getType().equals(JNDIConsts.JNDI_CLIENT_PUBLISH)) {
      try {
        channel.get().dispatch(
            evt.getUnicastAddress(),
            JNDIConsts.JNDI_SERVER_DISCO,
            new MinaAddress(Localhost.getPreferredLocalAddress().getHostAddress(), port)
        );
      } catch (Exception e) {
        log.warning("Could not dispatch JNDI server publishing event", e);
      }
    }
  }

  @Override
  public void onUp(EventChannelEvent event) {
    try {
      channel.get().dispatch(
          event.getAddress(),
          JNDIConsts.JNDI_SERVER_DISCO,
          new MinaAddress(Localhost.getPreferredLocalAddress().getHostAddress(), port)
      );
    } catch (Exception e) {
      log.warning("Could not dispatch JNDI server publishing event", e);
    }
  }

  @Override
  public void onDown(EventChannelEvent event) {
  }

  /**
   * @return this instance's root JNDI {@link Context}.
   */
  public Context getRootContext() {
    Assertions.illegalState(root == null, "Context not initialized");
    return root;
  }

  /**
   * @return this instance's {@link RemoteContext}.
   */
  @Override
  public RemoteContext getRemoteContext() {
    Assertions.illegalState(root == null, "Context not initialized");
    return (RemoteContext) root;
  }

  /**
   * Stops this instance (this internally closes the <code>EventChannel</code>
   * that this instance holds).
   */
  public void stop() {
    ThreadShutdown.create(serverThread).shutdownLenient();
    if (channel != null) {
      channel.close();
    }
  }

  /**
   * Starts this instance and blocks indefinitely.
   *
   * @param daemon
   *          if <code>true</code>, this instance will be started as a daemon
   *          thread.
   */
  public void start(boolean daemon) throws Exception {
    serverThread = NamedThreadFactory.createWith("ubik.jndi.server@" + domain).setDaemon(true).newThread(new Runnable() {
      @Override
      public void run() {
        doRun();
      }
    });
    serverThread.start();
    startBarrier.await();
  }

  /**
   *  This method insures that objects bound to the JNDI tree are indeed stubs (it performs the conversion
   *  to stub if required). This allows using this instance in-JVM without having to do stub conversion
   *  calling {@link Hub#exportObject(Object)} with the object intended for binding to the JNDI beforehand.
   */
  private Object makeStub(Name name, Object toBind) throws RemoteException, IOException {
    Object remote;

    // Object is already in storable form.
    if (toBind instanceof StubContainer) {
      log.debug("Object %s already transformed to StubContainer, so not making stub", name);
      return toBind;
    }

    // Object not already a stub, do stubbing
    if (!Stubs.isStub(toBind)) {
      log.debug("Making stub for object %s", name);
      remote = Hub.exportObject(toBind);

    // Object is a stub,  using as is.
    } else {
      log.debug("Object %s already a stub", name);
      remote = toBind;
    }

    // performing stub enrichment
    log.debug("Performing stub enrichment for object %s", name);
    String          baseUrl = "ubik://" + Localhost.getPreferredLocalAddress().getHostAddress() + ":" + port + "/";
    JndiBindingInfo info    = new JndiBindingInfo(baseUrl, name, channel.get().getDomainName(), channel.get().getMulticastAddress());
    remote                  = Hub.getModules().getStubProcessor().enrichForJndiBinding(remote, info);

    // converting to StubContainer for storage in JNDI
    if (Stubs.isStub(remote)) {
      StubInvocationHandler handler = Stubs.getStubInvocationHandler(remote);
      log.debug("Got stub invocation handler %s for %s", handler, name);
      // stateless stub: register so that it is updated with new
      // endpoints appearing on the network

      if (handler instanceof RemoteRefStateless) {
        log.debug("Registering %s named %s with stateless stub table", handler, name);
        RemoteRefStateless ref = (RemoteRefStateless) handler;
        Hub.getModules().getStatelessStubTable().registerStatelessRef(ref, ref.getContexts());
      }
      return handler.toStubContainer(remote);
    }
    return remote;
  }

  private final void doRun() {
    try {
      log.warning("Starting JNDI server on port %s, domain %s", port, domain);

      channel.get().registerAsyncListener(JNDIConsts.JNDI_CLIENT_PUBLISH, this);
      channel.start();

      TCPAddress address = new MinaAddress(Localhost.getPreferredLocalAddress().getHostAddress(), port);

      UbikRemoteContext context = UbikRemoteContext.newInstance(channel);
      context.addBindListener(new BindListener() {
        @Override
        public Object onBind(Name name, Object toExport) throws NamingException {
          try {
            return makeStub(name, toExport);
          } catch (Exception e) {
            log.error("Could not make stub for object %s (name: %s)", e, toExport, name.toString());
            NamingException nae = new NamingException("Could not make stub for object " + name);
            nae.setRootCause(e);
            throw nae;
          }
        }
      });
      root = context;

      Properties props = new Properties();
      props.setProperty(Consts.TRANSPORT_TYPE, MinaTransportProvider.TRANSPORT_TYPE);
      props.setProperty(MinaTransportProvider.BIND_ADDRESS, address.getHost());
      props.setProperty(MinaTransportProvider.PORT, Integer.toString(address.getPort()));
      Hub.exportObject(this, props);

      channel.get().dispatch(JNDIConsts.JNDI_SERVER_PUBLISH, address);

      log.warning("JNDI Server started");

      startBarrier.started();

      while (true) {
        Thread.sleep(Integer.MAX_VALUE);
      }
    } catch (InterruptedException e) {
      log.warning("Thread interrupted - shutting down JNDI server: " + port + ", " + domain);
      startBarrier.failed(e);
    } catch (Exception e) {
      log.error("Could not start JNDI server", e);
      startBarrier.failed(e);
    }
  }

}
