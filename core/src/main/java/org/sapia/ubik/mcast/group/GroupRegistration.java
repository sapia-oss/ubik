package org.sapia.ubik.mcast.group;

/**
 * Models a registration to a group.
 * 
 * @see GroupMembershipService#joinGroup(String, byte[])
 * 
 * @author yduchesne
 * 
 */
public interface GroupRegistration {
  
  /**
   * return the unique identifier of "this" member.
   */
  String getMemberId();
  
  /**
   * Triggers leaving the group registered to.
   */
  void leave();
  
}