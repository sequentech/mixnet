/*
 *
 * Copyright 2011 Douglas Wikström
 *
 * This file is part of a package for Verificatum that provides native
 * elliptic curve code (ECN).
 *
 * ECN is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * ECN is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with ECN.  If not, see <http://www.gnu.org/licenses/>.
 */

package jecn;

import java.math.*;
import java.util.*;

/**
 *
 * @author Douglas Wikstrom
 */
public class ECN {

    /**
     * Load native code upon loading this class.
     */
    static {
	System.loadLibrary("jecn");
    }

    /**
     * Computes a modular exponentiation.
     *
     * @param exponent Exponent used to compute power.
     * @param modulus Modulus.
     * @return This instance to the power of <code>exponent</code>
     * modulo <code>modulus</code>.
     */
    public native static byte[][] exp(byte[] modulus,
                                      byte[] a,
                                      byte[] b,
                                      byte[] x,
                                      byte[] y,
                                      byte[] exponent);

    public native static byte[][] sexp(byte[] modulus,
                                       byte[] a,
                                       byte[] b,
                                       byte[][] basesx,
                                       byte[][] basesy,
                                       byte[][] exponents);

    public native static long fexp_precompute(byte[] modulus,
                                              byte[] a,
                                              byte[] b,
                                              byte[] basisx,
                                              byte[] basisy,
                                              int bitLength,
                                              int size);

    public native static byte[][] fexp(long tablePtr, byte[] exponent);

    public native static void fexp_clear(long tablePtr);

    public static void main(String[] args) {

        BigInteger modulus = new BigInteger("1");
        BigInteger a = new BigInteger("2");
        BigInteger b = new BigInteger("3");

        BigInteger x = new BigInteger("4");
        BigInteger y = new BigInteger("5");
        BigInteger exponent = new BigInteger("6");

        byte[][] res = exp(modulus.toByteArray(),
                           a.toByteArray(),
                           b.toByteArray(),
                           x.toByteArray(),
                           y.toByteArray(),
                           exponent.toByteArray());

        BigInteger rx = new BigInteger(res[0]);
        BigInteger ry = new BigInteger(res[1]);

        System.out.println("rx = " + rx);
        System.out.println("ry = " + ry);

    }
}
