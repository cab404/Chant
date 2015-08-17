package com.cab404.chant;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Recycling semi-concurrent semi-nonblocking pool <br/>
 * Created at 11:57 on 01/08/15
 *
 * @author cab404
 */
public abstract class LoaningPool<A> {

    /**
     * How many objects do we preserve.
     */
    private final int reserved;

    public class Borrow implements AutoCloseable, Iterable<A> {
        A value;
        Borrow next;
        int length = 0;

        private Borrow(A value) {
            this.value = value;
        }

        public A get() {
            return value;
        }

        public int length() {
            return length;
        }

        public A get(int index) {
            if (index == 0)
                return value;
            if (next == null)
                return null;
            else
                return next.get(index - 1);
        }


        private Borrow getBorrow(int index) {
            if (index == 0)
                return this;
            if (next == null)
                return null;
            else
                return next.getBorrow(index - 1);
        }

        public synchronized void expand(int by) {
            this.length += by;
            if (next == null)
                next = borrow(by);
            else
                next.expand(by);
        }

        /**
         * Removes first {@code howMany} elements from borrow, and returns new head of borrow
         */
        public Borrow cutFirst(int howMany) {
            if (howMany == 0) return this;
            Borrow newHead = getBorrow(howMany);
            if (newHead == null) throw new IndexOutOfBoundsException();
            Borrow oldTail = getBorrow(howMany - 1);
            if (oldTail == null) throw new IndexOutOfBoundsException();

            oldTail.next = null;
            free();
            return newHead;
        }

        @Override
        public void close() {
            free();
        }

        /**
         * Same as close - frees resources
         */
        public void free() {
            if (data.size() >= reserved)
                dispose(value);
            else {
                clear(value);
                data.add(this);
            }
            if (next != null)
                next.free();

            next = null;
            length = 0;
        }

        @Override
        public Iterator<A> iterator() {
            return new Iterator<A>() {
                Borrow now = Borrow.this;

                @Override
                public boolean hasNext() {
                    return now != null;
                }

                @Override
                public A next() {
                    try {
                        return now.value;
                    } finally {
                        now = now.next;
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        }
    }

    private final Queue<Borrow> data;

    public LoaningPool(int reservedCount) {
        this.reserved = reservedCount;
        data = new ConcurrentLinkedQueue<>();
    }

    /**
     * Empties preserved objects.
     */
    public void cleanPreserved() {
        data.clear();
    }

    /**
     * Borrows {@code howMuch} objects.
     */
    public Borrow borrow(int howMuch) {
        if (data.isEmpty()) data.add(new Borrow(genObject()));
        Borrow poll = data.poll();
        poll.length = howMuch;
        if (howMuch > 1) poll.next = borrow(howMuch - 1);
        return poll;
    }

    /**
     * Generates new object
     */
    abstract A genObject();

    /**
     * Prepare your object to be completely obliterated.
     */
    abstract void dispose(A object);

    /**
     * Prepare your object for a new user.
     */
    abstract void clear(A object);

}
