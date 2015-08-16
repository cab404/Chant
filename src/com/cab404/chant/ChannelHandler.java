package com.cab404.chant;

/**
 * Created at 00:00 on 01/08/15
 * @author cab404
 */
public interface ChannelHandler {

    boolean process(SocketReadInfo info);

}
