package org.sapia.ubik.rmi.server.gc;

import org.sapia.ubik.jmx.MBeanContainer;
import org.sapia.ubik.jmx.MBeanFactory;
import org.sapia.ubik.net.*;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.PropUtil;
import org.sapia.ubik.rmi.server.*;
import org.sapia.ubik.rmi.server.perf.HitStatFactory;
import org.sapia.ubik.rmi.server.perf.HitsPerHourStatistic;
import org.sapia.ubik.rmi.server.perf.HitsPerMinStatistic;
import org.sapia.ubik.rmi.server.perf.Statistic;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.taskman.TaskManager;

import java.lang.ref.*;

import java.util.*;

import javax.management.ObjectName;


/**
 * This class implements a the client-side distributed garbage
 * collection algorithm.
 *
 * @author Yanick Duchesne
 * 2002-09-02
 */
public class ClientGC implements Task, ClientGCMBean, MBeanFactory {

  class RemoteObjectCountStat extends Statistic{
    RemoteObjectCountStat(){
      super("ClientGcRemoteObjectCount");
    }
    public double getStat() {
      Map[] refsByHost = (Map[])_objByHosts.values().toArray(new Map[_objByHosts.size()]);
      int total = 0;
      for(int i = 0; i < refsByHost.length; i++){
        total+=refsByHost[i].size();
      }
      return total;
    }
  }
  
  /* time at which GC checks for unreferenced objects */
  public static final long GC_CLEAN_INTERVAL = 10000;

  /* size of OID batches sent to the server */
  public static final int GC_CLEAN_SIZE = 1000;
  
  
  private long            _gcInterval  = GC_CLEAN_INTERVAL;
  private int             _gcBatchSize = GC_CLEAN_SIZE;
  private Map             _objByHosts  = Collections.synchronizedMap(new HashMap());
  private long            _lastGlobalPingTime = System.currentTimeMillis();
  private int             _threshold, _lastGcCount;
  private HitsPerMinStatistic  _gcRefPerMin;
  private HitsPerMinStatistic  _gcDerefPerMin;
  private HitsPerMinStatistic  _gcConnectionsPerMin;
  private HitsPerHourStatistic _forcedGcPerHour;
  private Statistic            _objectCount = new RemoteObjectCountStat();
  
  private TaskManager     _taskMan;

  public ClientGC(TaskManager taskman) {
    PropUtil props = new PropUtil().addProperties(System.getProperties());
    _taskMan = taskman;

    _gcInterval       = props.getLongProperty(Consts.CLIENT_GC_INTERVAL, GC_CLEAN_INTERVAL);
    _gcBatchSize      = props.getIntProperty(Consts.CLIENT_GC_BATCHSIZE, GC_CLEAN_SIZE);
    _threshold        = props.getIntProperty(Consts.CLIENT_GC_THRESHOLD, 0);
    
    _gcConnectionsPerMin   = HitStatFactory.createHitsPerMin("ClientGcConnectionsPerMin", _gcInterval, Hub.statsCollector);     
    _gcRefPerMin           = HitStatFactory.createHitsPerMin("ClientGcRefPerMin", _gcInterval, Hub.statsCollector);
    _gcDerefPerMin        = HitStatFactory.createHitsPerMin("ClientGcDerefPerMin", _gcInterval, Hub.statsCollector);
    _forcedGcPerHour       = HitStatFactory.createHitsPerHour("ClientGcForcedPerHour", _gcInterval, Hub.statsCollector);
    
    Hub.statsCollector.addStat(_objectCount);
    
    _taskMan.addTask(new TaskContext("UbikRMI.ClientGC", _gcInterval), this);
    
    Log.info(getClass(), "Will run every " + _gcInterval + " ms.");
  }

  /**
   * Registers an object identifier, associated to a stub. These mappings are
   * kept by server(every remote object is kept internally on a per-server basis).
   *
   * @param address the <code>ServerAddress</code> representing the server from which
   * the remote instance comes from.
   * @param oid the <code>OID</code> of the remote instance to keep.
   * @param remote the remote instance (a stub) to track.
   */
  public synchronized boolean register(ServerAddress address, OID oid,
    Object remote) {
    
    if(Log.isDebug()){
      Log.debug(getClass(), "Registering remote object: " + oid + " from " + address);
    }
    
    Map hostMap = getHostMap(address);

    if (hostMap.containsKey(oid)) {
      return false;
    }
    
    if(_gcRefPerMin.isEnabled()){
      _gcRefPerMin.hit();
    }

    hostMap.put(oid, new WeakReference(remote));

    return true;
  }
  
