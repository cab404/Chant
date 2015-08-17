package com.cab404.chant;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Information about connection. <br/>
 * Created at 03:15 on 13/08/15
 *
 * @author cab404
 */
public class ClientInfo {
    public Astral astral;

    public WaitressTask waitress;
    public SocketChannel channel;

    /**
     * Can change during read
     */
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
     * If data changed since last time it was sent to receivers.
     */
    public boolean changed;
    /**
     * Previous size of data
     */
    public int lastSize;
    /**
     * If connection should be killed now
     */
    public volatile boolean markedDead;
    /**
     * If waitress should be dismissed for now
     */
    public volatile boolean markedIdle;
    /**
     * Place your things here
     */
    public Object customData;
    /**
     * Was 'free' method executed on this?
     */
    public boolean freed;

    /**
     * Disconnects client, closing channel and marking it dead
     */
    public void disconnect() throws IOException {
        markedDead = true;
        if (channel.isOpen()) {
            channel.close();
            astral.handler.handleDisconnect(this);
        }
    }

    /**
     * Frees resources by nulling links to them and atomizing borrow chain.
     */
    public void free() {
        lastUsedArray = null;
        data.free();

        freed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!freed) System.err.println("Resources weren't freed, yet we are finalizing.");
        super.finalize();
    }
}
