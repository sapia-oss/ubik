package org.sapia.ubik.rmi.examples.camel;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.camel.CamelBroadcastDispatcherSupport;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class CamelRabbitMqEventChannel {

  
  public static void main(String[] args) throws Exception {
    final EventChannel channel = new EventChannel("camel-example", 
        Conf.newInstance()
          .addProperties(
              Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_CAMEL,
              Consts.BROADCAST_CAMEL_ENDPOINT_URI, "rabbitmq://localhost/ubik/eventchannel",
              CamelBroadcastDispatcherSupport.PROPERTY_PREFIX + ".rabbitmq.option.exchangeType", "topic",
              CamelBroadcastDispatcherSupport.PROPERTY_PREFIX + ".rabbitmq.option.autoDelete", "true",
              CamelBroadcastDispatcherSupport.PROPERTY_PREFIX + ".rabbitmq.option.durable", "false",
              CamelBroadcastDispatcherSupport.PROPERTY_PREFIX + ".rabbitmq.option.routingKey", "org.sapia.ubik.mcast.RemoteEvent",
              CamelBroadcastDispatcherSupport.PROPERTY_PREFIX + ".rabbitmq.header.rabbitmq.ROUTING_KEY", "org.sapia.ubik.mcast.RemoteEvent"
          )
    );
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        channel.close();
      }
    });
    
    channel.start();
    
    while (true) {
      Thread.sleep(10000);
    }
    
  }
}
