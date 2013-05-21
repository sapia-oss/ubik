package org.sapia.ubik.rmi.naming.remote;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InitialContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.PropertiesUtil;
import org.sapia.ubik.util.Props;

public class JNDIServerFailoverTest {

	private EmbeddableJNDIServer server;
	private AtomicInteger counter = new AtomicInteger();
	
	@Before
	public void setUp() throws Exception {
	  PropertiesUtil.clearUbikSystemProperties();
		System.setProperty(Consts.UBIK_DOMAIN_NAME, "ubik.test");
		System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "false");
		System.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
		System.setProperty(Consts.BROADCAST_MEMORY_NODE, "test");
		server = new EmbeddableJNDIServer(new EventChannel("ubik.test", Props.getSystemProperties()), 1099);
		server.start(true);
	}

	@After
	public void tearDown() throws Exception {
    PropertiesUtil.clearUbikSystemProperties();
		server.stop();
		Hub.shutdown();
	}	
	
	@Test
	public void testFailover() throws Exception {


		Hashtable props = new Hashtable();
		props.put(InitialContext.PROVIDER_URL, "ubik://" + Localhost.getAnyLocalAddress().getHostAddress() +":1099/");
		props.put(InitialContext.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
		InitialContext context = new InitialContext(props);  
	
		try {
  		FailoverService downService = mock(FailoverService.class);
  		doAnswer(new Answer<Void>() {
  			@Override
  			public Void answer(InvocationOnMock invocation) throws Throwable {
  				if(counter.incrementAndGet() == 2) {
  					throw new RemoteException();
  				}
  			  return null;
  			}
			}).when(downService).invoke();
  		context.bind("service", Hub.exportObject(downService));
  		
  		
  		FailoverService downServiceStub = (FailoverService)context.lookup("service");
  		downServiceStub.invoke();
  		downServiceStub.invoke();
  		verify(downService, times(3)).invoke();
		} finally {
			context.close();
		}
	}	
	
	public static interface FailoverService {
		
		public void invoke() throws RemoteException;
		
	}
	
	public class TestFailoverService implements FailoverService {
		public void invoke() throws RemoteException {
			if(counter.incrementAndGet() == 2) {
				throw new RemoteException();
			}
		}
	}
}
