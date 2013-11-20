package org.sapia.ubik.ioc.guice;

import javax.naming.NamingException;

import org.sapia.ubik.ioc.NamingService;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * This class implements a {@link Provider} that exports an arbitrary object to
 * Ubik's JNDI.
 * 
 * <p>
 * An instance of this class expects a {@link NamingService} as a dependency.
 * <p>
 * Example:
 * 
 * <pre>
 * 
 * // creating the NamingService instance and binding it to Guice
 * final NamingService naming = new NamingServiceImpl(&quot;default&quot;).setJndiHost(Localhost.getLocalAddress().getHostAddress()).setJndiPort(1099);
 * 
 * Injector injector = Guice.createInjector(new AbstractModule() {
 * 
 *   &#064;Override
 *   protected void configure() {
 *     bind(NamingService.class).toInstance(naming);
 *     bind(TimeServiceIF.class).toProvider(new RemoteServiceExporter&lt;TimeServiceIF&gt;(new TimeServiceImpl(), &quot;services/time&quot;));
 *   }
 * 
 * });
 * 
 * // calling getInstance() internally invokes get() on the RemoteServiceExporter,
 * // which publishes the service
 * // to the JNDI.
 * TimeServiceIF server = injector.getInstance(TimeServiceIF.class);
 * 
 * System.out.println(&quot;Bound time server&quot;);
 * while (true) {
 *   Thread.sleep(10000);
 * }
 * </pre>
 * 
 * For the service to be exported, the exporter must be invoked from the
 * application (in order for the {@link #get()} or through a dependency
 * injection - i.e.: the exported object is injected internally by Guice's
 * injector into another object.
 * <p>
 * Indeed, it is when the {@link #get()} method of this class is called that a
 * corresponding instance will export the intended object.
 * 
 * @author yduchesne
 * 
 * @param <T>
 *          the type of the object to export.
 * @see #get()
 * @see RemoteServiceImporter
 */
public class RemoteServiceExporter<T> implements Provider<T> {

  @Inject(optional = false)
  private NamingService naming;
  private String jndiName;
  private T toExport;
  private boolean isBound;

  /**
   * @param toExport
   *          the {@link Object} to export.
   * @param jndiName
   *          the JNDI name under which to bind the given object.
   */
  public RemoteServiceExporter(T toExport, String jndiName) {
    this.toExport = toExport;
    this.jndiName = jndiName;
  }

  /**
   * This method returns the object that must be exported. At its first
   * invocation, this method will export its encapsulated object to Ubik's JNDI.
   * 
   * @return the object that must be exported.
   */
  public T get() {
    if (!isBound) {
      try {
        naming.bind(jndiName, toExport);
      } catch (NamingException e) {
        throw new IllegalStateException(String.format("Could not export %s under %s", toExport, jndiName), e);
      }
      isBound = true;
    }
    return toExport;
  }

}
