package com.cab404.chant;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Sorry for no comments!
 * Created at 02:31 on 01/08/15
 *
 * @author cab404
 */
public interface ChannelHandlerAssigner {

    boolean assignHandler(SocketReadInfo info);
}
