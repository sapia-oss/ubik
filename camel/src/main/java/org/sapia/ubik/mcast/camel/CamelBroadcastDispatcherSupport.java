package org.sapia.ubik.mcast.camel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ConnectionStateListenerList;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;

/**
 * Support class for implementing Camel-based {@link BroadcastDispatcher}s.
 * 
 * @author yduchesne
 *
 */
public abstract class CamelBroadcastDispatcherSupport implements BroadcastDispatcher {

  public static final String PROPERTY_PREFIX = "ubik.rmi.naming.broadcast.camel";

  public static final String DEFAULT_CAMEL_TRANSPORT_TYPE = "camel/broadcast";
  
  /**
   * {@link MulticastAddress} implementation.
   * 
   * @author yduchesne
   *
   */
  public static final class CamelMulticastAddress implements MulticastAddress, Externalizable {
    
    static final long serialVersionUID = 1L;
  
    private String endpointUri;
    
    /**
     * Meant for externalization only.
     */
    public CamelMulticastAddress() {
    }
    
    /**
     * @param endpointUri URI of the endpoint to which this address corresponds.
     */
    public CamelMulticastAddress(String endpointUri) {
      this.endpointUri = endpointUri;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CamelMulticastAddress) {
        CamelMulticastAddress other = (CamelMulticastAddress) obj;
        return getTransportType().equals(other.getTransportType()) && endpointUri.equals(other.endpointUri);
      }
      return false;
    }
    
    @Override
    public String getTransportType() {
      return DEFAULT_CAMEL_TRANSPORT_TYPE;
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      endpointUri = in.readUTF();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeUTF(endpointUri);
    }
    
    @Override
    public Map<String, String> toParameters() {
      Map<String, String> params = new HashMap<String, String>();
      params.put(Consts.BROADCAST_CAMEL_ENDPOINT_URI, endpointUri);
      return params;
    }
    
