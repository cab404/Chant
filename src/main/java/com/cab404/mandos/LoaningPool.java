package com.cab404.mandos;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Recycling semi-concurrent semi-nonblocking semi-sometimes-pool <br/>
 * Created at 11:57 on 01/08/15
 *
 * @author cab404
 */
public abstract class LoaningPool<A> {

    /**
     * How many objects do we preserve.
     */
    private final int reserved;

    private static boolean ANDROID = Package.getPackage("android.os") != null;

    public interface Borrow<A> extends AutoCloseable, Iterable<A> {
        A get(int index);

        int length();

        void expand(int by);

        void close();

        Borrow cutFirst(int howMany);
    }

    public static class SingularBorrow<A> implements Borrow<A> {
        private final LoaningPool<A> data;
        public final LinkedList<A> entries;

        private SingularBorrow(A value, LoaningPool<A> data) {
            this.entries = new LinkedList<>();
            entries.add(value);
            this.data = data;
        }

        @Override
        public A get(int index) {
            return entries.get(index);
        }

        @Override
        public int length() {
            return entries.size();
        }

        @Override
        public void expand(int by) {
            for (int i = 0; i < by; i++)
                entries.add(data.genObject());
        }

        @Override
        public Borrow cutFirst(int howMany) {
            for (int i = 0; i < howMany; i++)
                data.dispose(entries.remove());
            return this;
        }

        @Override
        public void close() {
            entries.clear();
        }

        @Override
        public Iterator<A> iterator() {
            return entries.iterator();
        }
    }

    public static class RecursiveBorrow<A> implements Borrow<A> {
        RecursiveBorrow<A> next;
        LoaningPool<A> data;
        int length = 0;
        A value;

        private RecursiveBorrow(A value, LoaningPool<A> data) {
            this.value = value;
            this.data = data;
        }

        @Override
        public int length() {
            return length;
        }

        public A get() {
            return value;
        }

        @Override
        public A get(int index) {
            if (index == 0)
                return value;
            if (next == null)
                return null;
            else
                return next.get(index - 1);
        }


        private RecursiveBorrow getBorrow(int index) {
            if (index == 0)
                return this;
            if (next == null)
                return null;
            else
                return next.getBorrow(index - 1);
        }

        @Override
        public synchronized void expand(int by) {
            this.length += by;
            if (next == null)
                //noinspection unchecked
                next = data.data.isEmpty()
                        ? new RecursiveBorrow<>(data.genObject(), data)
                        : (RecursiveBorrow<A>) data.data.poll();
            else
                next.expand(by);
        }

        /**
         * Removes first {@code howMany} elements from borrow, and returns new head of borrow
         */
        @Override
        public Borrow cutFirst(int howMany) {
            if (howMany == 0) return this;
            RecursiveBorrow newHead = getBorrow(howMany);
            if (newHead == null) throw new IndexOutOfBoundsException();
            RecursiveBorrow oldTail = getBorrow(howMany - 1);
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
            data.offer(this);

            if (next != null)
                next.free();

            next = null;
            length = 0;
        }

        @Override
        public Iterator<A> iterator() {
            return new Iterator<A>() {
                RecursiveBorrow<A> now = RecursiveBorrow.this;

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

    private final Queue<Borrow<A>> data;

    public LoaningPool(int reservedCount) {
        this.reserved = reservedCount;
        if (ANDROID)
            data = new ConcurrentLinkedQueue<>();
        else
            data = null;
    }

    /**
     * Empties preserved objects.
     */
    public void cleanPreserved() {
        if (data != null)
            data.clear();
    }

    /**
     * Borrows {@code howMuch} objects.
     */
    public Borrow borrow(int howMuch) {
        Borrow poll;

        // Borrowing first element
        if (ANDROID)
            if (data.isEmpty())
                poll = new RecursiveBorrow<>(genObject(), this);
            else
                poll = data.poll();
        else
            poll = new SingularBorrow<>(genObject(), this);

        poll.expand(howMuch - 1);

        return poll;
    }

    /**
     * Generates new object
     */
    abstract A genObject();

    protected void offer(Borrow<A> borrow) {
        if (ANDROID)
            if (data.size() >= reserved)
                dispose(borrow.get(0));
            else {
                clear(borrow.get(0));
                data.add(borrow);
            }
    }

    /**
     * Prepare your object to be completely obliterated.
     */
    abstract void dispose(A object);

    /**
     * Prepare your object for a new user.
     */
    abstract void clear(A object);

}
