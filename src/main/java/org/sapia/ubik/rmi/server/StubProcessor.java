package org.sapia.ubik.rmi.server;

import java.lang.reflect.InvocationHandler;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.Timer;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.Stubs;
import org.sapia.ubik.rmi.server.stub.creation.DefaultStubCreationStrategy;
import org.sapia.ubik.rmi.server.stub.creation.StubCreationStrategy;
import org.sapia.ubik.rmi.server.stub.enrichment.ReliableStubEnrichmentStrategy;
import org.sapia.ubik.rmi.server.stub.enrichment.StatelessStubEnrichmentStrategy;
import org.sapia.ubik.rmi.server.stub.enrichment.StubEnrichmentStrategy;
import org.sapia.ubik.rmi.server.stub.enrichment.StubEnrichmentStrategy.JndiBindingInfo;
import org.sapia.ubik.rmi.server.stub.handler.DefaultStubInvocationHandlerCreationStrategy;
import org.sapia.ubik.rmi.server.stub.handler.StubInvocationHandlerCreationStrategy;

/**
 * The {@link StubProcessor} centralizes logic pertaining to stub creation. This logic can be
 * modified/extended through the implementation of specific strategies (as we'll detail further
 * below). 
 * <p>
 * Since Ubik stub are based on dynamic proxies by default, the logic pertaining to stub creation
 * has been sub-divided as "strategies", with each strategy addressing a specific phase in the stub
 * life-cycle:
 * <ol>
 *   <li>Invocation handler creation: since dynamic proxies wrap an {@link InvocationHandler}, creating
 *   such a handler is the first step in creating a new stub. Ubik expects a specific type of invocation
 *   handler (such handlers have to implement the {@link StubInvocationHandler} 
 *   interface). 
 *   <li>Stub creation: this step involves actually creating the dynamic proxy that will wrap a
 *   given {@link StubInvocationHandler} - created in the first step.
 *   <li>Lastly, there is another step, which may or me not be executed: it involves "enriching" a
 *   stub that is bound to Ubik's JNDI so that it can perform load-balancing, failover, and discovery 
 *   of newly bound endpoints - basically, this involves creating so-called "smart stubs" out of
 *   basic, plain-vanilla ones.
 * </ol> 
 * 
 * Each step in the stub life-cycle is abstracted through an interface. Respectively to the steps given
 * above, these interfaces are as follows:
 * <ol>
 *   <li> {@link StubInvocationHandlerCreationStrategy}
 *   <li> {@link StubCreationStrategy}
 *   <li> {@link StubEnrichmentStrategy}
 * </ol>
 * Generally speaking, the {@link StubProcessor} treats strategies along the lines of the chain of 
 * responsibility pattern: the processor will query each strategy implementation to inquire about its applicability, 
 * using the first one that indeed applies (i.e.: delegating to it the actual operation).
 * <p>
 * Therefore, for any strategy type (invocation handler creation, stub creation, stub enrichment), the default
 * behavior can be overridden by inserting the required strategy implementation (see 
 * {@link #insertHandlerCreationStrategy(StubInvocationHandlerCreationStrategy), #insertStubCreationStrategy(StubEnrichmentStrategy),
 * #insertEnrichmentStrategy(StubEnrichmentStrategy)}).
 * 
 * @author yduchesne
 *
 */
public class StubProcessor {
  
  private Timer                     stubCreation    = Stats.getInstance().createTimer(
      getClass(), 
      "StubCreation", 
      "Avg stub creation time");

  private Timer                     handlerCreation = Stats.getInstance().createTimer(
      getClass(), 
      "InvocationHandlerCreation", 
      "Avg handler creation time");
  
  private Timer                     enrichment      = Stats.getInstance().createTimer(
      getClass(), 
      "StubEnrichment", 
      "Avg stub enrichment time");  
  
  private ServerTable serverTable;
  
  StubProcessor(ServerTable serverTable) {
    this.serverTable = serverTable;
  }

  
  private List<StubInvocationHandlerCreationStrategy> handlerStrategies    = 
    new CopyOnWriteArrayList<StubInvocationHandlerCreationStrategy>();
  
  private List<StubCreationStrategy>                  stubStrategies       =
    new CopyOnWriteArrayList<StubCreationStrategy>();
  
  private List<StubEnrichmentStrategy>                enrichmentStrategies = 
    new CopyOnWriteArrayList<StubEnrichmentStrategy>();
  
  {
    handlerStrategies.add(new DefaultStubInvocationHandlerCreationStrategy());
    stubStrategies.add(new DefaultStubCreationStrategy());
    enrichmentStrategies.add(new StatelessStubEnrichmentStrategy());
    enrichmentStrategies.add(new ReliableStubEnrichmentStrategy());
  }

  /**
   * Inserts a given {@link StubInvocationHandlerCreationStrategy} at the head of the list of such strategies
   * kept by this instance.
   * @param strategy the {@link StubInvocationHandlerCreationStrategy} to insert.
   */
  public void insertHandlerCreationStrategy(StubInvocationHandlerCreationStrategy strategy) {
    handlerStrategies.add(0, strategy);
  }