    @Override
    public String toString() {
      return endpointUri;
    }
  }
  
  // ==========================================================================
  
  protected final Category log = Log.createCategory(getClass());
  
  private EventConsumer               consumer;
  private CamelContext                context;
  private String                      endpointUri;
  private MulticastAddress            endpointAddress;
  private ConnectionStateListenerList connectionListeners = new ConnectionStateListenerList();
  private Endpoint                    endpoint;
  private ProducerTemplate            template;
  private Map<String, String>         headers = new HashMap<String, String>();
  // --------------------------------------------------------------------------
  // Lifecycle
 
  @Override
  public void initialize(EventConsumer consumer, final Conf config) {
    this.consumer = consumer;
    context = new DefaultCamelContext(new SimpleRegistry());
    final String endpointUriWithOptions = config.getNotNullProperty(Consts.BROADCAST_CAMEL_ENDPOINT_URI);
    log.info("Camel endpoint URI (node %s): %s", consumer.getNode(), endpointUriWithOptions);
    
    int i;
    if ((i = endpointUriWithOptions.indexOf("?")) > 0) {
      endpointUri = endpointUriWithOptions.substring(0, i);
    } else {
      endpointUri = endpointUriWithOptions;
    }
    
    Uri uri = Uri.parse(endpointUriWithOptions);
    String optionPrefix = PROPERTY_PREFIX + "." + uri.getScheme() + ".option";
    String headerPrefix = PROPERTY_PREFIX + "." + uri.getScheme() + ".header";
    
    Map<String, String> options = new HashMap<String, String>();
    for (String n : config.propertyNames()) {
      if (n.startsWith(optionPrefix) && n.length() > optionPrefix.length()) {
        String optName  = n.substring(optionPrefix.length() + 1);
        String optValue = config.getProperty(n);
        log.info("Setting option: %s=%s", optName, optValue);
        options.put(optName, optValue);
      } else if (n.startsWith(headerPrefix) && n.length() > headerPrefix.length()) {
        String optName  = n.substring(headerPrefix.length() + 1);
        String optValue = config.getProperty(n);
        log.info("Setting header: %s=%s", optName, optValue);
        headers.put(optName, optValue);
      } 
    }

    final StringBuilder endpointUriWithAdditionalOptions = new StringBuilder(endpointUriWithOptions);
    if (!options.isEmpty()) {
      if (!endpointUriWithOptions.contains("?")) {
        endpointUriWithAdditionalOptions.append("?");
      } 
      int count = 0;
      for (Map.Entry<String, String> entry : options.entrySet()) {
        if (count > 0) {
          endpointUriWithAdditionalOptions.append("&");
        }
        endpointUriWithAdditionalOptions.append(entry.getKey()).append("=").append(entry.getValue());
        count++;
      }
    } 
    
    log.info("Connection string: %s", endpointUriWithAdditionalOptions.toString());
    
    endpointAddress = new CamelMulticastAddress(endpointUri);
    
    RouteBuilder routes = new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from(endpointUriWithAdditionalOptions.toString())
          .setExchangePattern(ExchangePattern.InOnly)
          .process(new Processor() {
          @Override
          public void process(Exchange exchange) throws Exception {
            RemoteEvent evt = doReadEventFrom(exchange);
            CamelBroadcastDispatcherSupport.this.log.debug("Receiving remote event: %s", evt.getType());
            if (evt.isSync()) {
              CamelBroadcastDispatcherSupport.this.log.warning(
                  "Received sync remote event: %s. Ignoring (broadcast events should be async)", evt.getType()
              );
            } else {
              CamelBroadcastDispatcherSupport.this.log.debug(
                  "Received async event %s from node %s", evt.getType(), evt.getNode()
              );
              CamelBroadcastDispatcherSupport.this.consumer.onAsyncEvent(evt);
            }
          }
        });
      }
    };
    
    try {
      context.addRoutes(routes);
    } catch (Exception e) {
      throw new IllegalStateException("Could not set up CamelContext", e);
    }

    doInitializeContext(context);

    endpoint = context.getEndpoint(endpointUriWithAdditionalOptions.toString());
    template = context.createProducerTemplate();
  }
  
  @Override
  public void start() {
    Assertions.illegalState(consumer == null, "EventConsumer not set");
    Assertions.illegalState(context == null, "CamelContext not set");
    try {
      context.start();
      connectionListeners.notifyConnected();
    } catch (Exception e) {
      throw new IllegalStateException("Could not start CamelContext", e);
    }
  }
  
  @Override
  public void close() {
    if (context != null) {
      try {
        context.stop();
      } catch (Exception e) {
        log.warning("Error shutting down CamelContext", e);
      }
    }
  }
  
  @Override
  public void dispatch(ServerAddress unicastAddr, boolean alldomains,
      String type, Object data) throws IOException {
    Exchange exc = doCreateExchangeFor(
        new RemoteEvent(type, data).setNode(consumer.getNode()).setUnicastAddress(unicastAddr)
    );
    for (Map.Entry<String, String> h : headers.entrySet()) {
      exc.getIn().setHeader(h.getKey(), h.getValue());
    }
    template.send(endpoint, exc);  
  }
  
  @Override
  public void dispatch(ServerAddress unicastAddr, String domain, String type,
      Object data) throws IOException {
    Exchange exc = doCreateExchangeFor(
        new RemoteEvent(domain, type, data).setNode(consumer.getNode()).setUnicastAddress(unicastAddr)
    );
    for (Map.Entry<String, String> h : headers.entrySet()) {
      exc.getIn().setHeader(h.getKey(), h.getValue());
    }
    template.send(endpoint, exc);
  }
  
  @Override
  public MulticastAddress getMulticastAddress() {
    return this.endpointAddress;
  }
  
  @Override
  public MulticastAddress getMulticastAddressFrom(Conf props) {
    return new CamelMulticastAddress(props.getNotNullProperty(Consts.BROADCAST_CAMEL_ENDPOINT_URI));
  }
  
  @Override
  public String getNode() {
    return consumer.getNode();
  }
  
  @Override
  public void removeConnectionStateListener(ConnectionStateListener listener) {
    connectionListeners.remove(listener);
  }
  
  @Override
  public void addConnectionStateListener(ConnectionStateListener listener) {
    connectionListeners.add(listener);
  }

  // --------------------------------------------------------------------------
  // Restricted methods
  
  /**
   * Allows initializing the {@link CamelContext} with additional state.
   * 
   * @param context the {@link CamelContext}.
   */
  protected void doInitializeContext(CamelContext context) {
  }
  
  /**
   * @param outgoing a {@link RemoteEvent} that will be sent as part of the {@link Exchange} that is returned.
   * @return a new {@link Exchange}, wrapping the given remote event.
   */
  protected abstract Exchange doCreateExchangeFor(RemoteEvent outgoing);
  
  /**
   * @param incoming an incoming {@link Exchange}.
   * @return a new {@link RemoteEvent}, extracted from the given exchange.
   */
  protected abstract RemoteEvent doReadEventFrom(Exchange incoming);
  
  protected EventConsumer consumer() {
    return consumer;
  }
  
  protected CamelContext context() {
    return context;
  }
  
}
