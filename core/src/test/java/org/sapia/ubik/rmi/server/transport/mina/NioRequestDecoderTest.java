package org.sapia.ubik.rmi.server.transport.mina;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.mina.NioRequestDecoder;

public class NioRequestDecoderTest {

	private IoSession 					session;
	private Map<String, Object> sessionData;


	@Before
  public void setUp() throws Exception {
		sessionData = new HashMap<String, Object>();
		session 		= mock(IoSession.class);
		
		when(session.getAttribute(anyString())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
			  return sessionData.get(invocation.getArguments()[0]);
			}
		});
		
		when(session.setAttribute(anyString(), anyObject())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
			  return sessionData.put((String)invocation.getArguments()[0], invocation.getArguments()[1]);
			}
		});		

  }

	@Test
  public void testDoDecode() throws Exception{
    
    String toEncode = "THIS_IS_A_TEST";
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = MarshalStreamFactory.createOutputStream(bos);
    oos.writeObject(toEncode);
    oos.flush();

    byte[] bytes = bos.toByteArray();
    bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    dos.writeInt(bytes.length);
    dos.write(bytes);
    dos.flush();
    
    NioRequestDecoder decoder = new NioRequestDecoder();
    TestDecoderOutput out = new TestDecoderOutput();

    bytes = bos.toByteArray();
    ByteBuffer buf = ByteBuffer.allocate(4+bytes.length ,true);
    buf.put(bytes);
    buf.flip();
    decoder.doDecode(session, buf, out);
    assertEquals(toEncode, out.msg);
  }

}
