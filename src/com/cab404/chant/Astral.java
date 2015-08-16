package com.cab404.chant;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * We have all the stuff in astral
 * Created at 20:58 on 31/07/15
 * <p/>
 * initial buffer size
 *
 * @author cab404
 */
public class Astral {
    public final ThreadPoolExecutor processing;
    public final LoaningPool<ByteBuffer> bufferPool;
    public final AstralConfig cfg;

    public final ChannelHandlerAssigner assigner;

    public volatile boolean shutdownFlag = false;

    public Astral(AstralConfig config, ChannelHandlerAssigner assigner) {
        this.cfg = config;
        this.assigner = assigner;
        this.bufferPool = new ByteBufferLoaningPool(Astral.this.cfg.maximumReceiveBufferMemory / Astral.this.cfg.receiveBufferSize);
        this.processing = new ThreadPoolExecutor(
                1,
                cfg.numberOfProcessingThreads,
                20, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(20)
        );
    }

    private class ByteBufferLoaningPool extends LoaningPool<ByteBuffer> {
        public ByteBufferLoaningPool(int size) {
            super(size);
        }

        @Override
        ByteBuffer genObject() {
            return ByteBuffer.allocate(cfg.receiveBufferSize);
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
}
