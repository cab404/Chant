package com.cab404.mandos;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clever girl in sexy dress... errr, I mean, task which
 * handles data manipulation and schedules pushes to ClientInputHandler.
 * Created at 01:14 on 13/08/15
 *
 * @author cab404
 */
public class WaitressTask implements Runnable {

    private ClientInfo info;
    /* 
     * Made for possible situation, when waitress is dismissed as idle, and then immediately added 
     * back - in that case if waitresses are assigned to different threads in thread pool executor, 
     * unwanted concurrency may occur.
     */
    private final Lock timeTravelingWaitressLock = new ReentrantLock();

    public WaitressTask(ClientInfo info) {
        this.info = info;
    }

    @Override
    public void run() {
        /* Simple */
        if (info.freed || info.markedDead) {
            System.out.println("We're dead, sorry.");
            return;
        }
        if (!timeTravelingWaitressLock.tryLock()) {
            System.out.println("Ultra-rare time traveling waitress detected!");
            return;
        }
        try {
            /*
             * Yes, I know data can change during execution of this method, but it's should be fine :3
             */
            int currSize = Methods.buffersLength(info.lastUsedArray);

            if (info.lastSize != currSize) {

                info.lastSize = currSize;
                info.lal = System.currentTimeMillis();
                info.changed = true;

            }

            /* If tts is zero, then we're sending data straightaway - so we can't just write 'else' block */
            if (info.lastSize == currSize || info.tts == 0) {

                /* Time since last update */
                long elapsed = System.currentTimeMillis() - info.lal;
                
                /* Checking if we are idle */
                if (elapsed >= info.astral.cfg.pollingPeriod)
                    info.markedIdle = true;
                /* If we are idle, then sending all data to input handler and exiting for now. */
                if (info.markedIdle) {
                    System.out.println("Dismissed waitress");
                    info.astral.handler.handleInput(info);
                    info.changed = false;
                    return;
                }
                
                /* If tts reached, then sending data to CIH */
                if (elapsed >= info.tts && info.changed) {
                    info.astral.handler.handleInput(info);
                    info.changed = false;
                }

            }

            /* Dying if client is marked as dead */
            if (!info.markedDead)
                info.astral.processing.execute(this);

        } finally {
            timeTravelingWaitressLock.unlock();
        }

    }

}
