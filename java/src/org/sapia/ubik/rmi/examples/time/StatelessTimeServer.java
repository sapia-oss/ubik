package org.sapia.ubik.rmi.examples.time;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Localhost;


/**
 * @
 */
public class StatelessTimeServer {
  private static TimeServiceIF _theTimeService;

  public StatelessTimeServer() {
  }

  public static void main(String[] args) {
    try {
      // Parse the input arguments
      String aJndiUrlProvider    = "ubik://" + Localhost.getLocalAddress().getHostAddress() + ":1099/";
      String aJndiInitialContext = "org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory";

      if (args.length > 0) {
        aJndiUrlProvider = args[0];

        if (args.length > 1) {
          aJndiInitialContext = args[1];
        }
      }

      // Create the time service instance
      _theTimeService = new StatelessTimeServiceImpl();

      // Create the properties for the JNDI operation
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, aJndiUrlProvider);
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, aJndiInitialContext);
      props.setProperty(Consts.UBIK_DOMAIN_NAME, Consts.DEFAULT_DOMAIN);

      // Bind the time service to the JNDI service
      InitialContext context = new InitialContext(props);
      context.bind("util/timeService", _theTimeService);
      System.out.println("Time service bind and ready to rock");

      while (true) {
        Thread.sleep(100000);
      }
    } catch (NamingException ne) {
      System.err.println("Error creating the JNDI context");
      ne.printStackTrace();
    } catch (InterruptedException ie) {
      System.err.println("The time server is interrupted and will exit");
      ie.printStackTrace();
    } catch (Exception e) {
      System.err.println("System error running the time service");
      e.printStackTrace();
    }
  }
}
