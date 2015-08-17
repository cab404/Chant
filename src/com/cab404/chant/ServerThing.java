package com.cab404.chant;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Sorry for no comments!
 * Created at 17:54 on 04/07/15
 *
 * @author cab404
 */
public class ServerThing {


    public static void main(String[] args) throws IOException {
        LoaningPool<BigInteger> bblp = new LoaningPool<BigInteger>(10) {
            @Override
            BigInteger genObject() {
                System.out.println("creating new one");
                return new BigInteger("0");
            }

            @Override
            void dispose(BigInteger object) {
                System.out.println("dispose of that");
            }

            @Override
            void clear(BigInteger object) {
                System.out.println("cleaning");
            }
        };


        LoaningPool<BigInteger>.Borrow borrow = bblp.borrow(2);
        borrow.expand(3);
        borrow.expand(2);
        borrow.expand(5);
        borrow.free();

        while (borrow != null) {
            System.out.println(borrow.length);
            borrow = borrow.next;
        }


    }
}
