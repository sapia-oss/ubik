package org.sapia.ubik.mcast.tcp.netty;

import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.tcp.BaseTcpUnicastDispatcher;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Time;

/**
 * Netty-based implementationf of the {@link UnicastDispatcher} interface.
 * 
 * @author yduchesne
 * 
 */
public class NettyTcpUnicastDispatcher extends BaseTcpUnicastDispatcher implements NettyMcastConsts {

  private Category log = Log.createCategory(getClass());
  private NettyTcpUnicastAddress address;
  private NettyTcpUnicastServer server;
  private int marshallingBufferSize;

  /**
   * @param consumer
   *          the {@link EventConsumer} to which incoming events should be
   *          dispatched.
   * @param the
   *          marshalling buffer size.
   */
  public NettyTcpUnicastDispatcher(EventConsumer consumer, int marshallingBufferSize) {
    super(consumer);
    this.marshallingBufferSize = marshallingBufferSize;

    try {
      log.info("Acquiring network address");
      String host = Localhost.getPreferredLocalAddress().getHostAddress();
      this.address = new NettyTcpUnicastAddress(host, new TcpPortSelector().select(host));
    } catch (Exception e) {
      throw new IllegalStateException("Could not acquire server address", e);
    }

    log.info("Network address acquired");
  }

  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    return address;
  }

  @Override
  protected void doStart() {
    log.info("Starting...");

    Conf config = Conf.getSystemProperties();

    ThreadingConfiguration ioConf = ThreadingConfiguration.newInstance()
        .setCorePoolSize(config.getIntProperty(SERVER_IO_CORE_THREADS_KEY, DEFAULT_SERVER_IO_CORE_THREADS))
        .setMaxPoolSize(config.getIntProperty(SERVER_IO_MAX_THREADS_KEY, DEFAULT_SERVER_IO_MAX_THREADS))
        .setQueueSize(config.getIntProperty(SERVER_IO_QUEUE_SIZE_KEY, DEFAULT_SERVER_IO_QUEUE_SIZE))
        .setKeepAlive(Time.createSeconds(config.getLongProperty(SERVER_IO_KEEP_ALIVE_KEY, DEFAULT_SERVER_IO_KEEP_ALIVE)));

    ThreadingConfiguration workerConf = ThreadingConfiguration.newInstance()
        .setCorePoolSize(config.getIntProperty(SERVER_WORKER_CORE_THREADS_KEY, DEFAULT_SERVER_WORKER_CORE_THREADS))
        .setMaxPoolSize(config.getIntProperty(SERVER_WORKER_MAX_THREADS_KEY, DEFAULT_SERVER_WORKER_MAX_THREADS))
        .setQueueSize(config.getIntProperty(SERVER_WORKER_QUEUE_SIZE_KEY, DEFAULT_SERVER_WORKER_QUEUE_SIZE))
        .setKeepAlive(Time.createSeconds(config.getLongProperty(SERVER_WORKER_KEEP_ALIVE_KEY, DEFAULT_SERVER_WORKER_KEEP_ALIVE)));

    server = new NettyTcpUnicastServer(consumer, address, ioConf, workerConf);
    server.start();
    log.info("Started");
  }

  @Override
  protected String doGetTransportType() {
    return NettyTcpUnicastAddress.TRANSPORT_TYPE;
  }

  @Override
  protected ConnectionFactory doGetConnectionFactory(int soTimeout) {
    return new NettyTcpUnicastConnectionFactory(marshallingBufferSize);
  }

  @Override
  protected void doClose() {
    log.debug("Proceeding to close");
    try {
      if (server != null) {
        log.debug("Closing server connections");
        server.close();
      }
    } finally {
    }
    log.debug("Closed");
  }

}
