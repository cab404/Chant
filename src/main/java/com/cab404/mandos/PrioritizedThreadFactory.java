package com.cab404.mandos;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory with changeable thread priority. <br/>
 * Created at 23:53 on 16/08/15
 *
 * @author cab404
 */
public class PrioritizedThreadFactory implements ThreadFactory {

    private final int priority;

    public PrioritizedThreadFactory(int priority) {
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setPriority(priority);
        return t;
    }
}
