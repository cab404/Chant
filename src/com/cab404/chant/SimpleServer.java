package com.cab404.chant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Sorry for no comments!
 * Created at 14:46 on 06/07/15
 *
 * @author cab404
 */
public class SimpleServer implements Runnable {

    @Override
    public void run() {
        try {

            System.out.println("Initializing selectors");
            Selector connect_selector = Selector.open();

            System.out.println("Initializing data input");
            ServerSocketChannel ssock = ServerSocketChannel.open();

            System.out.println("Initializing configuration");
            Astral astral = new Astral(new AstralConfig(), new ChannelHandlerAssigner() {
                @Override
                public boolean assignHandler(SocketReadInfo info) {
                    System.out.println("SOME DATA! -> " + new String(info.lastUsedArray[0].array()));
                    System.out.println("Responding with helloworld");
                    try {
                        info.channel.write(
                                ByteBuffer.wrap(
                                        ("Hello, client at " + info.channel.socket().getInetAddress() + "!").getBytes()
                                )
                        );
                    } catch (IOException e) {
                        System.out.println("Exception while writing " + e);
                    }
                    return true;
                }
            });

            System.out.println("Initializing reading threads");
            ReaderTask rt;
            rt = new ReaderTask(astral);
            new Thread(rt, "RT #1").start();

            System.out.println("Configuring connection acceptor");

            ssock.configureBlocking(true);
            ssock.bind(new InetSocketAddress("127.0.0.1", 6934));
            ssock.configureBlocking(false);
            ssock.register(connect_selector, SelectionKey.OP_ACCEPT);

            System.out.println("Starting infinite loop");
            //noinspection InfiniteLoopStatement
            while (true) {
                int select = connect_selector.select();
                if (select != 0) {
                    rt.wakeup();
                    synchronized (rt) {
                        for (SelectionKey ignored : connect_selector.selectedKeys()) {
                            SocketChannel client = ssock.accept();
                            System.out.println("Accepted connection...");
                            client.configureBlocking(false);
                            System.out.println("Configured connection...");
                            rt.register(client);
                            System.out.println("...initialized connection sequence to " + client.socket().getInetAddress());
                        }
                    }
                }
                connect_selector.selectedKeys().clear();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
