
/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 *
 * This file is part of Verificatum.
 *
 * Verificatum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Verificatum is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Verificatum.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package verificatum.arithm;

import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;

import verificatum.test.*;

/**
 * Tests some of the functionality of {@link LargeInteger}.
 *
 * @author Douglas Wikstrom
 */
public class TestLargeInteger {

    public static boolean convertToByteArray(TestParameters tp)
        throws Exception {
        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        for (int i = 0; i < tp.testSize; i++) {
            LargeInteger n = new LargeInteger(100, rs);

            byte[] bytes = n.toByteArray();

            LargeInteger m = new LargeInteger(bytes);
            LargeInteger k = new LargeInteger(bytes, 0, bytes.length);
            if (!m.equals(n) || !k.equals(n)) {
                return false;
            }
        }
        return true;
    }

    public static boolean convertToByteTree(TestParameters tp)
        throws Exception {
        RandomSource rs =
            new PRGHeuristic(tp.prgseed.getBytes());

        // Test ByteTree
        for (int i = 0; i < 20 * tp.testSize; i++) {
            LargeInteger n = new LargeInteger(100, rs);

            ByteTreeBasic bt = n.toByteTree();
            ByteTreeReader btr = bt.getByteTreeReader();

            LargeInteger m = new LargeInteger((n.bitLength()+15) / 8, btr);

            if (!n.equals(m)) {
                return false;
            }
        }
        return true;
    }

    public static boolean convertArrayByteTree(TestParameters tp)
        throws Exception {
        RandomSource rs =
            new PRGHeuristic(tp.prgseed.getBytes());

        // Test ByteTree array
        for (int i = 0; i < tp.testSize; i++) {
            LargeInteger[] na = LargeInteger.random(10, 100, rs);


            LargeInteger ub = LargeInteger.TWO.shiftLeft(100);

            ByteTreeBasic bt =
                LargeInteger.toByteTree(ub.toByteArray().length, na);
            ByteTreeReader btr = bt.getByteTreeReader();

            LargeInteger[] ma =
                LargeInteger.toLargeIntegers(na.length,
                                             btr,
                                             LargeInteger.ZERO,
                                             ub);
            for (int j = 0; j < na.length; j++) {
                if (!na[j].equals(ma[j])) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isProbablePrimeSmall(TestParameters tp)
        throws Exception {

    	int certainty = 80;
    	RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        for(int i = 3; i < 10000; i++){

            LargeInteger sp = new LargeInteger(i);
            if (sp.isProbablePrime(rs) != sp.value.isProbablePrime(certainty)) {
                return false;
            }
        }
        return true;
    }

    public static boolean randomPrimeExact(TestParameters tp)
        throws Exception {

    	int certainty = 80;
    	RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        for(int i = 3; i < 1000; i++){

            LargeInteger e = LargeInteger.randomPrimeExact(100,
                                                           rs,
                                                           certainty);

            if (!e.isProbablePrime(rs)) {
                return false;
            }
        }
        return true;
    }

    public static boolean modPow(TestParameters tp) throws Exception {

        RandomSource rs =
            new PRGHeuristic(tp.prgseed.getBytes());

        // Choose random elements
        LargeInteger basis = new LargeInteger(1024, rs);
        LargeInteger exponent = new LargeInteger(1024, rs);
        LargeInteger modulus = new LargeInteger(1024, rs);

        for (int i = 0; i < tp.testSize; i++) {

            LargeInteger result = basis.modPow(exponent, modulus);

            BigInteger correct =
                basis.value.modPow(exponent.value, modulus.value);

            basis = new LargeInteger(correct);

            if (!result.value.equals(correct)) {
                return false;
            }
        }
        return true;
    }

    public static boolean modPowProd(TestParameters tp) throws Exception {

        RandomSource rs =
            new PRGHeuristic(tp.prgseed.getBytes());

        for (int size = 1; size < 100; size += 13) {

            // Choose random elements
            LargeInteger[] bases = LargeInteger.random(size, 1124, rs);
            LargeInteger[] exponents = LargeInteger.random(size, 1124, rs);
            LargeInteger modulus = SafePrimeTable.safePrime(1024);

            for (int i = 0; i < size; i++) {
                bases[i] = bases[i].mod(modulus);
            }


            LargeInteger res1 =
                LargeInteger.naiveModPowProd(bases, exponents, modulus);

            LargeInteger res2 =
                LargeInteger.modPowProd(bases, exponents, modulus);

            if (!res1.equals(res2)) {
                return false;
            }
        }
        return true;
    }

    public static boolean legendre(TestParameters tp) throws Exception {

        RandomSource rs =
            new PRGHeuristic(tp.prgseed.getBytes());

        int REPETITIONS = 1000;
        int size = 100;

        for (int i = 0; i < REPETITIONS; i++) {
            LargeInteger n = new LargeInteger(size, rs);

            int intn = n.value.intValue() & 0xFF;

            if (intn % 4 == 3) {

                for (int j = 0; j < 10; j++) {

                    LargeInteger a = new LargeInteger(size, rs);
                    a = a.mul(a).mod(n);
                    if (a.legendre(n) < 0) {
                        return false;
                    }
                }
                for (int j = 0; j < 10; j++) {

                    LargeInteger a = new LargeInteger(size, rs);
                    a = a.mul(a).neg().mod(n);
                    if (a.legendre(n) > 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
