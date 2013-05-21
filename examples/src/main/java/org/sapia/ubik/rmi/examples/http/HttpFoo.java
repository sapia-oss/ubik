package org.sapia.ubik.rmi.examples.http;

import java.util.Properties;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.examples.UbikFoo;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.server.transport.http.HttpConsts;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpFoo {
  public static void main(String[] args) {
    try {
      Hub.getModules().getTransportManager().registerProvider(new HttpTransportProvider());
			Log.setDebug();
      Properties props = new Properties();
      props.setProperty(Consts.TRANSPORT_TYPE,
        HttpConsts.HTTP_TRANSPORT_TYPE);
      props.setProperty(HttpConsts.HTTP_PORT_KEY, "8080");
      
			// If Ubik server is behind a firewall 
			// at www.somedomain.net, connect to it using
			// as follows (assuming port is 8181; change according to
			// your config)
      //props.setProperty(HttpConsts.SERVER_URL_KEY, "http://www.somedomain.net:8181/ubik");
      
      Hub.exportObject(new UbikFoo(), props);

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
