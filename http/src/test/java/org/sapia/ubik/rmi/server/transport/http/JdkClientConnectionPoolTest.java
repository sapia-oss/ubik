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
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.http.JdkRmiClientConnection.State;
import org.sapia.ubik.util.TimeValue;
import org.sapia.ubik.util.SysClock.MutableClock;

public class JdkClientConnectionPoolTest {

  private static final int READ_TIMEOUT = 100;
  
  private MutableClock            clock;
  private SyncPoint               receiveStartedSyncPoint;
  private SyncPoint               receiveEndedSyncPoint;
  private ExecutorService         executor;
  private JdkClientConnectionPool pool;
  
  @Before
  public void setUp() throws Exception {
    clock                   = new MutableClock();
    receiveStartedSyncPoint = new SyncPoint();
    receiveEndedSyncPoint   = new SyncPoint();
    executor                = Executors.newSingleThreadExecutor();
    
    JdkRmiClientConnection.JdkRmiClientConnectionFactory factory = () -> { 
      JdkRmiClientConnection connection = new JdkRmiClientConnection() {
        
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
      connection.setReadTimeout(TimeValue.createMillis(READ_TIMEOUT));
      return connection;
    };
    
    pool = new JdkClientConnectionPool(HttpAddress.newDefaultInstance("test", 8888), factory);
  }
  
  @After
  public void tearDown() {
    pool.clear();
    executor.shutdownNow();
  }

  @Test
  public void testAcquire() throws Exception {
    RmiConnection conn = pool.acquire();
    
    assertThat(pool.isActive(conn)).isTrue();
    assertThat(pool.getInternalPool().getAvailableCount()).isEqualTo(0);
    assertThat(pool.getInternalPool().getBorrowedCount()).isEqualTo(1);
  }

  @Test
  public void testClear() throws Exception {
    JdkRmiClientConnection conn = (JdkRmiClientConnection) pool.acquire();
    pool.release(conn);
    pool.clear();
    
    assertThat(conn.isClosed()).isTrue();
    assertThat(pool.isActive(conn)).isFalse();
    assertThat(pool.getInternalPool().getAvailableCount()).isEqualTo(0);
    assertThat(pool.getInternalPool().getBorrowedCount()).isEqualTo(0);
  }

  @Test
  public void testRelease() throws Exception {
    RmiConnection conn = pool.acquire();
    pool.release(conn);
    
    assertThat(pool.isActive(conn)).isFalse();
    assertThat(pool.getInternalPool().getAvailableCount()).isEqualTo(1);
    assertThat(pool.getInternalPool().getBorrowedCount()).isEqualTo(0);
  }

  @Test
  public void testInvalidate() throws Exception {
    JdkRmiClientConnection conn = (JdkRmiClientConnection) pool.acquire();
    pool.invalidate(conn);

    assertThat(conn.isClosed()).isTrue();
    assertThat(pool.isActive(conn)).isFalse();
    assertThat(pool.getInternalPool().getAvailableCount()).isEqualTo(0);
    assertThat(pool.getInternalPool().getBorrowedCount()).isEqualTo(0);
  }

  @Test
  public void testTerminateTimedOutConnections_with_timed_out_connection() throws Exception {
    JdkRmiClientConnection conn = (JdkRmiClientConnection) pool.acquire();

    executor.execute(() ->  { 
      try {
        conn.send("test"); 
        conn.receive();
      } catch (Exception e) {
      
      }
    });
    
    receiveStartedSyncPoint.await();
    assertThat(conn.getState()).isEqualTo(State.READ);
    clock.increaseCurrentTimeMillis(READ_TIMEOUT + 1);
    pool.terminateTimedOutConnections();
    
    assertThat(conn.isClosed()).isTrue();
    assertThat(pool.isActive(conn)).isFalse();
    assertThat(pool.getInternalPool().getAvailableCount()).isEqualTo(0);
    assertThat(pool.getInternalPool().getBorrowedCount()).isEqualTo(0);
  }
  
  @Test
  public void testTerminateTimedOutConnections_within_timeout() throws Exception {
    JdkRmiClientConnection conn = (JdkRmiClientConnection) pool.acquire();

    executor.execute(() ->  { 
      try {
        conn.send("test"); 
        conn.receive();
      } catch (Exception e) {
      
      }
    });
    
    receiveStartedSyncPoint.await();
    assertThat(conn.getState()).isEqualTo(State.READ);
    clock.increaseCurrentTimeMillis(READ_TIMEOUT);
    pool.terminateTimedOutConnections();
    
    assertThat(conn.isClosed()).isFalse();
    assertThat(pool.isActive(conn)).isTrue();
    assertThat(pool.getInternalPool().getAvailableCount()).isEqualTo(0);
    assertThat(pool.getInternalPool().getBorrowedCount()).isEqualTo(1);
  }

}
