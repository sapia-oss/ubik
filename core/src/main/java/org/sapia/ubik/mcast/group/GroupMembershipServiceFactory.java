package org.sapia.ubik.mcast.group;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.provider.Providers;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

/**
 * A factory of {@link GroupMembershipService} instances.
 * 
 * @author yduchesne
 *
 */
public class GroupMembershipServiceFactory {
  
  private static Category log = Log.createCategory(GroupMembershipServiceFactory.class);

  private GroupMembershipServiceFactory() {
  }
  
  /**
   * @param props a {@link Conf} instance holding at least the property corresponding to {@link Consts#GROUP_MEMBERSHIP_PROVIDER},
   * indicating which {@link GroupMembershipService} implementation to use.
   * @return a new {@link GroupMembershipService}.
   */
  public static GroupMembershipService createGroupMemberShipService(Conf props) {
    String provider = props.getNotNullProperty(Consts.GROUP_MEMBERSHIP_PROVIDER);
    log.info("Creating group membership service %s", provider);
    GroupMembershipService gms = Providers.get().load(GroupMembershipService.class, provider);
    gms.initialize(props);
    return gms;
  }
}
