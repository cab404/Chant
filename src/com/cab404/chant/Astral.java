package com.cab404.chant;

import java.nio.ByteBuffer;
import java.util.Timer;
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
    public final LoaningPool<ByteBuffer> bufferPool;
    public final ThreadPoolExecutor processing;
    public final ThreadPoolExecutor waitresses;
    public final ClientInputHandler handler;
    /**
     * Buffer shrinking, data pushing, etc.
     */
    public final Timer maintenance;
    /**
     * Configuration :/
     */
    public final AstralConfig cfg;
    public final ReaderTask rt;
    
    public volatile boolean shutdownFlag = false;

    public Astral(AstralConfig config, ClientInputHandler handler) {
        this.cfg = config;
        this.handler = handler;
        this.bufferPool = new ByteBufferLoaningPool(this, Astral.this.cfg.maximumReceiveBufferMemory / Astral.this.cfg.receiveBufferSize);
        this.processing = new ThreadPoolExecutor(
                1,
                cfg.numberOfProcessingThreads,
                20, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100),
                new PrioritizedThreadFactory(Thread.MAX_PRIORITY)
        );
        this.waitresses = new ThreadPoolExecutor(
                1,
                cfg.numberOfProcessingThreads,
                20, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100),
                new PrioritizedThreadFactory(Thread.MIN_PRIORITY)
        );
        this.maintenance = new Timer("maintenance");
        this.rt = new ReaderTask(this);
    }

}
