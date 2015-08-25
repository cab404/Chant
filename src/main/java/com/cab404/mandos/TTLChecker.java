package com.cab404.mandos;

import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Used for pereodic reading thread invokations, as reading thread uses
 * Created at 05:14 on 17/08/15
 *
 * @author cab404
 */
public class TTLChecker extends TimerTask {

    // TODO: Make global client list
    private final CopyOnWriteArraySet<ClientInfo> toCheck;
    private final Astral astral;

    public TTLChecker(Astral astral) {
        this.astral = astral;
        toCheck = new CopyOnWriteArraySet<>();
    }

    public void add(ClientInfo info) {
        toCheck.add(info);
    }

    @Override
    public void run() {
        for (ClientInfo info : toCheck) {
            if (info.markedDead)
                toCheck.remove(info);
            else {
                if (info.ttl != -1 && System.currentTimeMillis() - info.lal > info.ttl) {
                    astral.handler.handleTTLReach(info);
                    info.markedDead = true;
                }
            }
        }
    }

}
