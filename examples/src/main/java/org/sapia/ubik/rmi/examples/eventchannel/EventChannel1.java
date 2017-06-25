package org.sapia.ubik.rmi.examples.eventchannel;

import java.io.IOException;

import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelStateListener;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.RemoteEvent;

public class EventChannel1 {

  public static void main(String[] args) throws Exception {
    EventChannel channel = new EventChannel("test");
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        channel.close();
      }
    });
    
    channel.addEventChannelStateListener(new EventChannelStateListener() {
      @Override
      public void onDown(EventChannelEvent event) {
        System.out.println("Peer is down: " + event.getNode() + ":" + event.getAddress());
      }
      @Override
      public void onHeartbeatRequest(EventChannelEvent event) {
      }
      @Override
      public void onHeartbeatResponse(EventChannelEvent event) {
      }
      @Override
      public void onLeft(EventChannelEvent event) {
        System.out.println("Peer has left: " + event.getNode() + ":" + event.getAddress());
        System.out.println("View:");
        for (NodeInfo peer : channel.getView().getNodeInfos()) {
          System.out.println(peer.getNode() + ":" + peer.getAddr());
        }
      }
      @Override
      public void onUp(EventChannelEvent event) {
        System.out.println("Peer is up: " + event.getNode() + ":" + event.getAddress());
        System.out.println("View:");
        for (NodeInfo peer : channel.getView().getNodeInfos()) {
          System.out.println(peer.getNode() + ":" + peer.getAddr());
        }
      }
    });
    
    channel.registerAsyncListener("messageEvent", new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        try {
          System.out.println("Event received: " + evt.getData());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    
    channel.start();
    
    while (true) {
      Thread.sleep(1000);
      channel.dispatch("messageEvent", "Hello from " + channel.getNode() + ":" + channel.getUnicastAddress());
    }
    
  }
}
