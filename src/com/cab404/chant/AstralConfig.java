package com.cab404.chant;

import java.util.concurrent.TimeUnit;

/**
 * Sorry for no comments!
 * Created at 11:49 on 01/08/15
 *
 * @author cab404
 */
public class AstralConfig {

    /**
     * Number of threads used for task processing.
     */
    public int numberOfProcessingThreads = Runtime.getRuntime().availableProcessors();
    /**
     * Abstract memory limit, since which server will stop allocating memory for cached buffers
     */
    public int maximumReceiveBufferMemory = 200 * 1024;
    /**
     * Standard buffer size
     */
    public int receiveBufferSize = 8 * 1024;
    /**
     * Start number of standard buffers allocated for fetching
     */
    public int startBufferCount = 3;
    /**
     * If there is not enought memory to receive the packet, how much do we additionally allocate?
     */
    public int enlargeCount = 2;
    /**
     * How long do we wait on idle connection
     */
    public long ttl = TimeUnit.SECONDS.toMillis(5);
    /**
     * How long do we wait after connection became idle to send data to receivers.
     */
    public long tts = 0;

    /**
     * After that or less space remains in fetch buffer, we are going to enlarge it
     */
    public long enlargeBefore = 512;

}
