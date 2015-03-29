package org.sapia.ubik.mcast.camel;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.ExchangeBuilder;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.McastUtil;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.rmi.Consts;

/**
 * Implements a default {@link BroadcastDispatcher} based on the {@link CamelBroadcastDispatcherSupport}
 * class.
 * <p>
 * This implementation should satisfy most needs. Use in conjunction with appropriate dependencies
 * and configure the endpoint URI accordingly.
 * 
 * @see Consts#BROADCAST_CAMEL_ENDPOINT_URI
 * 
 * @author yduchesne
 *
 */
public class DefaultCamelBroadcastDispatcher extends CamelBroadcastDispatcherSupport {
  
  private static final int BUFSZ = 2048;
  
  @Override
  protected Exchange doCreateExchangeFor(RemoteEvent outgoing) {
    log.debug("Sending event: %s", outgoing.getType());
    try {
      return ExchangeBuilder.anExchange(context())
          .withBody(McastUtil.toBytes(outgoing, BUFSZ))
          .withPattern(ExchangePattern.InOnly)
          .build();
    } catch (IOException e) {
      throw new IllegalStateException("Could not serialize remote event", e);
    }
  }
  
  @Override
  protected RemoteEvent doReadEventFrom(Exchange incoming) {
    try {
      RemoteEvent evt = (RemoteEvent) McastUtil.fromBytes(incoming.getIn().getBody(byte[].class));
      log.debug("Sending event: %s", evt.getType());
      return evt;
    } catch (IOException e) {
      throw new IllegalStateException("Could not deserialize remote event", e);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Class not found when deserializing remote event", e);
    }
  }
}
