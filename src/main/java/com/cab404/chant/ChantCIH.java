package com.cab404.chant;

import com.almworks.sqlite4java.SQLiteConnection;
import com.cab404.mandos.ClientInfo;
import com.cab404.mandos.ClientInputHandler;

/**
 * Sorry for no comments!
 * Created at 16:49 on 23/08/15
 *
 * @author cab404
 */
public class ChantCIH implements ClientInputHandler {

    @Override
    public void handleInput(ClientInfo info) {
        new SQLiteConnection();
    }

    @Override
    public void handleConnect(ClientInfo info) {

    }

    @Override
    public void handleTTLReach(ClientInfo info) {

    }

    @Override
    public void handleDisconnect(ClientInfo info) {

    }
}
