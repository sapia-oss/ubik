package org.sapia.ubik.mcast;

import java.io.IOException;
import java.util.Properties;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.provider.Providers;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

/**
 * This class implements a factory of {@link UnicastDispatcher}s and
 * {@link BroadcastDispatcher}s.
 *
 * @author yduchesne
 *
 */
public final class DispatcherFactory {

  private static Category log = Log.createCategory(DispatcherFactory.class);

  /**
   * Private constructor.
   */
  private DispatcherFactory() {
  }

  /**
   * Creates a {@link UnicastDispatcher}, based on the given {@link Properties},
   * and returns it.
   *
   * @param consumer
   *          an {@link EventConsumer}.
   * @param props
   *          the {@link Conf} containing configuration values.
   * @return the {@link BroadcastDispatcher} corresponding to the given
   *         properties.
   * @throws IOException
   *           if a problem occurs creating the dispatcher.
   */
  public static UnicastDispatcher createUnicastDispatcher(EventConsumer consumer, Conf props) throws IOException {
    UnicastDispatcher dispatcher = loadUnicastDispatcher(props);
    dispatcher.initialize(consumer, props);
    return dispatcher;
  }

  /**
   * @param props a {@link Conf} instance containing configuration values.
   * @return the {@link UnicastDispatcher} that is configured.
   * @see Consts#UNICAST_PROVIDER
   */
  public static UnicastDispatcher loadUnicastDispatcher(Conf props) {
    String provider = props.getProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP_NIO);
    log.info("Creating unicast dispatcher %s", provider);
    return Providers.get().load(UnicastDispatcher.class, provider);
  }

  /**
   * Creates a {@link BroadcastDispatcher}, based on the given
   * {@link Properties}, and returns it.
   *
   * @param consumer
   *          an {@link EventConsumer}.
   * @param props
   *          the {@link Conf} containing configuration values.
   * @return the {@link BroadcastDispatcher} corresponding to the given
   *         properties.
   * @throws IOException
   *           if a problem occurs creating the dispatcher.
   */
  public static BroadcastDispatcher createBroadcastDispatcher(EventConsumer consumer, Conf props) throws IOException {
    BroadcastDispatcher dispatcher = loadBroadcastDispatcher(props);
    dispatcher.initialize(consumer, props);
    return dispatcher;
  }
  
  /**
   * @param props a {@link Conf} instance containing configuration values.
   * @return the {@link BroadcastDispatcher} that is configured.
   * @see Consts#BROADCAST_PROVIDER
   */
  public static BroadcastDispatcher loadBroadcastDispatcher(Conf props) {
    String provider = props.getProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_UDP);
    log.info("Creating broadcast dispatcher %s", provider);
    return Providers.get().load(BroadcastDispatcher.class, provider);
  }

  /**
   * @param from
   *          the {@link Properties} from which to construct a
   *          {@link MulticastAddress}.
   * @return the {@link MulticastAddress} that was created from the given
   *         {@link Properties}.
   */
  public static MulticastAddress getMulticastAddress(Properties from) {
    Conf props = new Conf().addProperties(from);
    String provider = props.getProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_UDP);
    BroadcastDispatcher dispatcher = Providers.get().load(BroadcastDispatcher.class, provider);
    return dispatcher.getMulticastAddressFrom(props);
  }

}
