package org.sapia.ubik.rmi.server.transport.mina;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;

public class MinaResponseEncoderTest {

  private MinaResponseEncoder encoder;
  private IoSession session;
  private Map<String, Object> sessionData;
  private TestEncoderOutput output;

  @Before
  public void setUp() throws Exception {
    encoder = new MinaResponseEncoder();
    sessionData = new HashMap<String, Object>();
    session = mock(IoSession.class);

    when(session.getAttribute(anyString())).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return sessionData.get(invocation.getArguments()[0]);
      }
    });

    when(session.setAttribute(anyString(), anyObject())).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return sessionData.put((String) invocation.getArguments()[0], invocation.getArguments()[1]);
      }
    });
    output = new TestEncoderOutput();
  }

  @Test
  public void testEncode() throws Exception {
    String toEncode = "THIS_IS_A_TEST";
    MinaResponse res = new MinaResponse();
    res.setObject(toEncode);

    encoder.encode(session, res, output);
    int length = output.buf.getInt();
    byte[] bytes = new byte[length];
    output.buf.get(bytes);

    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    ObjectInputStream ois = MarshalStreamFactory.createInputStream(bis);
    assertEquals(toEncode, ois.readObject());

  }

}
