package com.cab404.chant;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Arrays;

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

                synchronized (this) {
                    selector.select(1000);
                }
                // Wait for sync
                if (selector.selectedKeys().isEmpty() && wakeupFlag) try {
                    System.out.println("Wakeup!");
                    wakeupFlag = false;
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                for (SelectionKey key : selector.selectedKeys()) {

                    SocketChannel channel = (SocketChannel) key.channel();
//                    System.out.println("Catched connection to " + channel.getRemoteAddress());
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

                        key.attach(client);
                    } else {
                        if (client.data.get(client.data.length() - 1).remaining() <= astral.cfg.enlargeBefore)
                            client.data.expand(astral.cfg.enlargeCount);
                    }

                    client.lastUsedArray = Methods.borrowToArray(client.data);

//                    System.out.println("Starting nonblocking read from " + channel + " to " + Arrays.toString(client.lastUsedArray));
//                    long ns = System.currentTimeMillis();
                    channel.read(client.lastUsedArray);
//                    System.out.println("ntime -> " + (System.currentTimeMillis() - ns));

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
