package org.sapia.ubik.rmi.examples.time;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Localhost;


/**
 * @
 */
public class TimeClient {
  private InitialContext _theContext;
  private TimeServiceIF  _theTimeService;
  private boolean        _isLogging;

  public TimeClient() {
    this(true);
  }

  public TimeClient(boolean isLogging) {
    try {
      Log.setDebug();
      _isLogging = isLogging;

      Properties props = new Properties();

      props.setProperty(InitialContext.PROVIDER_URL, "ubik://" + Localhost.getAnyLocalAddress().getHostAddress() + ":1099/");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());
      props.setProperty(Consts.UBIK_DOMAIN_NAME,
        Consts.DEFAULT_DOMAIN);      

      _theContext = new InitialContext(props);
    } catch (Exception ne) {
      System.err.println("Error creating the JNDI context");
      ne.printStackTrace();
      throw new RuntimeException("Error creating the JNDI context - " +
        ne.getMessage());
    }
  }

  public static void main(String[] args) {
    try {
      Log.setWarning();
      new TimeClient().execute();
      Hub.shutdown(30000);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public void execute() {
    try {
      String aTime = getTimeService().getTime();

      if (_isLogging) {
        System.out.println("Current time --> " + aTime);
      }
    } catch (NamingException ne) {
      System.err.println("Error looking up the time service");
      ne.printStackTrace();
    }
  }

  public TimeServiceIF getTimeService() throws NamingException {
    if (_theTimeService == null) {
      Object anObject = _theContext.lookup("util/timeService");

      if (anObject instanceof TimeServiceIF) {
        _theTimeService = (TimeServiceIF) anObject;
      } else {
        throw new NamingException(
          "The object received is not a TimeServiceIF: " + anObject);
      }
    }

    return _theTimeService;
  }
}
