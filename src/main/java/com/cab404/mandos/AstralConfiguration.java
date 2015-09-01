package com.cab404.mandos;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Sorry for no comments!
 * Created at 11:49 on 01/08/15
 *
 * @author cab404
 */
public class AstralConfiguration {

    public InetSocketAddress bindTo = new InetSocketAddress(6934);
    /**
     * Number of threads used for task processing.
     */
    public int numberOfProcessingThreads = Runtime.getRuntime().availableProcessors();
    /**
     * Abstract memory limit, since which server will stop allocating memory for cached buffers, in bytes
     */
    public int maximumReceiveBufferMemory = 200 * 1024;
    /**
     * Receive buffer size, in bytes
     */
    public int receiveBufferSize = 1024;
    /**
     * Start number of standard buffers initially allocated for fetching
     */
    public int startBufferCount = 3;
    /**
     * If there is not enough memory to receive the packet, how many receive buffers do we additionally allocate?
     */
    public int enlargeCount = 2;
    /**
     * Default for new clients:
     * How long do we wait on idle connection until closing it, in milliseconds. Set -1 to keep it forever.
     */
    public long ttl = TimeUnit.SECONDS.toMillis(5);
    /**
     * Default for new clients:
     * How long do we wait after connection became idle to send data to receivers, in milliseconds.
     */
    public long tts = 1;

    /**
     * How long we are looking at zero byte input till waitress is dismissed
     */
    public long pollingPeriod = 10;

    /**
     * How long we are looking at zeros till waitress is dismissed
     */
    public long ttlCheckPeriod = 1000;

    /**
     * After that or less bytes of free space remains in receive buffer, we are going to enlarge it.
     * Should be less than receive buffer size.
     */
    public long enlargeBefore = 512;

}
