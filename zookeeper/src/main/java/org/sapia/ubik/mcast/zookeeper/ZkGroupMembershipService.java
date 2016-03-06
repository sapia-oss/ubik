package org.sapia.ubik.mcast.zookeeper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.apache.curator.retry.RetryUntilElapsed;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.group.GroupMembershipListener;
import org.sapia.ubik.mcast.group.GroupMembershipService;
import org.sapia.ubik.mcast.group.GroupRegistration;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.TimeValue;

/**
 * Implements the {@link GroupMembershipService} interface over Zookeeper. An instance of this class is a heavyweight
 * whose resources should be released properly upon appplication termination, by calling {@link #close()}. Conversely,
 * make sure to call {@link #start()} before calling {@link #joinGroup(String, String, byte[], GroupMembershipListener)}.
 * 
 * @author yduchesne
 *
 */
public class ZkGroupMembershipService implements GroupMembershipService {
    
  public static final String CONNECTION_RETRY_INTERVAL         = "ubik.rmi.naming.mcast.zk.connection.retry.interval";

  public static final String CONNECTION_RETRY_MAX_TIME         = "ubik.rmi.naming.mcast.zk.connection.retry.max-time";
  
  public static final String MEMBERSHIP_CHECK_INTERVAL         = "ubik.rmi.naming.mcast.zk.members.check-interval";

  public static final String CONNECTION_TIMEOUT                = "ubik.rmi.naming.mcast.zk.connection.timeout";

  public static final String SESSION_TIMEOUT                   = "ubik.rmi.naming.mcast.zk.session.timeout";
  
  public static final String SERVER_LIST                       = "ubik.rmi.naming.mcast.zk.server-list";

  public static final String NAMESPACE                         = "ubik.rmi.naming.mcast.zk.namespace";

  
  public static final TimeValue DEFAULT_CONNECTION_RETRY_INTERVAL = TimeValue.valueOf("5s");
  public static final TimeValue DEFAULT_CONNECTION_RETRY_MAX_TIME = TimeValue.valueOf("1min");

  public static final TimeValue DEFAULT_CONNECTION_TIMEOUT        = TimeValue.valueOf("1s");
  public static final TimeValue DEFAULT_SESSION_TIMEOUT           = TimeValue.valueOf("10s");
  public static final TimeValue DEFAULT_MEMBERSHIP_CHECK_INTERVAL = TimeValue.valueOf("1s");

  public static final int       DEFAULT_RETRY_MAX_ATTEMPTS = 5;
  public static final String    DEFAULT_ZK_NAMESPACE = "ubik";
  
  
  private Category log = Log.createCategory(getClass());
  
  private CuratorFramework client;

  private TimeValue membershipCheckInterval;
  
  private volatile boolean started;
  
  private Map<String, ZkGroupRegistration> registrations    = new ConcurrentHashMap<>();
  
  public ZkGroupMembershipService() {
  }
  
  @Override
  public void initialize(Conf config) {
    int    connectionRetryInterval = (int) config.getTimeProperty(CONNECTION_RETRY_INTERVAL, DEFAULT_CONNECTION_RETRY_INTERVAL).getValueInMillis();
    int    connectionRetryMaxTime  = (int) config.getTimeProperty(CONNECTION_RETRY_MAX_TIME, DEFAULT_CONNECTION_RETRY_MAX_TIME).getValueInMillis();
    int    connectionTimeOut       = (int) config.getTimeProperty(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT).getValueInMillis();
    int    sessionTimeout          = (int) config.getTimeProperty(SESSION_TIMEOUT, DEFAULT_SESSION_TIMEOUT).getValueInMillis();
    String namespace               = config.getProperty(NAMESPACE, DEFAULT_ZK_NAMESPACE);
    String serverList              = config.getNotNullProperty(SERVER_LIST);
    membershipCheckInterval        = config.getTimeProperty(MEMBERSHIP_CHECK_INTERVAL, DEFAULT_MEMBERSHIP_CHECK_INTERVAL);
     
    log.info("Initializing Zookeeper connection:");
    log.info("  Connection retry interval: %s ms", connectionRetryInterval);
    log.info("  Connection retry max time: %s ms", connectionRetryMaxTime);
    log.info("  Connection timeout.......: %s ms", connectionTimeOut);
    log.info("  Session timeout..........: %s ms", sessionTimeout);
    log.info("  Namespace................: %s ms", namespace);
    log.info("  Server list..............: %s", serverList);
    
    client = CuratorFrameworkFactory.builder()
      .namespace(namespace)
      .retryPolicy(new RetryUntilElapsed(connectionRetryMaxTime, connectionRetryInterval))
      .connectionTimeoutMs(connectionTimeOut)
      .sessionTimeoutMs(sessionTimeout)
      .connectString(serverList)
      .build();
      
    log.info("Zookeeper connection initialization completed");
  }

