package org.sapia.ubik.ioc.guice;

import javax.naming.NamingException;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * This class implements a {@link Provider} that exports an arbitrary object to Ubik's JNDI.
 * 
 * <p>
 * An instance of this class expects a {@link NamingService} as a dependency.
 * <p>
 * Example:
 * <pre>
 * 
 *   // creating the NamingService instance and binding it to Guice
 *   final NamingService naming = new NamingServiceImpl("default")
 *     .setJndiHost(Localhost.getLocalAddress().getHostAddress())
 *     .setJndiPort(1099);    
 *   
 *   Injector injector = Guice.createInjector(new AbstractModule(){
 *     
 *     @Override
 *     protected void configure() {
 *       bind(NamingService.class).toInstance(naming);
 *       bind(TimeServiceIF.class).toProvider(new RemoteServiceExporter<TimeServiceIF>(new TimeServiceImpl(), "services/time"));
 *     }
 *     
 *   });
 *   
 *   // calling getInstance() internally invokes get() on the RemoteServiceExporter, which publishes the service
 *   // to the JNDI.
 *   TimeServiceIF server = injector.getInstance(TimeServiceIF.class);
 *   
 *   System.out.println("Bound time server");
 *   while(true){
 *     Thread.sleep(10000);
 *   }
 * </pre>
 * For the service to be exported, the exporter must be invoked from the application (in order for the {@link #get()} or through
 * a dependency injection - i.e.: the exported object is injected internally by Guice's injector into another object.
 * <p>
 * Indeed, it is when the {@link #get()} method of this class is called that a corresponding instance will export the intended object.
 * 
 * @author yduchesne
 *
 * @param <T> the type of the object to export.
 * @see #get()
 * @see RemoteServiceImporter 
 */
public class RemoteServiceExporter<T> implements Provider<T>{
  
  @Inject(optional=false)
  private NamingService _naming;
  private String _jndiName;
  private T _toExport;
  private boolean _isBound;
  
  public RemoteServiceExporter(T toExport, String jndiName){
    _toExport = toExport;
    _jndiName = jndiName;
  }
  
  /**
   * This method returns the object that must be exported. At its first invocation, this method will export its 
   * encapsulated object to Ubik's JNDI. 
   */
  public T get() {
    if(!_isBound){
      try{
        _naming.bind(_jndiName, _toExport);
      }catch(NamingException e){
        throw new IllegalStateException("Could not export " + _toExport + " under " + _jndiName, e);
      }
      _isBound = true;
    }
    return _toExport;
  }

}
