package org.sapia.ubik.rmi.examples;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.InitialContext;

import org.sapia.ubik.rmi.naming.remote.JndiConsts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;

public class CustomMulticastBind implements Foo, Bar{
  
  @Override
  public Bar getBar() throws RemoteException {
    return this;
  }
  
  @Override
  public String getMsg() throws RemoteException {
    return "Hello World";
  }
  
  public static void main(String[] args) throws Exception{
    
    Properties props = new Properties();
    props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099");
    props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
    props.setProperty(JndiConsts.MCAST_ADDR_KEY, CustomMulticastJndiRunner.CUSTOM_MCAST_ADDRESS);
    props.setProperty(JndiConsts.MCAST_PORT_KEY, Integer.toString(CustomMulticastJndiRunner.CUSTOM_MCAST_PORT));
    InitialContext       ctx = new InitialContext(props);
    ctx.bind("foo", new CustomMulticastBind());
    
    while(true) {
      Thread.sleep(100000);
    }
    
  }

}
