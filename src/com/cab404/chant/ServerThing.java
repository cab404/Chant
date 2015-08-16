package com.cab404.chant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

/**
 * Sorry for no comments!
 * Created at 17:54 on 04/07/15
 *
 * @author cab404
 */
public class ServerThing {

    private static String req = "GET /all/img/PH/2bcaa5df2ba616d2598485760a122e72.gif HTTP/1.0\r\n\r\n\r\n";

    public static void main(String[] args) throws IOException {

        LoaningPool<ByteBuffer> loan = new LoaningPool<ByteBuffer>(4) {

            @Override
            ByteBuffer genObject() {
                return ByteBuffer.allocate(1024);
            }

            @Override
            void dispose(ByteBuffer object) {
            }

            @Override
            void clear(ByteBuffer object) {
                object.clear();
            }
        };

        try (LoaningPool<ByteBuffer>.Borrow a = loan.borrow(1)) {
            a.get().put(0, (byte) 12);
        }

        try (LoaningPool<ByteBuffer>.Borrow a = loan.borrow(1)) {
            System.out.println(a.get().array()[0]);
        }


        AbstractSelector selector = SelectorProvider.provider().openSelector();
        for (int i = 1; i <= 5; i++) {

            SocketChannel channel = selector.provider().openSocketChannel();
            channel.connect(new InetSocketAddress("cab404.ru", 80));
            channel.configureBlocking(false);
            ByteBuffer request = ByteBuffer.wrap(req.getBytes());
            channel.write(request);
            ByteBuffer request2 = ByteBuffer.wrap(req.getBytes());
            channel.write(request2);
            channel.register(selector, SelectionKey.OP_READ);

        }

        System.out.println();
        ByteBuffer buffer = ByteBuffer.allocate(2 * 1024 * 1024);

        SelectionKey what = null;
        int downloaded = 0;
        long num = 0;

        while (true) {

            if (what == null) {
                if (selector.keys().isEmpty()) break;
                selector.select(1000);
                if (selector.selectedKeys().isEmpty()) break;
                what = selector.selectedKeys().iterator().next();

            } else {
                SocketChannel ch = (SocketChannel) what.channel();
                int read = ch.read(buffer);

                if (read == 0) {
                    num += buffer.position();
                    buffer.rewind();
                }

                if (read != 0)
                    System.out.println(downloaded + ":" + read + ":" + num);

                if (read == -1) {
                    what.cancel();
                    what = null;
                    downloaded++;
                    num = 0;
                }

            }
        }

    }
}
