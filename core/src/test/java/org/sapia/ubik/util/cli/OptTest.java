package org.sapia.ubik.util.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OptTest {

  @Test
  public void testGetTrimmedValueOrBlankWithNull() {
    Opt opt = new Opt(Opt.Type.SWITCH, "test", null);
    assertEquals("", opt.getTrimmedValueOrBlank());
  }

  @Test
  public void testGetTrimmedValueOrBlankWithEmptyString() {
    Opt opt = new Opt(Opt.Type.SWITCH, "test", "");
    assertEquals("", opt.getTrimmedValueOrBlank());
  }

  @Test
  public void testIsBlankOrNullWithNull() {
    Opt opt = new Opt(Opt.Type.SWITCH, "test", null);
    assertTrue(opt.isBlankOrNull());
  }

  @Test
  public void testIsBlankOrNullWithEmptyString() {
    Opt opt = new Opt(Opt.Type.SWITCH, "test", "");
    assertTrue(opt.isBlankOrNull());
  }

  @Test
  public void testGetIntValue() {
    Opt opt = new Opt(Opt.Type.SWITCH, "test", "1");
    opt.getIntValue();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetInvalidIntValue() {
    Opt opt = new Opt(Opt.Type.SWITCH, "test", "a");
    opt.getIntValue();
  }

}
