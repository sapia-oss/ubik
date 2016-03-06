package org.sapia.ubik.mcast.group;

import java.io.IOException;

import org.sapia.ubik.util.Conf;

/**
 * Models the behavior for managing group membership.
 * 
 * @author yduchesne
 *
 */
public interface GroupMembershipService {
  
  /**
   * @param groupName the name of the group to join.
   * @param memberId the member ID to use (must be unique within group).
   * @param payload an arbitrary payload.
   * @param a {@link GroupMembershipListener} to notify when other members appear.
   * @return a {@link GroupRegistration}.
   * @throws IOException if an I/O error occurs joining the group.
   */
  public GroupRegistration joinGroup(String groupName, String memberId, byte[] payload, GroupMembershipListener listener) throws IOException;
  
  /**
   * @param config the {@link Conf} instance to use for retrieving configuration properties.
   */
  public void initialize(Conf config);
  
  /**
   * Starts this instance.
   */
  public void start();
  
  /**
   * Releases this instance's resources.
   */
  public void close();

}
