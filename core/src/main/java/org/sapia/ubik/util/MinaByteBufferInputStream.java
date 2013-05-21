package org.sapia.ubik.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.mina.common.ByteBuffer;

/**
 * Implements an {@link InputStream} over a {@link ByteBuffer}.
 * 
 * @author yduchesne
 * 
 */
public class MinaByteBufferInputStream extends InputStream {

	private ByteBuffer buf;

	public MinaByteBufferInputStream(ByteBuffer buf) {
		this.buf = buf;
	}
	
	@Override
	public int available() throws IOException {
	  return buf.remaining();
	}

	public int read() throws IOException {
		if (!buf.hasRemaining()) {
			return -1;
		}
    return buf.get() & 0xFF;
	}

	public int read(byte[] bytes, int off, int len)
	    throws IOException {
		if (!buf.hasRemaining()) {
			return -1;
		}

		len = Math.min(len, buf.remaining());
		buf.get(bytes, off, len);
		return len;
	}

}
