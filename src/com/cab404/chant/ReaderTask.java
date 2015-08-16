package com.cab404.chant;

import java.io.IOException;
import java.nio.channels.*;

/**
 * Sorry for no comments!
 * Created at 23:36 on 31/07/15
 *
 * @author cab404
 */
public class ReaderTask implements Runnable {
    private final Astral astral;
    private volatile boolean wakeupFlag = false;
    private Selector selector;

    public ReaderTask(Astral where) throws IOException {
        selector = Selector.open();
        this.astral = where;
    }

    public void wakeup() {
        wakeupFlag = true;
        selector.wakeup();
    }

    public void goodnight() {
        wakeupFlag = false;
    }

    public void register(SelectableChannel ch) throws ClosedChannelException {
        selector.wakeup();
        synchronized (this) {
            ch.register(selector, SelectionKey.OP_READ);
        }
    }

    @Override
    public void run() {

        System.out.println("Started reader task");
        while (!astral.shutdownFlag) {
            try {

                selector.select();

                // Wait for sync
                if (selector.selectedKeys().isEmpty() && wakeupFlag) {
                    System.out.println("Wakeup!");
                    while (wakeupFlag)
                        Thread.yield();
                }

                for (SelectionKey key : selector.selectedKeys()) {

                    SocketChannel channel = (SocketChannel) key.channel();
                    SocketReadInfo client = (SocketReadInfo) key.attachment();

                    if (client == null) {
                        System.out.println("It's a new client");

                        client = new SocketReadInfo();
                        client.channel = channel;
                        client.astral = astral;
                        client.data = astral.bufferPool.borrow(astral.cfg.startBufferCount);

                        client.lal = System.currentTimeMillis();
                        client.ttl = astral.cfg.ttl;
                        client.tts = astral.cfg.tts;
                        client.lastUsedArray = Methods.borrowToArray(client.data);
                        key.attach(client);
                    } else {
                        if (client.data.length() == 0) {
                            System.out.println("Client disposed.");
                            continue;
                        }
                        if (client.data.get(client.data.length() - 1).remaining() <= astral.cfg.enlargeBefore) {
                            client.data.expand(astral.cfg.enlargeCount);
                            client.lastUsedArray = Methods.borrowToArray(client.data);
                        }
                    }

                    channel.read(client.lastUsedArray);

                    if (client.waitress == null)
                        astral.processing.execute(client.waitress = new WaitressTask(client));
                }
                selector.selectedKeys().clear();

            } catch (IOException e) {
//                System.err.println("Cannot select." + e);
            }
        }
    }

}
