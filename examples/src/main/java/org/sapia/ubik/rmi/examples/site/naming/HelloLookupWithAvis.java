package org.sapia.ubik.rmi.examples.site.naming;

import java.util.Properties;

import javax.naming.InitialContext;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;

public class HelloLookupWithAvis {

	public static void main(String[] args) {
	
    try{
      Properties props = new Properties();
      
      props.setProperty(InitialContext.PROVIDER_URL, 
              "ubik://localhost:1099/");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
              RemoteInitialContextFactory.class.getName());
      
      props.setProperty(Consts.BROADCAST_PROVIDER, 
      				Consts.BROADCAST_PROVIDER_AVIS);
      props.setProperty(Consts.BROADCAST_AVIS_URL, "elvin://localhost");
      
      InitialContext context = new InitialContext(props);
      
      Hello hello = (Hello)context.lookup("server/hello");
      
      // do not forget...
      context.close();
      
      System.out.println(hello.getMessage());
    }catch(Exception e){
      e.printStackTrace();
    }
  }		
}
