package org.sapia.ubik.mcast.memory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.group.GroupMembershipListener;
import org.sapia.ubik.mcast.group.GroupMembershipService;
import org.sapia.ubik.mcast.group.GroupRegistration;
import org.sapia.ubik.util.Conf;

/**
 * An in-memory implementation of the {@link GroupMembershipService} interface.
 * 
 * @author yduchesne
 */
public class InMemoryGroupMembershipService implements GroupMembershipService {
  
  private Category log = Log.createCategory(getClass());
  
  private static Map<String, InMemoryGroupRegistration> GLOBAL_REGISTRATIONS = new ConcurrentHashMap<>();
  
  private Set<String> thisInstanceMembers = new HashSet<>();
  
  @Override
  public synchronized GroupRegistration joinGroup(String groupName, String memberId, byte[] payload, GroupMembershipListener listener)
      throws IOException {
    log.debug("Member %s joining group %s", memberId, groupName);
    InMemoryGroupRegistration registration = new InMemoryGroupRegistration(memberId, listener, payload);
    for (InMemoryGroupRegistration r : GLOBAL_REGISTRATIONS.values()) {
      r.listener.onMemberDiscovered(registration.getMemberId(), payload);
      listener.onMemberDiscovered(r.memberId, r.payload);
    }
    GLOBAL_REGISTRATIONS.put(registration.getMemberId(), registration);
    thisInstanceMembers.add(memberId);
    return registration;
  }
  
  public boolean hasRegistration(String memberId) {
    return thisInstanceMembers.contains(memberId);
  }
  
  @Override
  public void initialize(Conf config) {
    // noop
  }
  
  @Override
  public void start() {
    // noop
  }
  
  @Override
  public void close() {
    for (String m : thisInstanceMembers) {
      GLOBAL_REGISTRATIONS.remove(m);
    }
  }
  
  // ==========================================================================
  // Inner classes
  
  private class InMemoryGroupRegistration implements GroupRegistration {
    
    private String memberId;
    private GroupMembershipListener listener;
    private byte[] payload;
    
    private InMemoryGroupRegistration(String memberId, GroupMembershipListener listener, byte[] payload) {
      this.memberId = memberId;
      this.listener = listener;
      this.payload  = payload;
    }
    
    @Override
    public String getMemberId() {
      return memberId;
    }
    
    @Override
    public void leave() {
      thisInstanceMembers.remove(memberId);
      GLOBAL_REGISTRATIONS.remove(memberId);
      for (InMemoryGroupRegistration r : GLOBAL_REGISTRATIONS.values()) {
        r.listener.onMemberLeft(memberId);
      }
    }
    
  }
}
