package com.cab404.chant;

import java.io.IOException;
import java.nio.channels.*;

/**
 * Starts reading into buffers and manages waitresses. <br/>
 * Created at 23:36 on 31/07/15
 *
 * @author cab404
 */
public class ReaderTask implements Runnable {

    private volatile boolean pauseFlag = false;

    private final Object wakeupLock = new Object();

    private final Astral astral;

    private TTLChecker ttlChecker;

    private Selector selector;

    public ReaderTask(Astral where) {
        this.astral = where;
    }

    /**
     * Initializes selector.
     */
    public void initialize() throws IOException {
        selector = Selector.open();
        astral.maintenance.schedule(ttlChecker = new TTLChecker(astral), astral.cfg.ttl, astral.cfg.ttlCheckPeriod);
    }

    /**
     * Cancels select() on reader thread, and waits until {@link #resume()} is invoked.
     * NOT THREAD SAFE. pause() and resume() should be invoked one after another.
     */
    public void pause() {
        pauseFlag = true;
        selector.wakeup();
    }

    /**
     * Continues running after {@link #pause()} was invoked.
     * NOT THREAD SAFE. pause() and resume() should be invoked one after another.
     */
    public void resume() {
        pauseFlag = false;
        synchronized (wakeupLock) {
            wakeupLock.notify();
        }
    }

    /**
     * Registers new channel in RT's selector.
     * Note that you should add new channels in between {@link #pause()} and {@link #resume()} calls.
     */
    public void register(SelectableChannel ch) throws ClosedChannelException {
        if (selector == null) throw new RuntimeException("Not yet initialized!");
        selector.wakeup();
        ch.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Initializes new client.
     */
    private ClientInfo initNewClient(SelectionKey key) {
        ClientInfo client = new ClientInfo();

        client.channel = (SocketChannel) key.channel();
        client.astral = astral;
        client.data = astral.bufferPool.borrow(astral.cfg.startBufferCount);

        client.lal = System.currentTimeMillis();
        client.ttl = astral.cfg.ttl;
        client.tts = astral.cfg.tts;
        client.lastUsedArray = Methods.borrowToArray(client.data);

        ttlChecker.add(client);

        return client;
    }

    @Override
    public void run() {

        System.out.println("Started reader task");
        while (!astral.shutdownFlag) {
            try {

                selector.select();

                // Wait for sync
                if (selector.selectedKeys().isEmpty() && pauseFlag) {
                    try {
                        while (pauseFlag)
                            synchronized (wakeupLock) {
                                wakeupLock.wait();
                            }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                for (SelectionKey key : selector.selectedKeys()) {

                    SocketChannel channel = (SocketChannel) key.channel();
                    ClientInfo client = (ClientInfo) key.attachment();

                    if (client == null) {

                        key.attach(client = initNewClient(key));
                        astral.handler.handleConnect(client);

                    } else {

                        if (client.data.length() == 0 || !client.channel.isOpen() || client.freed) {
                            System.err.println("Disposed client in read thread.");
                            continue;
                        }
                        // Increasing array size (if nessesary).
                        if (client.data.get(client.data.length() - 1).remaining() <= astral.cfg.enlargeBefore) {
                            client.data.expand(astral.cfg.enlargeCount);
                            client.lastUsedArray = Methods.borrowToArray(client.data);
                        }

                    }

                    long readCount;
                    try {
                        readCount = channel.read(client.lastUsedArray);
                    } catch (AsynchronousCloseException e) {
                        // Channel was closed, we'll just wait.
                        readCount = 0;
                    }

                    if (readCount == -1) {
                        client.markedIdle = true;
                        channel.shutdownInput();
                        key.cancel();
                    } else {
                        if (client.markedIdle) {
                            client.markedIdle = false;
                            astral.waitresses.execute(client.waitress);
                        }
                    }

                    if (client.waitress == null)
                        astral.waitresses.execute(client.waitress = new WaitressTask(client));
                }
                selector.selectedKeys().clear();

            } catch (IOException e) {
                System.err.println("Cannot select. " + e);
            }
        }
    }

}
