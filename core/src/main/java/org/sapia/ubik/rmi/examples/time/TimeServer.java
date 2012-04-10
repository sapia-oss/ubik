package org.sapia.ubik.rmi.examples.time;

import java.net.UnknownHostException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.util.Localhost;


/**
 * @
 */
public class TimeServer {
  private static TimeServiceIF _theTimeService;

  public TimeServer() {
  }

  public static void main(String[] args) {
    try {
      _theTimeService = new TimeServiceImpl();

      System.setProperty(Consts.MARSHALLING, "true");

      Properties props = new Properties();

      props.setProperty(InitialContext.PROVIDER_URL, "ubik://" + Localhost.getAnyLocalAddress().getHostAddress() +":1099/");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext context = new InitialContext(props);
      context.bind("util/timeService", _theTimeService);
      System.out.println("Time service bound");

      while (true) {
        Thread.sleep(100000);
      }
    } catch (UnknownHostException e){
      System.err.println("Could not find JNDI host");
      e.printStackTrace();      
    } catch (NamingException ne) {
      System.err.println("Error creating the JNDI context");
      ne.printStackTrace();
    } catch (InterruptedException ie) {
      System.err.println("The time server is interrupted and will exit");
    } catch (RuntimeException re) {
      System.err.println("System error running the time service");
      re.printStackTrace();
    }
  }
}
