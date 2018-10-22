package org.sapia.ubik.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UbikMetrics {

  private static UbikMetrics global = new UbikMetrics();
  
  public static UbikMetrics globalMetrics() {
    return global;
  }
  
  private Map<String, AtomicLong> metricsByName = new ConcurrentHashMap<>();
  
  public void incrementCounter(String name) {
    doGetOrCreateCounterFor(name).incrementAndGet();
  }
  
  public void incrementCounterBy(String name, long delta) {
    doGetOrCreateCounterFor(name).addAndGet(delta);
  }
  
  public Map<String, Long> makeSnapshot() {
    LinkedHashMap<String, Long> result = new LinkedHashMap<>();
    synchronized (metricsByName) {
      metricsByName.entrySet().forEach(e -> result.put(e.getKey(), e.getValue().get()));
    }
    
    return result;
  }
  
  private AtomicLong doGetOrCreateCounterFor(String name) {
    AtomicLong counter = metricsByName.get(name);
    if (counter == null) {
      synchronized (metricsByName) {
        counter = metricsByName.get(name);
        if (counter == null) {
          counter = new AtomicLong();
          metricsByName.put(name, counter);
        }
      }
    }
    
    return counter;
  }
  
}
