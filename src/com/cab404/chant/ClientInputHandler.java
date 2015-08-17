package com.cab404.chant;

/**
 * Sorry for no comments!
 * Created at 02:31 on 01/08/15
 *
 * @author cab404
 */
public interface ClientInputHandler {
    void handleInput(ClientInfo info);
    void handleConnect(ClientInfo info);
    void handleTTLReach(ClientInfo info);
    void handleDisconnect(ClientInfo info);
}
