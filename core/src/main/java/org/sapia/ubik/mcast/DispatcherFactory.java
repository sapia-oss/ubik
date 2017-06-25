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
   * @param context a {@link DispatcherContext}.

   * @throws IOException
   *           if a problem occurs creating the dispatcher.
   */
  public static UnicastDispatcher createUnicastDispatcher(DispatcherContext context) throws IOException {
    String provider = context.getConf().getProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP_NIO);

    UnicastDispatcher dispatcher = loadUnicastDispatcher(provider);
    dispatcher.initialize(context);
    return dispatcher;
  }
  
  /**
   * @param provider the name of the {@link UnicastDispatcher} to load.
   * 
   * @return the {@link UnicastDispatcher} that is not yet initialized.
   * @see Consts#UNICAST_PROVIDER
   */
  public static UnicastDispatcher loadUnicastDispatcher(String provider) {
    log.info("Creating unicast dispatcher %s", provider);
    return Providers.get().load(UnicastDispatcher.class, provider);
  }

  /**
   * Creates a {@link BroadcastDispatcher}, based on the given
   * {@link Properties}, and returns it.
   *
   * @param context a {@link DispatcherContext}.

   * @throws IOException
   *           if a problem occurs creating the dispatcher.
   */
  public static BroadcastDispatcher createBroadcastDispatcher(DispatcherContext context) throws IOException {
    String provider = context.getConf().getProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_UDP);

    BroadcastDispatcher dispatcher = loadBroadcastDispatcher(provider);
    dispatcher.initialize(context);
    return dispatcher;
  }
  
  /**
   * @param provider the name of the {@link BroadcastDispatcher} to load.

   * @return the {@link BroadcastDispatcher} that is not yet initialized.
   * @see Consts#BROADCAST_PROVIDER
   */
  public static BroadcastDispatcher loadBroadcastDispatcher(String provider) {
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
