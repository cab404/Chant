package com.cab404.chant;


import java.io.IOException;

/**
 * Sorry for no comments!
 * Created at 01:14 on 13/08/15
 *
 * @author cab404
 */
public class WaitressTask implements Runnable {

    private SocketReadInfo info;

    public WaitressTask(SocketReadInfo info) {
        this.info = info;
    }

    boolean markedDead = false;

    @Override
    public void run() {
        int currSize = Methods.buffersLength(info.lastUsedArray);
        if (info.lastSize != currSize) {
            System.out.println("Got some new data " + currSize);
            info.lastSize = currSize;
            info.lal = System.currentTimeMillis();
            info.changed = true;
            int cBufferLength = info.data.length();

            if ((currSize + info.astral.cfg.enlargeCount) / info.astral.cfg.receiveBufferSize >= cBufferLength) {
                info.data.expand(info.astral.cfg.enlargeCount);
            }
        } else {
            long duration = System.currentTimeMillis() - info.lal;
            if (duration > info.tts && info.changed) {
                System.out.println("Send timeout reached on " + currSize + " bytes");
                info.changed = false;
                if (info.astral.assigner.assignHandler(info)) {
                    System.out.println("Selector said we don't need no stinkin' connection!");
                    markedDead = true;
                }
            }
            if (duration > info.ttl) {
                System.out.println("TTL reached.");
                markedDead = true;
            }
        }

        if (!markedDead)
            info.astral.processing.execute(this);
        else {
            System.out.println("Dying right about now.");
            dispose();
        }
    }

    public void dispose() {
        try {
            info.data.free();
            info.channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
