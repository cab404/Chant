package com.cab404.mandos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Sorry for no comments!
 * Created at 09:56 on 25/08/15
 *
 * @author cab404
 */
public class ListenerLoop implements Runnable {

    public final Astral astral;

    public ListenerLoop(Astral astral) {
        this.astral = astral;
    }

    @Override
    public void run() {
        try {
            
            System.out.println("Initializing...");

            Selector connect_selector = Selector.open();
            ServerSocketChannel ssock = ServerSocketChannel.open();

            astral.rt.initialize();

            astral.maintenance.schedule(new TTLChecker(astral), 1000, 1000);

            new Thread(astral.rt, "RT").start();

            ssock.configureBlocking(true);
            ssock.bind(astral.cfg.bindTo);
            ssock.configureBlocking(false);
            ssock.register(connect_selector, SelectionKey.OP_ACCEPT);

            System.out.println("Starting.");

            //noinspection InfiniteLoopStatement
            while (!astral.collapse) {
                int select = connect_selector.select();
                if (select != 0) {
                    astral.rt.pause();
                    for (SelectionKey ignored : connect_selector.selectedKeys()) {
                        SocketChannel client = ssock.accept();
                        client.configureBlocking(false);
                        astral.rt.register(client);
                        System.out.println("Initialized connection sequence to " + client.socket().getInetAddress());
                    }
                    astral.rt.resume();
                }
                connect_selector.selectedKeys().clear();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
