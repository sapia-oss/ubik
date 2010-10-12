package org.sapia.ubik.rmi.server.gc;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;

import org.sapia.ubik.jmx.MBeanContainer;
import org.sapia.ubik.jmx.MBeanFactory;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.PropUtil;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.OID;
import org.sapia.ubik.rmi.server.perf.HitStatFactory;
import org.sapia.ubik.rmi.server.perf.HitsPerHourStatistic;
import org.sapia.ubik.rmi.server.perf.HitsPerMinStatistic;
import org.sapia.ubik.rmi.server.perf.Statistic;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.taskman.TaskManager;


/**
 * This class implements a the client-side distributed garbage
 * collection algorithm.
 *
 * @author Yanick Duchesne
 * 2002-09-02
 */
public class ClientGC implements Task, ClientGCMBean, MBeanFactory {

  class RemoteObjectCountStat extends Statistic {
    RemoteObjectCountStat(){
      super("ClientGcRemoteObjectCount");
    }
    public double getStat() {
      int total = 0;
      // Create a new list with value of map to avoid concurrent modification exception
      for (Map<OID, Reference<Object>> refsForHost: new ArrayList<Map<OID, Reference<Object>>>(_objByHosts.values())) {
        total += refsForHost.size();
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
  private Map<ServerAddress, Map<OID, Reference<Object>>> _objByHosts  = new ConcurrentHashMap<ServerAddress, Map<OID, Reference<Object>>>();
  private long            _lastGlobalPingTime = System.currentTimeMillis();
  private int             _threshold, _lastGcCount;
  private HitsPerMinStatistic  _gcRefPerMin;
  private HitsPerMinStatistic  _gcDerefPerMin;
  private HitsPerMinStatistic  _gcConnectionsPerMin;
  private HitsPerHourStatistic _forcedGcPerHour;
  private Statistic            _objectCount = new RemoteObjectCountStat();
  
  private TaskManager     _taskMan;

  /**
   * Creates a new {@link ClientGC} instance.
   *
   * @param taskman
   */
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
  public boolean register(ServerAddress address, OID oid, Object remote) {
    
    if (Log.isDebug()) {
      Log.debug(getClass(), "Registering remote object: " + oid + " from " + address);
    }

    synchronized (_objByHosts) {
      Map<OID, Reference<Object>> hostMap = _objByHosts.get(address);

      if (hostMap == null) {
        hostMap = new ConcurrentHashMap<OID, Reference<Object>>();
        _objByHosts.put(address, hostMap);
      }
      
      if (hostMap.containsKey(oid)) {
        return false;
      }
      
      if (_gcRefPerMin.isEnabled()) {
        _gcRefPerMin.hit();
      }
  
      hostMap.put(oid, new WeakReference<Object>(remote));
  
      return true;
    }
  }
  
  /* (non-Javadoc)
   * @see org.sapia.ubik.taskman.Task#exec(org.sapia.ubik.taskman.TaskContext)
   */
  public void exec(TaskContext ctx) {
    Set<ServerAddress> keySet;
    ServerAddress[] addresses;
    OID[]           oids;
    ArrayList<OID>  oidsToSend = new ArrayList<OID>(_gcBatchSize);
    int             totalCount = 0;
    int             remoteObjectCount = 0;
    Map<OID, Reference<Object>> refs;

    if(Log.isDebug()) {
      Log.debug(getClass(), "running client GC...");
    }

    try {
      keySet = _objByHosts.keySet();
      addresses = keySet.toArray(new ServerAddress[keySet.size()]);
  
      if (Log.isDebug()) {
        Log.debug(getClass(), "host count: " + addresses.length);
      }
  
      for (int i = 0; i < addresses.length; i++) {
        if (Log.isDebug()) {
          Log.debug(getClass(), "host address: " + addresses[i]);
        }
        
        refs = _objByHosts.get(addresses[i]);
        oids = refs.keySet().toArray(new OID[refs.size()]);
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
        
        boolean sent = true;
  
        for (int j = 0; j < oids.length; j++) {
          Reference<Object> ref = refs.get(oids[j]);
          if (ref.get() == null) {
            if (Log.isDebug()) {
              Log.debug(getClass(), oids[j] + " is null");
            }
  
            refs.remove(oids[j]);
            oidsToSend.add(oids[j]);
            totalCount++;
  
            if (oidsToSend.size() >= _gcBatchSize) {
              sent = doSend(oidsToSend.toArray(new OID[oidsToSend.size()]), oidsToSend.size(), addresses[i]);
              oidsToSend.clear();
            }
          }
           
        }
  
        if (sent) {
          doSend(oidsToSend.toArray(new OID[oidsToSend.size()]), oidsToSend.size(), addresses[i]);
        }
        if (totalCount > 0) {
          _lastGcCount = totalCount;
        }
        totalCount = 0;
        if (_threshold > 0 && refs.size() >= _threshold) {
          _forcedGcPerHour.hit();
          Runtime.getRuntime().gc();
        }      
      }
    } finally {
      _lastGlobalPingTime = System.currentTimeMillis();
    }
  }
  
  /////// JMX-Related
  
  public void setBatchSize(int size) {
    _gcBatchSize = size;
  }
  
  public int getBatchSize() {
    return _gcBatchSize;
  }
  
  public void setThreshold(int t) {
    _threshold = t;
  }

  public int getThreshold() {
    return _threshold;
  }

  public long getInterval() {
    return _gcInterval;
  }
  
  public int getRemoteObjectCount() {
    Set<ServerAddress> keySet = _objByHosts.keySet();
    ServerAddress[] addresses = keySet.toArray(new ServerAddress[keySet.size()]);
    int total = 0;
    for (int i = 0; i < addresses.length; i++) {
      Map<OID, Reference<Object>> refs  = _objByHosts.get(addresses[i]);
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

  private boolean doSend(OID[] toSend, int count, ServerAddress addr) {
    if ((count == 0) && ((System.currentTimeMillis() - _lastGlobalPingTime) < _gcInterval)) {
      Log.info(getClass(), "Skipping Client GC command to " + addr + " bacause interval " + _gcInterval + " not met");
      return true;
    }
    Log.info(getClass(), "Sending Client GC command to " + addr);
    
    Connection conn = null;

    try {
      if (count > 0) {
        if (Log.isDebug()) {
          Log.debug(getClass(), "sending GC command to " + addr + "; cleaning " + count + " objects");
        }
        
        if(Log.isInfo()){
          for(int i = 0; i < toSend.length; i++){
            Log.info(getClass(), "Dereferencing: " + toSend[i]);
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
      if (e instanceof RemoteException) {
        Log.info(ClientGC.class, "Error sending GC command to server " + addr + " - cleaning up corresponding remote objects", e);
        synchronized (_objByHosts) {
          _objByHosts.remove(addr);
        }
      }
      
      if (conn != null) {
        conn.close();
      }
      
      return false;
    }

    for (int k = 0; k < count; k++) {
      toSend[k] = null;
    }
    
    return true;
  }

}
