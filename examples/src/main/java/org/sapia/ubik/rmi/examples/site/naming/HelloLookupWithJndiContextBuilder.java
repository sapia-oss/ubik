package org.sapia.ubik.rmi.examples.site.naming;

import javax.naming.Context;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.JNDIContextBuilder;

public class HelloLookupWithJndiContextBuilder {

	public static void main(String[] args) {
	
    try{
    	
    	Context context = JNDIContextBuilder.newInstance()
    			.property(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_AVIS)
    			.property(Consts.BROADCAST_AVIS_URL, "elvin://localhost")
    			.build();
      
      Hello hello = (Hello)context.lookup("server/hello");
      
      // do not forget...
      context.close();
      
      System.out.println(hello.getMessage());
    }catch(Exception e){
      e.printStackTrace();
    }
  }		
}
