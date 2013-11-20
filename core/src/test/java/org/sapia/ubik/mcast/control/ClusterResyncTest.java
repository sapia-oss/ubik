package org.sapia.ubik.mcast.control;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Clock;
import org.sapia.ubik.util.Collections2;

public class ClusterResyncTest {

  private EventChannelController controller;
  private ChannelCallback callback;
  private Clock.MutableClock clock;
  private ServerAddress serverAddress;
  private ControllerConfiguration config;

  @Before
  public void setUp() throws IOException, InterruptedException {
    callback = mock(ChannelCallback.class);
    clock = Clock.MutableClock.getInstance();
    config = new ControllerConfiguration();
    config.setResyncInterval(1000);
    config.setHeartbeatInterval(1000);
    config.setResponseTimeout(100);
    controller = new EventChannelController(clock, config, callback);
    serverAddress = mock(ServerAddress.class);

    when(callback.getNodes()).thenReturn(new HashSet<String>());
    when(callback.getAddress()).thenReturn(serverAddress);
    when(callback.getNode()).thenReturn("testNode");
    when(callback.sendSynchronousRequest(anySet(), any(SynchronousControlRequest.class))).thenReturn(new HashSet<SynchronousControlResponse>());

  }

  @Test
  public void testResync() {
    controller.checkStatus();
    clock.increaseCurrentTimeMillis(1001);
    controller.checkStatus();
    verify(callback).resync();
  }

  @Test
  public void testResyncNodeCountThresholdReached() {
    controller.checkStatus();
    config.setResyncNodeCount(1);
    when(callback.getNodes()).thenReturn(Collections2.arrayToSet("test"));
    clock.increaseCurrentTimeMillis(1001);
    controller.checkStatus();
    verify(callback).resync();
  }

  @Test
  public void testResyncNotRetriedBeforeTimeout() {
    controller.checkStatus();
    clock.increaseCurrentTimeMillis(1001);
    controller.checkStatus();
    controller.checkStatus();
    verify(callback).resync();
  }

  @Test
  public void testResyncRetriedAfterTimeout() {
    controller.checkStatus();
    clock.increaseCurrentTimeMillis(1001);
    controller.checkStatus();
    clock.increaseCurrentTimeMillis(1001);
    controller.checkStatus();
    verify(callback, times(2)).resync();
  }
}
