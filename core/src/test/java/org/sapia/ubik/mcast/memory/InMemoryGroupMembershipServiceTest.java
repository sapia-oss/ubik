package org.sapia.ubik.mcast.memory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.mcast.group.GroupMembershipListener;
import org.sapia.ubik.mcast.group.GroupMembershipServiceFactory;
import org.sapia.ubik.mcast.group.GroupRegistration;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryGroupMembershipServiceTest {
 
  @Mock
  private GroupMembershipListener listener1, listener2;
  
  private InMemoryGroupMembershipService memberShip;
  

  @Before
  public void setUp() throws Exception {
    memberShip = new InMemoryGroupMembershipService();
  }
  
  @After
  public void tearDown() {
    memberShip.close();
  }
  

  @Test
  public void testJoinGroup() throws IOException {
    memberShip.joinGroup("test", "member-1", new byte[0], listener1);
    memberShip.joinGroup("test", "member-2", new byte[0], listener2);

    verify(listener1).onMemberDiscovered(eq("member-2"), any(byte[].class));
    verify(listener2).onMemberDiscovered(eq("member-1"), any(byte[].class));
    assertTrue(memberShip.hasRegistration("member-1"));
    assertTrue(memberShip.hasRegistration("member-2"));
  }
  
  @Test
  public void testLeaveGroup() throws IOException {
    GroupRegistration registration = memberShip.joinGroup("test", "member-1", new byte[0], listener1);
    memberShip.joinGroup("test", "member-2", new byte[0], listener2);
    
    registration.leave();

    verify(listener2).onMemberLeft("member-1");
    assertFalse(memberShip.hasRegistration("member-1"));
  }
  
  @Test
  public void testCreationWithFactory() throws Exception {
    GroupMembershipServiceFactory.createGroupMemberShipService(Conf.newInstance().addProperties(Consts.GROUP_MEMBERSHIP_PROVIDER, Consts.GROUP_MEMBERSHIP_PROVIDER_MEMORY));
  }


}
