package org.sapia.ubik.rmi.naming.remote;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.util.Assertions;

/**
 * Convenience builder class used to create Ubik JNDI {@link Context} instances.
 * 
 * @author yduchesne
 * 
 */
public final class JNDIContextBuilder {

  private Properties properties = new Properties();

  private String domain = Defaults.DEFAULT_DOMAIN;
  private String host;
  private int    port   = JNDIConsts.DEFAULT_PORT;

  /**
   * @param domain
   *          the domain of the JNDI server to which to connect (defaults to
   *          {@link JNDIConsts#DEFAULT_DOMAIN}.
   * @return this instance.
   */
  public JNDIContextBuilder domain(String domain) {
    this.domain = domain;
    return this;
  }

  /**
   * @param host
   *          the host of the JNDI server to connect to.
   * @return this instance.
   */
  public JNDIContextBuilder host(String host) {
    this.host = host;
    return this;
  }

  /**
   * @param port
   *          the port of the JNDI server to connect to (defaults to
   *          {@link JNDIConsts#DEFAULT_PORT}.
   * @return this instance.
   */
  public JNDIContextBuilder port(int port) {
    this.port = port;
    return this;
  }

  /**
   * Adds a property to this instance's {@link Properties}, which will be passed
   * to the {@link InitialContext} that this instance creates.
   * 
   * @param name
   *          the name of the property to add.
   * @param value
   *          the property value.
   * @return
   */
  public JNDIContextBuilder property(String name, String value) {
    this.properties.setProperty(name, value);
    return this;
  }

  /**
   * @param props
   *          some {@link Properties} to copy to this instance's own properties.
   * @return this instance.
   */
  public JNDIContextBuilder properties(Properties props) {
    for (String name : props.stringPropertyNames()) {
      this.properties.setProperty(name, props.getProperty(name));
    }
    return this;
  }

  /**
   * @return a new JNDI {@link Context}, which may be used to perform lookup.
   * @throws NamingException
   *           if a problem occurs trying to build the {@link Context}.
   */
  public Context build() throws NamingException {
    Assertions.notNull(domain, "The domain was not set");
    Assertions.notNull(host, "The host was not set");
    Properties props = new Properties();
    props.setProperty(Consts.UBIK_DOMAIN_NAME, domain);
    props.setProperty(InitialContext.PROVIDER_URL, "ubik://" + host + ":" + port);
    props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
    InitialContext ctx = new InitialContext(props);
    return ctx;
  }

  /**
   * @return a new instance of this class.
   */
  public static JNDIContextBuilder newInstance() {
    return new JNDIContextBuilder();
  }
}
