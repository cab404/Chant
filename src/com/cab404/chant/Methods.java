package com.cab404.chant;

import java.nio.ByteBuffer;

/**
 * Static methods. Might be first optimization targets in future.
 * Created at 01:45 on 13/08/15
 *
 * @author cab404
 */
public class Methods {

    public static ByteBuffer[] borrowToArray(LoaningPool<ByteBuffer>.Borrow borrow) {
        ByteBuffer[] bf = new ByteBuffer[borrow.length()];
        int i = 0;
        for (ByteBuffer buf : borrow) {
            bf[i++] = buf;
        }
        return bf;
    }

    public static int buffersLength(ByteBuffer[] array) {
        int len = 0;
        for (ByteBuffer buf : array) {
            len += buf.position();
            if (buf.remaining() != 0)
                break;
        }
        return len;
    }

}
