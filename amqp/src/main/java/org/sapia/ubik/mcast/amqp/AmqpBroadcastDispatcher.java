package org.sapia.ubik.mcast.amqp;

import java.net.MalformedURLException;

import javax.jms.ConnectionFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;
import org.apache.qpid.amqp_1_0.jms.impl.ConnectionFactoryImpl;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.camel.DefaultCamelBroadcastDispatcher;

/**
 * Extends the {@link DefaultCamelBroadcastDispatcher} class by internally creating a {@link ConnectionFactory} and
 * registering it with Camel's {@link Registry}.
 * @author yduchesne
 *
 */
public class AmqpBroadcastDispatcher extends DefaultCamelBroadcastDispatcher {
  
  private static final String AMQP_PROPERTY_PREFIX   = PROPERTY_PREFIX + ".amqp";
  private static final String REF_CONNECTION_FACTORY = "amqpConnectionFactory";

  private ConnectionFactoryImpl connections;
  
  @Override
  public void initialize(DispatcherContext context) {
    context.getConf().addProperties(AMQP_PROPERTY_PREFIX + ".option.connectionFactory", "#" + REF_CONNECTION_FACTORY);
    try {
      connections = ConnectionFactoryImpl.createFromURL(context.getConf().getNotNullProperty(AmqpConsts.BROADCAST_AMQP_CONNECTION_URL));
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Could not create connection factory", e);
    }
    super.initialize(context);
  }
 
  @Override
  protected void doInitializeContext(CamelContext context) {
    Registry registry = (Registry) context.getRegistry();
    SimpleRegistry sreg = null;
    if (registry instanceof SimpleRegistry) {
      sreg = (SimpleRegistry) registry;
    } else if (registry instanceof PropertyPlaceholderDelegateRegistry) {
      PropertyPlaceholderDelegateRegistry preg = (PropertyPlaceholderDelegateRegistry) registry;
      if (preg.getRegistry() instanceof SimpleRegistry) {
        sreg = (SimpleRegistry) preg.getRegistry();
      }
    }
    
    if (sreg != null) {
      sreg.put(REF_CONNECTION_FACTORY, connections);
    } else {
      throw new IllegalStateException("Expected instance of SimpleRegistry on CamelContext, got: " 
          + context.getRegistry().getClass().getName());
    }
  }
  
}