  @Override
  public synchronized void start() {
    Assertions.illegalState(started, "Instance already started");
    Assertions.illegalState(client == null, "Cannot start: instance not initialized");
    client.start();
    Hub.getModules().getTaskManager().addTask(new TaskContext(getClass().getSimpleName(), membershipCheckInterval.getValueInMillis()), new Task() {
      @Override
      public void exec(TaskContext ctx) {
        if (started) {
          checkMembership();
        } else {
          ctx.abort();
        }
      }
    });   
    started = true;
  }
  
  @Override
  public synchronized void close() {
    if (started) {
      for (ZkGroupRegistration r : registrations.values()) {
        r.member.close();
      }
      registrations.clear();
      client.close();
      started = false;
    }
  }
  
  @Override
  public synchronized GroupRegistration joinGroup(String groupName, String memberId, byte[] payload, GroupMembershipListener listener)
      throws IOException {
    Assertions.illegalState(!started, "Instance not started (invoke start method)");
    log.debug("Member %s joining group: %s", memberId, groupName);
    GroupMember         member       = new GroupMember(client, groupName.startsWith("/") ? groupName : "/" + groupName, memberId, payload);
    member.start();
    ZkGroupRegistration registration = new ZkGroupRegistration(new RegistrationFacade() {
      @Override
      public void removeRegistration(String memberId) {
        registrations.remove(memberId);
      }
    },
    memberId, member, listener);
    registrations.put(memberId, registration);
    return registration;
  }

  // --------------------------------------------------------------------------
  // Restricted 
  
  void checkMembership() {
    for (ZkGroupRegistration r : registrations.values()) {
      r.checkGroupState();
    }    
  }

  // --------------------------------------------------------------------------
  // Inner classes
  
  interface RegistrationFacade {
    void removeRegistration(String memberId);
  }
 
  static class ZkGroupRegistration implements GroupRegistration {
    
    private static Category log = Log.createCategory(ZkGroupRegistration.class);
    
    private RegistrationFacade      registrations;
    private String                  memberId;
    private GroupMember             member;
    private GroupMembershipListener listener;
    private Set<String>             currentPeers = new ConcurrentSkipListSet<>();
    
    ZkGroupRegistration(RegistrationFacade registrations, final String memberId, GroupMember member, GroupMembershipListener listener) {
      this.registrations = registrations;
      this.memberId      = memberId; 
      this.member        = member;
      this.listener      = listener;
      currentPeers.addAll(Collects.filterAsSet(member.getCurrentMembers().keySet(), new Condition<String>() {
        @Override
        public boolean apply(String otherMemberId) {
          return !otherMemberId.equals(memberId);
        }
      }));
      for (String m : currentPeers) {
        byte[] payload = member.getCurrentMembers().get(m);
        if (payload != null) {
          listener.onMemberDiscovered(m, payload);
        }
      }
    }
    
    @Override
    public String getMemberId() {
      return memberId;
    }
    
    @Override
    public synchronized void leave() {
      registrations.removeRegistration(memberId);
      member.close();
    }
    
    private void checkGroupState() {
      Set<String> newPeers = new HashSet<>();
      Set<String> allPeers = new HashSet<>();

      for (Map.Entry<String, byte[]> m : member.getCurrentMembers().entrySet()) {
        if (!m.getKey().equals(memberId)) {
          if (!currentPeers.contains(m.getKey())) {
            log.debug("Found new peer (adding to member view): %s", m.getKey());
            newPeers.add(m.getKey());
            listener.onMemberDiscovered(m.getKey(), m.getValue());
          }
          allPeers.add(m.getKey());
        }
      }
      
      // remaining peers will be "dead" ones
      currentPeers.removeAll(allPeers);
      try {
        for (String p : currentPeers) {
          log.debug("Found dead peer (removing from member view): %s", p);
          listener.onMemberLeft(p);
        }
      } finally {
        // making sure the following is performed
        currentPeers = allPeers;
      }
    }
  }
}