  /**
   * Adds a given {@link StubInvocationHandlerCreationStrategy} to the tail of the list of such strategies
   * kept by this instance.
   * @param strategy the {@link StubInvocationHandlerCreationStrategy} to add.
   */
  public void appendHandlerCreationStrategy(StubInvocationHandlerCreationStrategy strategy) {
    handlerStrategies.add(strategy);
  }
  
  /**
   * Inserts a given {@link StubCreationStrategy} at the head of the list of such strategies
   * kept by this instance.
   * @param strategy the {@link StubCreationStrategy} to insert.
   */    
  public void insertStubCreationStrategy(StubEnrichmentStrategy strategy) {
    enrichmentStrategies.add(0, strategy);
  }
  
  /**
   * Adds a given {@link StubCreationStrategy} to the tail of the list of such strategies
   * kept by this instance.
   * @param strategy the {@link StubCreationStrategy} to add.
   */  
  public void appendStubCreationStrategy(StubCreationStrategy strategy) {
    stubStrategies.add(strategy);
  }
  
  /**
   * Inserts a given {@link StubEnrichmentStrategy} at the head of the list of such strategies
   * kept by this instance.
   * @param strategy the {@link StubEnrichmentStrategy} to insert.
   */  
  public void insertEnrichmentStrategy(StubEnrichmentStrategy strategy) {
    enrichmentStrategies.add(0, strategy);
  }
  
  /**
   * Adds a given {@link StubEnrichmentStrategy} to the tail of the list of such strategies
   * kept by this instance.
   * @param strategy the {@link StubEnrichmentStrategy} to add.
   */  
  public void appendEnrichmentStrategy(StubEnrichmentStrategy strategy) {
    enrichmentStrategies.add(strategy);
  }
  
  /**
   * This method iterates through this instance's {@link StubInvocationHandlerCreationStrategy}, selecting the
   * first one that applies (see {@link StubInvocationHandlerCreationStrategy#apply(Object)}.
   * <p>
   * It delegates to the selected strategy the creation of the {@link StubInvocationHandler} expected by the caller.
   * 
   * @param toExport the {@link Object} to export (as a remote object).
   * @param context the {@link RemoteRefContext} that the handler should wrap.  
   * @return the {@link StubInvocationHandler} that was created.
   * 
   * @throws RemoteException
   */
  public StubInvocationHandler createInvocationHandlerFor(Object toExport, RemoteRefContext context) throws RemoteException {
    
    for(StubInvocationHandlerCreationStrategy stra : handlerStrategies) {
      if(stra.apply(toExport)) {
        handlerCreation.start();
        StubInvocationHandler handler = stra.createInvocationHandlerFor(toExport, context);
        handlerCreation.end();
        return handler;
      }
    }
    throw new IllegalStateException("Could not create StubInvocationHandler: no strategy defined or applies");
  }
  
  /**
   * This method iterates through this instance's {@link StubCreationStrategy}, selecting the
   * first one that applies (see {@link StubCreationStrategy#apply(Object)}.
   * <p>
   * It delegates to the selected strategy the creation of the stub that is expected by the caller.
   * 
   * @param toExport the {@link Object} that was exported (as a remote object).
   * @param context the {@link StubInvocationHandler} that the stub should wrap.  
   * @return the stub that was created.
   */  
  public Object createStubFor(Object exported, StubInvocationHandler handler) {
    for(StubCreationStrategy stra : stubStrategies) {
      if(stra.apply(exported, handler)) {
        stubCreation.start();
        Object stub = stra.createStubFor(exported, handler, serverTable.getTypeCache().getInterfaceArrayFor(exported.getClass()));
        stubCreation.end();
        return stub;        
      }
    }
    throw new IllegalStateException("Could not create stub invocation handler: no strategy defined or applies");    
  }

  /**
   * This method iterates through this instance's {@link StubEnrichmentStrategy}, selecting the
   * first one that applies (see {@link StubEnrichmentStrategy#apply(Object)}.
   * <p>
   * It delegates to the selected strategy the enrichment of the stub that is passed in.
   * 
   * @param stub the {@link Object} that corresponds to the stub to enrich.
   * @param bindingInfo the Ubik JNDI parameters that are used to bind the stub.
   * @return an enriched stub.
   */  
  public Object enrichForJndiBinding(Object stub, JndiBindingInfo bindingInfo) throws RemoteException {
    if(!Stubs.isStub(stub)) {
      Properties props = new Properties();
      props.setProperty(Consts.TRANSPORT_TYPE, Hub.DEFAULT_TRANSPORT_TYPE);
      stub = serverTable.exportObject(stub, props);
    } 
    
    for(StubEnrichmentStrategy stra : enrichmentStrategies) {
      if(stra.apply(stub, bindingInfo)) {
        enrichment.start();
        Object enriched = stra.enrich(stub, bindingInfo);
        enrichment.end();
        return enriched;
      }
    }
    throw new IllegalStateException("Could not enrich stub: no strategy defined or applies");
  } 
}
