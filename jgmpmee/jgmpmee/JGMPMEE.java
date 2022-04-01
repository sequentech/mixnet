/*

Copyright 2008 2009 Douglas Wikstrom

This file is part of Java GMP Modular Exponentiation Extension
(JGMPMEE).

JGMPMEE is free software: you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JGMPMEE is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
License for more details.

You should have received a copy of the GNU Lesser General Public
License along with JGMPMEE.  If not, see
<http://www.gnu.org/licenses/>.

*/

package jgmpmee;

import java.math.*;
import java.util.*;

/**
 * Allows invoking the modular exponentiation, simultaneous modular
 * exponentiation routines, primality tests, and related routines of
 * the <a href="http://gmplib.org">Gnu Multiprecision Library
 * (GMP)</a>and <a href="http://www.mixnet.org">GMPMEE</a> (a
 * minor extension of GMP).
 *
 * <p>
 *
 * @author Douglas Wikstrom
 */
public class JGMPMEE {

    /**
     * Converts an array of <code>BigInteger</code> to an array of
     * <code>byte[]</code> representing the integers in two's
     * complement representation.
     *
     * @param bis Integers to be converted.
     * @return Array of converted integers.
     */
    static byte[][] convert(BigInteger[] bis) {

	byte[][] native_bis = new byte[bis.length][];
	for (int i = 0; i < native_bis.length; i++) {
	    native_bis[i] = bis[i].toByteArray();
	}
	return native_bis;
    }

    /**
     * Computes a modular exponentiation.
     *
     * @param exponent Exponent used to compute power.
     * @param modulus Modulus.
     * @return This instance to the power of <code>exponent</code>
     * modulo <code>modulus</code>.
     */
    public static BigInteger powm(BigInteger basis,
				  BigInteger exponent,
				  BigInteger modulus)
	throws ArithmeticException {
	return new BigInteger(GMPMEE.powm(basis.toByteArray(),
					  exponent.toByteArray(),
					  modulus.toByteArray()));
    }

    /**
     * Computes a simultaneous modular exponentiation.
     *
     * @param bases Basis element.
     * @param exponents Exponent used to compute power.
     * @param modulus Modulus.
     * @return Product of the bases to the powers of
     * <code>exponents</code> modulo <code>modulus</code>.
     */
    public static BigInteger spowm(BigInteger[] bases,
				   BigInteger[] exponents,
				   BigInteger modulus) {
	byte[][] native_bases = convert(bases);
	byte[][] native_exponents = convert(exponents);
	return new BigInteger(GMPMEE.spowm(native_bases,
					   native_exponents,
					   modulus.toByteArray()));
    }

    /**
     * Returns the Legendre symbol of this instance modulo the
     * input.
     *
     * @param odd_prime An odd prime modulus.
     * @param value Integer to be tested.
     * @return Legendre symbol of <code>value</code> modulo
     * <code>odd_prime</code>.
     */
    public static int legendre(BigInteger value, BigInteger odd_prime) {
	return GMPMEE.legendre(value.toByteArray(), odd_prime.toByteArray());
    }
}