  public void exec(TaskContext ctx) {
    Set             keySet;
    ServerAddress[] addresses;
    OID[]           oids;
    OID[]           oidsToSend = new OID[_gcBatchSize];
    int             count      = 0;
    int             totalCount = 0;
    int             remoteObjectCount = 0;
    Reference       ref;
    Map             refs;

    if(Log.isDebug()) {
      Log.debug(getClass(), "running client GC...");
    }

    try {
      keySet      = _objByHosts.keySet();
      addresses   = (ServerAddress[]) keySet.toArray(new ServerAddress[keySet.size()]);
  
      if (Log.isDebug()) {
        Log.debug(getClass(), "host ids: " + addresses.length);
      }
  
      for (int i = 0; i < addresses.length; i++) {
        refs   = (Map) _objByHosts.get(addresses[i]);
        oids   = (OID[]) refs.keySet().toArray(new OID[refs.size()]);
        remoteObjectCount += refs.size();
  
        if (Log.isDebug()) {
          Log.debug(getClass(), addresses[i] + " oids: " + oids.length);
        }
  
        synchronized (_objByHosts) {
          if (oids.length == 0) {
            _objByHosts.remove(addresses[i]);
  
            break;
          }
        }
  
  
        for (int j = 0; j < oids.length; j++) {
          ref = (Reference) refs.get(oids[j]);
          if (ref.get() == null) {
            if (Log.isDebug()) {
              Log.debug(getClass(), oids[j] + " is null");
            }
  
            refs.remove(oids[j]);
            oidsToSend[count++] = oids[j];
            totalCount++;
  
            if (count >= oidsToSend.length) {
              doSend(oidsToSend, count, addresses[i]);
              count = 0;
            }
          }
           
        }
  
        doSend(oidsToSend, count, addresses[i]);
        
        if(totalCount > 0){
          _lastGcCount = totalCount;
        }
        totalCount = 0;
        count = 0;
        if(_threshold > 0 && refs.size() >= _threshold){
          _forcedGcPerHour.hit();
          Runtime.getRuntime().gc();
        }      
      }
    } finally {
      _lastGlobalPingTime = System.currentTimeMillis();
    }
  }
  
  /////// JMX-Related
  
  public void setBatchSize(int size){
    _gcBatchSize = size;
  }
  
  public int getBatchSize(){
    return _gcBatchSize;
  }
  
  public void setThreshold(int t){
    _threshold = t;
  }

  public int getThreshold(){
    return _threshold;
  }

  public long getInterval(){
    return _gcInterval;
  }
  
  public int getRemoteObjectCount(){
    Set keySet      = _objByHosts.keySet();
    ServerAddress[] addresses = (ServerAddress[]) keySet.toArray(new ServerAddress[keySet.size()]);
    int total = 0;
    for (int i = 0; i < addresses.length; i++) {
      Map refs  = (Map) _objByHosts.get(addresses[i]);
      total += refs.size();
    } 
    return total;
  }
  
  public Date getLastPingDate() {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(_lastGlobalPingTime);
    return cal.getTime();
  }
  
  public double getGcPerMin() {
    return _gcDerefPerMin.getStat();
  }
  
  public double getForcedGcPerHour() {
    return _forcedGcPerHour.getStat();
  }
  
  public int getLastGcCount() {
    return _lastGcCount;
  }
  
  //////// MBeanFactory
  
  public MBeanContainer createMBean() throws Exception{
    ObjectName name = new ObjectName("sapia.ubik.rmi:type=ClientGC");
    return new MBeanContainer(name, this);
  }
  
  //////// Private methods  

  private void doSend(OID[] toSend, int count, ServerAddress addr) {
    if ((count == 0) &&
          ((System.currentTimeMillis() - _lastGlobalPingTime) < _gcInterval)) {
      return;
    }
    
    Connection conn = null;

    try {
      if (count > 0) {
        if (Log.isDebug()) {
          Log.debug(getClass(), "sending GC command to " + addr + "; cleaning " + count + " objects");
        }
        
        if(Log.isDebug()){
          for(int i = 0; i < toSend.length; i++){
            Log.debug(getClass(), "Dereferencing: " + toSend[i]);
          }
        }
      } else {
        if (Log.isDebug()) {
          Log.debug(getClass(), "no garbage; pinging server...");
        }
      }
      
      conn = TransportManager.getConnectionsFor(addr).acquire();
      if(_gcConnectionsPerMin.isEnabled()){
        _gcDerefPerMin.hit(count);
        _gcConnectionsPerMin.hit();
      }
      conn.send(new CommandGc(toSend, count));
      conn.receive();
      TransportManager.getConnectionsFor(addr).release(conn);
    } catch (Throwable e) {
      if (conn != null) {
        conn.close();
      }
    }

    for (int k = 0; k < count; k++) {
      toSend[k] = null;
    }
  }

  private final Map getHostMap(ServerAddress addr) {
    Map hostMap = (Map) _objByHosts.get(addr);

    if (hostMap == null) {
      synchronized(_objByHosts){
        hostMap = (Map) _objByHosts.get(addr);
        if(hostMap == null){
          hostMap = Collections.synchronizedMap(new HashMap());
          _objByHosts.put(addr, hostMap);
        }
      }
    }

    return hostMap;
  }
  
}
