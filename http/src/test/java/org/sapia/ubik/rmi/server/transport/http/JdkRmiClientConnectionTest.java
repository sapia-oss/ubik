package org.sapia.ubik.rmi.server.transport.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.SyncPoint;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.http.JdkRmiClientConnection.State;
import org.sapia.ubik.util.SysClock.MutableClock;
import org.sapia.ubik.util.TimeValue;

public class JdkRmiClientConnectionTest {
  
  private MutableClock           clock;
  private JdkRmiClientConnection connection;
  private SyncPoint              receiveStartedSyncPoint;
  private SyncPoint              receiveEndedSyncPoint;
  private ExecutorService        executor;
  
  @Before
  public void setUp() throws Exception {
    clock                   = new MutableClock();
    receiveStartedSyncPoint = new SyncPoint();
    receiveEndedSyncPoint   = new SyncPoint();
    executor                = Executors.newSingleThreadExecutor();
    connection              = new JdkRmiClientConnection() {
      
      @Override
      protected void doClose() {
        receiveEndedSyncPoint.notifyCompletion();
      }
      
      @Override
      protected Object doReceive() throws IOException, ClassNotFoundException, RemoteException {
        try {
          receiveStartedSyncPoint.notifyCompletion();
          receiveEndedSyncPoint.await();
        } catch (InterruptedException e) {
          throw new RemoteException("Thread interrupted");
        }
        return null;
      }
      
      @Override
      protected Object doReceive(long timeout)
          throws IOException, ClassNotFoundException, RemoteException, SocketTimeoutException {
        return doReceive();
      }
      
      @Override
      protected void doSend(Object o, VmId associated, String transportType) throws IOException, RemoteException {
      }   
    };
    connection.setClock(clock);
    connection.setReadTimeout(TimeValue.createMillis(100));
  }
  
  @After
  public void tearDown() {
    connection.close();
    executor.shutdownNow();
  }

  @Test
  public void testIsInReadTimeout_with_timed_out_connection() throws Exception {
    executor.execute(() ->  { 
        try {
          connection.send("test"); 
          connection.receive();
        } catch (Exception e) {
        
        }
    });
    
    receiveStartedSyncPoint.await();
    assertThat(connection.getState()).isEqualTo(State.READ);
    clock.increaseCurrentTimeMillis(101);
    assertThat(connection.isInReadTimeout()).isTrue();
  }
  
  @Test
  public void testIsInReadTimeout_within_timeout() throws Exception {
    executor.execute(() ->  { 
        try {
          connection.send("test"); 
          connection.receive();
        } catch (Exception e) {
        
        }
    });
    
    receiveStartedSyncPoint.await();
    assertThat(connection.getState()).isEqualTo(State.READ);
    clock.increaseCurrentTimeMillis(100);
    assertThat(connection.isInReadTimeout()).isFalse();
  }

}
