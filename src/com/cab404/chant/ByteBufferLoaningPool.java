package com.cab404.chant;

import java.nio.ByteBuffer;

/**
 * Sorry for no comments!
 * Created at 06:47 on 17/08/15
 *
 * @author cab404
 */
class ByteBufferLoaningPool extends LoaningPool<ByteBuffer> {
    private Astral astral;

    public ByteBufferLoaningPool(Astral astral, int size) {
        super(size);
        this.astral = astral;
    }

    @Override
    ByteBuffer genObject() {
        return ByteBuffer.allocate(astral.cfg.receiveBufferSize);
    }

    @Override
    void dispose(ByteBuffer object) {
        // noop
    }

    @Override
    void clear(ByteBuffer object) {
        object.clear();
    }
}
