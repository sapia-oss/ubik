package org.sapia.ubik.rmi.server.transport.http;

import static org.junit.Assert.assertEquals;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Localhost;

@RunWith(MockitoJUnitRunner.class)
public class RemoteServerTest {

  private RemoteService normal, error;
  
  @Before
  public void setUp() {
    Hub.shutdown();
    System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "false");
    
    normal = new NormalService();
    error  = new ErrorService();    
  }
  @After
  public void tearDown() {
    Hub.shutdown();
    System.clearProperty(Consts.COLOCATED_CALLS_ENABLED);
  }

  @Test
  public void testNormal() throws Exception {
    Properties props = new Properties();
    props.setProperty(Consts.TRANSPORT_TYPE, HttpTransportProvider.TRANSPORT_TYPE);
    props.setProperty(HttpConsts.HTTP_PORT_KEY, "8001");
    Hub.exportObject(normal, props);

    RemoteService service = (RemoteService) Hub.connect(HttpAddress.newDefaultInstance(Localhost.getPreferredLocalAddress().getHostAddress(), 8001));
    
    List<Integer> fetched = new ArrayList<Integer>();
    Iterator<Integer> result = service.iterate();
    while (result.hasNext()) {
      fetched.add(result.next());
    }

    assertEquals(5, fetched.size());
  }
  
  
  @Test(expected = IllegalStateException.class)
  public void testErrorWhileFetching() throws Exception {
    Properties props = new Properties();
    props.setProperty(Consts.TRANSPORT_TYPE, HttpTransportProvider.TRANSPORT_TYPE);
    props.setProperty(HttpConsts.HTTP_PORT_KEY, "8000");
    Hub.exportObject(this.error, props);

    RemoteService service = (RemoteService) Hub.connect(HttpAddress.newDefaultInstance(Localhost.getPreferredLocalAddress().getHostAddress(), 8000));
    
    Iterator<Integer> result = service.iterate();
    while (result.hasNext()) {
      result.next();
    }

  }
  
  public static class NormalService implements RemoteService, Remote {
    @Override
    public Iterator<Integer> iterate() {
      return new RemoteIterator(Collects.arrayToList(0, 1, 2, 3, 4).iterator());
    }
  }

  public static class ErrorService implements RemoteService, Remote {
    
    @Override
    public Iterator<Integer> iterate() {
      Iterator<Integer> iterator = new Iterator<Integer>() {
        
        int count = 0;
        
        @Override
        public boolean hasNext() {
          if (count < 5) {
            return true;
          }
          throw new IllegalStateException("Stop!!!");
        }
        
        @Override
        public Integer next() {
          return count++;
        }
        
        @Override
        public void remove() {
        }
      };
      
      return new RemoteIterator(iterator);
    }
    
  }

}
