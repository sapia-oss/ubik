package org.sapia.ubik.net.netty;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;

@RunWith(MockitoJUnitRunner.class)
public class NettyMessageDecoderTest {

  private NettyRequestDecoder decoder;

  @Mock
  private ChannelHandlerContext context;

  @Mock
  private Channel channel;

  @Before
  public void setUp() {
    decoder = new NettyRequestDecoder();
  }

  @Test
  public void testDecode() throws Exception {

    NettyResponse toEncode = new NettyResponse("test");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = MarshalStreamFactory.createOutputStream(bos);
    oos.writeObject(toEncode);
    oos.flush();
    oos.close();

    byte[] bytes = bos.toByteArray();
    bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    dos.writeInt(bytes.length);
    dos.write(bytes);
    dos.flush();

    bytes = bos.toByteArray();
    ChannelBuffer buf = ChannelBuffers.dynamicBuffer(NettyResponse.PREFIX_LEN + bytes.length);
    buf.writeBytes(bytes);
    NettyResponse decoded = (NettyResponse) decoder.decode(context, channel, buf);
    assertEquals(toEncode.getObject(), decoded.getObject());
  }

}
