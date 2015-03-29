package org.sapia.ubik.rmi.examples.camel;

import org.apache.log4j.BasicConfigurator;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.amqp.AmqpConsts;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class CamelAmqpEventChannel {

  
  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();
    Log.setDebug();
    final EventChannel channel = new EventChannel("camel-example", 
        Conf.newInstance()
          .addProperties(
              Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_AMQP,
              Consts.BROADCAST_CAMEL_ENDPOINT_URI, "amqp:topic:ubik_event_channel",
              AmqpConsts.BROADCAST_AMQP_CONNECTION_URL, "amqp://guest:guest@localhost:5672"
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
