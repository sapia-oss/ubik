package org.sapia.ubik.mcast.camel;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.impl.DefaultMessage;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.testing.BroadcastDispatcherTestSupport;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class CamelBroadcastDispatcherSupportTest extends BroadcastDispatcherTestSupport {

  @Override
  protected BroadcastDispatcher createDispatcher(EventConsumer consumer)
      throws IOException {
  
    CamelBroadcastDispatcherSupport dispatcher = new CamelBroadcastDispatcherSupport() {
      @Override
      public void removeConnectionStateListener(ConnectionStateListener listener) {
      }
      
      @Override
      protected RemoteEvent doReadEventFrom(Exchange incoming) {
        return incoming.getIn().getBody(RemoteEvent.class);
      }
      
      @Override
      protected Exchange doCreateExchangeFor(RemoteEvent outgoing) {
        Message msg = new DefaultMessage();
        msg.setBody(outgoing, RemoteEvent.class);
        return ExchangeBuilder.anExchange(context()).withBody(outgoing).withPattern(ExchangePattern.InOnly).build();
      }
    };
    
    dispatcher.initialize(consumer, Conf.newInstance().addProperties(Consts.BROADCAST_CAMEL_ENDPOINT_URI, "vm:in?multipleConsumers=true"));
    
    return dispatcher;
  }
}
