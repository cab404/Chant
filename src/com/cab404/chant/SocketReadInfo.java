package com.cab404.chant;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Sorry for no comments!
 * Created at 03:15 on 13/08/15
 *
 * @author cab404
 */
public class SocketReadInfo {
    public Astral astral;
    public WaitressTask waitress;
    public SocketChannel channel;
    public LoaningPool<ByteBuffer>.Borrow data;
    public ByteBuffer[] lastUsedArray;

    /**
     * Last ALive - when was the last time channel has shown any signs of life.
     */
    public long lal;
    /**
     * Time To Live - how long connection should be idle to shut it down completely
     */
    public long ttl;
    /**
     * Time To Send - how long this connection should be idle to send it to receivers
     */
    public long tts;
    /**
     * Last Time Send - when was the last time data was processed in receivers
     */
    public long lts;
    /**
     * If data changed since last time it was sent to receivers.
     */
    public boolean changed;
    /**
     * Previous size of data
     */
    public int lastSize;
}
