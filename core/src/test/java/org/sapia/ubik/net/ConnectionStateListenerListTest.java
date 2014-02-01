package org.sapia.ubik.net;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionStateListenerListTest {
  
  @Mock
  private ConnectionStateListener     listener;
  
  private ConnectionStateListenerList listeners;
  
  @Before
  public void setUp() {
    listeners = new ConnectionStateListenerList();
  }

  @Test
  public void testAddRemove() {
    listeners.add(listener);
    listeners.remove(listener);
    listeners.notifyConnected();
    verify(listener, never()).onConnected();
  }


  @Test
  public void testNotifyConnected() {
    listeners.add(listener);
    listeners.notifyConnected();
    verify(listener).onConnected();    
  }

  @Test
  public void testNotifyReconnected() {
    listeners.add(listener);
    listeners.notifyReconnected();
    verify(listener).onReconnected();
  }

  @Test
  public void testNotifyDisconnected() {
    listeners.add(listener);
    listeners.notifyDisconnected();
    verify(listener).onDisconnected();
  }

}
