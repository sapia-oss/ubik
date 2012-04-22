package org.sapia.ubik.util.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Test;

public class CmdTest {
	
	
	@Test
	public void testGetOpts() {
	}

	@Test
	public void testFromArgsWithArgsOnly() {
		String args[] = new String[]{ "arg1", "arg2" };
		Cmd cmd = Cmd.fromArgs(args);
		assertNotNull(cmd.getOpt("arg1"));
		assertNotNull(cmd.getOpt("arg2"));
		assertNull(cmd.getOpt("arg1").getValue());
		assertNull(cmd.getOpt("arg2").getValue());
		assertEquals(Opt.Type.ARG, cmd.getOpt("arg1").getType());
		assertEquals(Opt.Type.ARG, cmd.getOpt("arg2").getType());		
	}
	
	@Test
	public void testFromArgsWithSwitchesOnly() {
		String args[] = new String[]{ "-sw1", "val1", "-sw2", "val2" };
		Cmd cmd = Cmd.fromArgs(args);
		assertEquals(2, cmd.getOpts().size());
		assertNotNull(cmd.getOpt("sw1"));
		assertNotNull(cmd.getOpt("sw2"));
		assertEquals("val1", cmd.getOpt("sw1").getValue());
		assertEquals("val2", cmd.getOpt("sw2").getValue());
		assertEquals(Opt.Type.SWITCH, cmd.getOpt("sw1").getType());
		assertEquals(Opt.Type.SWITCH, cmd.getOpt("sw2").getType());		
	}
	
	@Test
	public void testFromArgsSwitchWithoutValue() {
		String args[] = new String[]{ "-sw1", "val1", "-sw2", "val2", "-sw3" };
		Cmd cmd = Cmd.fromArgs(args);
		assertEquals(3, cmd.getOpts().size());		
		System.out.println(cmd.toString());
		assertNull(cmd.getOpt("sw3").getValue());
		assertEquals(Opt.Type.SWITCH, cmd.getOpt("sw3").getType());
	}	
	
	@Test
	public void testFromArgsWithMixture() {
		String args[] = new String[]{ "arg1", "-sw1", "val1", "-sw2", "val2", "arg2"};
		Cmd cmd = Cmd.fromArgs(args);
		assertEquals(4, cmd.getOpts().size());
		
		assertNotNull(cmd.getOpt("arg1"));
		assertNotNull(cmd.getOpt("arg2"));
		assertNull(cmd.getOpt("arg1").getValue());
		assertNull(cmd.getOpt("arg2").getValue());
		assertEquals(Opt.Type.ARG, cmd.getOpt("arg1").getType());
		assertEquals(Opt.Type.ARG, cmd.getOpt("arg2").getType());		
		
		assertNotNull(cmd.getOpt("sw1"));
		assertNotNull(cmd.getOpt("sw2"));
		assertEquals("val1", cmd.getOpt("sw1").getValue());
		assertEquals("val2", cmd.getOpt("sw2").getValue());
		assertEquals(Opt.Type.SWITCH, cmd.getOpt("sw1").getType());
		assertEquals(Opt.Type.SWITCH, cmd.getOpt("sw2").getType());		
	}		

	@Test
	public void testFromArgsMultipleSwitchesWithoutValue() {
		String args[] = new String[]{ "-sw1", "val1", "-sw2", "-sw3", "val2", "-sw4" };
		Cmd cmd = Cmd.fromArgs(args);
		assertEquals(4, cmd.getOpts().size());
		assertEquals(Opt.Type.SWITCH, cmd.getOpt("sw2").getType());
		assertEquals(Opt.Type.SWITCH, cmd.getOpt("sw4").getType());
		assertNull(cmd.getOpt("sw2").getValue());
		assertNull(cmd.getOpt("sw4").getValue());
	}
	
	@Test
	public void testFromArgsSwitchWithoutName() {
		String args[] = new String[]{ "-sw1", "val1", "-", "-sw3", "val2", "-sw4" };
		Cmd cmd = Cmd.fromArgs(args);
		assertEquals(3, cmd.getOpts().size());
	}			
	
	@Test
	public void testGetOptWithValue() {
		String args[] = new String[]{ "-sw1", "val1" };
		Cmd cmd = Cmd.fromArgs(args);
		assertEquals("val1", cmd.getOptWithValue("sw1").getTrimmedValueOrBlank());
	} 
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetOptWithValueNoValue() {
		String args[] = new String[]{ "-sw1" };
		Cmd cmd = Cmd.fromArgs(args);
		assertEquals("val1", cmd.getOptWithValue("sw1").getTrimmedValueOrBlank());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetOptWithValueBlank() {
		String args[] = new String[]{ "-sw1", "" };
		Cmd cmd = Cmd.fromArgs(args);
		assertEquals("val1", cmd.getOptWithValue("sw1").getTrimmedValueOrBlank());
	}		

	@Test
	public void testGetSwitchesAsProperties() {
		String args[] = new String[]{ "-sw1", "val1", "-sw2", "-sw3", "val2"};
		Cmd cmd = Cmd.fromArgs(args);
		Properties props = cmd.getSwitchesAsProperties();
		assertEquals(2, props.size());
		
		assertEquals("val1", props.getProperty("sw1"));
		assertEquals("val2", props.getProperty("sw3"));
	}

}
