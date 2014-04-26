package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.Spawn;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.concurrent.ThreadStartup;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelRef;
import org.sapia.ubik.mcast.EventChannelStateListener;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.UrlMaker;
import org.sapia.ubik.rmi.naming.remote.archie.SyncPutEvent;
import org.sapia.ubik.rmi.naming.remote.archie.UbikRemoteContext;
import org.sapia.ubik.rmi.naming.remote.proxy.LocalContext;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.stub.StubContainer;
import org.sapia.ubik.rmi.server.transport.mina.MinaAddress;
import org.sapia.ubik.rmi.server.transport.mina.MinaTransportProvider;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Localhost;

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
  private UbikRemoteContext root;
  private Context local;
  private ThreadStartup startBarrier = new ThreadStartup();
  private long syncInterval = Conf.newInstance().getLongProperty(Consts.JNDI_SYNC_MAX_COUNT, Defaults.DEFAULT_JNDI_SYNC_INTERVAL);
  private int syncMaxCount  = Conf.newInstance().getIntProperty(Consts.JNDI_SYNC_MAX_COUNT, Defaults.DEFAULT_JNDI_SYNC_MAX_COUNT);

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
    Assertions.illegalState(channel == null, "Event channel not initialized (server probably not started)");

    return channel.get();
  }

  /**
   * Handles {@link JNDIConsts#JNDI_CLIENT_PUBLISH} remote events. Dispatches
   * {@link JNDIConsts#JNDI_SERVER_DISCO} remote events in response.
   */
  @Override
  public void onAsyncEvent(final RemoteEvent evt) {
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
    } else if (evt.getType().equals(JndiSyncRequest.class.getName())) {
      Spawn.run(new Runnable() {
        @Override
        public void run() {
          try {
            handleJndiSyncRequest(evt);
          } catch (IOException e) {
            log.warning("Could not deserialize remote event data", e);
          }
        }
      });
    }
  }

  private void handleJndiSyncRequest(RemoteEvent evt) throws IOException {
    JndiSyncVisitor visitor = new JndiSyncVisitor();
    root.accept(visitor);
    JndiSyncRequest req = (JndiSyncRequest) evt.getData();
    Map<String, Integer> diff = req.diff(visitor.asMap());
    // checking count difference: if count < 0, means other instance is missing values.
    // if count > 0, means this instance is missing values
    for (Map.Entry<String, Integer> e : diff.entrySet()) {
      if (e.getValue() < 0) {
        try {
          Name path = root.getDelegate().getNameParser().parse(e.getKey());
          NamePart name = path.last();
          SyncPutEvent put = new SyncPutEvent(path.getTo(path.count() - 1), name, root.lookup(e.getKey()), true);
          channel.get().dispatch(SyncPutEvent.class.getName(), put);
        } catch (Exception err) {
          log.warning("Could not send event: %", err, SyncPutEvent.class.getName());
        }
      } else if (e.getValue() > 0) {
        try {
          channel.get().dispatch(evt.getUnicastAddress(), JndiSyncRequest.class.getName(), JndiSyncRequest.newInstance(visitor.asMap()));
        } catch (IOException err) {
          log.warning("Could not send event %s to %s", err, JndiSyncRequest.class.getName(), evt.getUnicastAddress());
        }
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
    Assertions.illegalState(root == null, "Context not initialized (server probably not started)");
    return root;
  }

  /**
   * @return this instance's {@link RemoteContext}.
   */
  @Override
  public RemoteContext getRemoteContext() {
    Assertions.illegalState(root == null, "Context not initialized (server probably not started)");
    return root;
  }

  /**
   * @return a {@link LocalContext}, which will internally convert {@link StubContainer}s bound to this instance to their stubs.
   */
  public Context getLocalContext() {
    Assertions.illegalState(root == null, "Context not initialized (server probably not started)");
    if (local == null) {
      try {
        local = new EmbeddedLocalContext(channel, UrlMaker.makeLookupBaseUrl(port), getRemoteContext());
      } catch (Exception e) {
        throw new IllegalStateException("Could not create local context", e);
      }
    }
    return local;
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

  private final void doRun() {
    try {
      log.warning("Starting JNDI server on port %s, domain %s", port, domain);

      channel.get().registerAsyncListener(JNDIConsts.JNDI_CLIENT_PUBLISH, this);
      channel.get().registerAsyncListener(JndiSyncRequest.class.getName(), this);
      channel.start();

      TCPAddress address = new MinaAddress(Localhost.getPreferredLocalAddress().getHostAddress(), port);

      UbikRemoteContext context = UbikRemoteContext.newInstance(channel);
      root = context;

      Properties props = new Properties();
      props.setProperty(Consts.TRANSPORT_TYPE, MinaTransportProvider.TRANSPORT_TYPE);
      props.setProperty(MinaTransportProvider.BIND_ADDRESS, address.getHost());
      props.setProperty(MinaTransportProvider.PORT, Integer.toString(address.getPort()));
      Hub.exportObject(this, props);

      log.warning("JNDI Server started. Listening on %s:%s", address.getHost(), address.getPort());
      Hub.getModules().getTaskManager().addTask(new TaskContext("", syncInterval), new Task() {
        private int execCount;
        @Override
        public void exec(TaskContext ctx) {
          JndiSyncVisitor visitor = new JndiSyncVisitor();
          root.accept(visitor);
          JndiSyncRequest request = JndiSyncRequest.newInstance(visitor.asMap());
          try {
            log.debug("Dispatching sync request");
            channel.get().dispatch(JndiSyncRequest.class.getName(), request);
          } catch (IOException e) {
            log.warning("Could not dispatch event %s", e, JndiSyncRequest.class.getName());
          }
          execCount++;
          if (syncMaxCount > 0 && execCount >= syncMaxCount) {
            ctx.abort();
          }
        }
      });

      startBarrier.started();

      channel.get().dispatch(JNDIConsts.JNDI_SERVER_PUBLISH, address);

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
