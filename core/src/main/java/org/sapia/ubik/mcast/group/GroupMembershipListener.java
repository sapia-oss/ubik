package org.sapia.ubik.mcast.group;

/**
 * An instance of this interface is
 * 
 * @author yduchesne
 *
 */
public interface GroupMembershipListener {
  
  /**
   * @param payload the arbitrary payload provided by the discovered member.
   */
  void onMemberDiscovered(String memberId, byte[] payload);
  
  /**
   * @param memberId the ID of a group member.
   */
  void onMemberLeft(String memberId);
}