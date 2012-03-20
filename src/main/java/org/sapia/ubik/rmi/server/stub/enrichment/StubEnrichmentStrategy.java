package org.sapia.ubik.rmi.server.stub.enrichment;

import java.rmi.RemoteException;

import javax.naming.Name;

import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.rmi.server.stub.StubStrategy;
import org.sapia.ubik.util.Assertions;

/**
 * An instance of this interface is expected to perform the enrichment of a stub that is bound
 * in Ubik's JNDI. That is: at binding time, such a strategy is invoked so that it can perform
 * stub substitution.
 */
public interface StubEnrichmentStrategy extends StubStrategy {
  
  /**
   * An instance of this class holds the JNDI-related information that 
   * is used to bind a stub into Ubik's JNDI.
   * 
   * @author yduchesne
   *
   */
  public static class JndiBindingInfo {
    
    private String           baseUrl; 
    private Name             name; 
    private DomainName       domainName;
    private MulticastAddress mcastAddress; 
    
    public JndiBindingInfo(
        String baseUrl, 
        Name name, 
        DomainName domainName, 
        MulticastAddress address) {
      this.baseUrl      = baseUrl;
      this.name         = name;
      this.domainName   = domainName;
      this.mcastAddress = address;
    }
    
    /**
     * @return the URL of the JNDI server under which the stub is to be bound.
     */
    public String getBaseUrl() {
      Assertions.notNull(baseUrl, "Base URL not set");
      return baseUrl;
    }
    
    /**
     * @return the domain name of the JNDI server.
     */
    public DomainName getDomainName() {
      Assertions.notNull(domainName, "Domain name not set");        
      return domainName;
    }

    /**
     * @return the {@link MulticastAddress} used for discovery.
     */
    public MulticastAddress getMcastAddress() {
      Assertions.notNull(mcastAddress, "Multicast address not set");        
      return mcastAddress;
    }
    
    /**
     * @return the JNDI {@link Name} under which the stub is to be bound.
     */
    public Name getName() {
      Assertions.notNull(name, "Name not set");        
      return name;
    }
  }
  
  /**
   * @param stub the stub that should be enriched.
   * @param info the {@link JndiBindingInfo} holding the JNDI-related parameters that
   * will be used to bind the stub into Ubik's JNDI.
   * @return <code>true</code> if this instance will handle the given stub.
   */
  public boolean apply(Object stub, JndiBindingInfo info);
  
  /**
   * @param stub the stub to enrich.
   * @param info the {@link JndiBindingInfo} holding the JNDI-related parameters that
   * will be used to bind the stub into Ubik's JNDI.
   * @return the enriched stub. 
   * @throws RemoteException if an error occurs while enriching the stub.
   */
  public Object enrich(Object stub, JndiBindingInfo info) throws RemoteException;
  
  
}