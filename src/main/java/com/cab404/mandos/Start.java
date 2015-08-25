package com.cab404.mandos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Sorry for no comments!
 * Created at 23:36 on 06/07/15
 *
 * @author cab404
 */
public class Start {

    public static void main(String... args) throws IOException {
        Astral astral = new Astral(new AstralConfiguration(), new SimpleCIH());
        new ListenerLoop(astral).run();
    }

    private static class SimpleCIH implements ClientInputHandler {
        @Override
        public void handleInput(final ClientInfo info) {
            info.astral.processing.execute(new Reply(info));
        }

        @Override
        public void handleConnect(ClientInfo info) {
            System.out.println("Connected to " + info.channel.socket().getInetAddress());
        }

        @Override
        public void handleTTLReach(ClientInfo info) {
            System.out.println("TTL Reached by " + info.channel.socket().getInetAddress());
            info.astral.processing.execute(new Disconnect(info));
        }

        @Override
        public void handleDisconnect(final ClientInfo info) {
            System.out.println("Planned disconnect on " + info.channel.socket().getInetAddress());
        }

        private static class Reply implements Runnable {
            private final ClientInfo info;

            public Reply(ClientInfo info) {
                this.info = info;
            }

            @Override
            public void run() {
                System.out.println("SOME DATA! -> ");
                StringBuilder data = new StringBuilder();
                for (int i = 0; i < info.lastUsedArray.length; i++) {
                    ByteBuffer bb = info.lastUsedArray[i];
                    data.append(new String(bb.array(), 0, bb.position()));
                }
                System.out.println(data);

                System.out.println("Responding with helloworld");
                try {
                    info.channel.write(
                            ByteBuffer.wrap(
                                    ("Hello, client at " + info.channel.socket().getInetAddress() + "!").getBytes()
                            )
                    );
                    info.astral.processing.execute(new Disconnect(info));
                } catch (IOException e) {
                    System.out.println("Exception while writing " + e);
                }

            }
        }

        private static class Disconnect implements Runnable {
            private final ClientInfo info;

            public Disconnect(ClientInfo info) {
                this.info = info;
            }

            @Override
            public void run() {
                try {
                    info.disconnect();
                    info.free();
                    System.out.println("Disconnected and freed resources");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
