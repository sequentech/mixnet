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
 * (GMP)</a>and GMPMEE (a minor extension of GMP).
 *
 * <p>
 *
 * @author Douglas Wikstrom
 */
public final class GMPMEE {

    /**
     * Load native code upon loading this class.
     */
    static {
	System.loadLibrary("jgmpmee");
    }

    /**
     * Computes a modular exponentiation.
     *
     * @param exponent Exponent used to compute power.
     * @param modulus Modulus.
     * @return This instance to the power of <code>exponent</code>
     * modulo <code>modulus</code>.
     */
    native static byte[] powm(byte[] basis,
			      byte[] exponent,
			      byte[] modulus);

    /**
     * Computes a simultaneous modular exponentiation.
     *
     * @param bases Basis element.
     * @param exponents Exponent used to compute power.
     * @param modulus Modulus.
     * @return Product of the bases to the powers of
     * <code>exponents</code> modulo <code>modulus</code>.
     */
    native static byte[] spowm(byte[][] bases,
			       byte[][] exponents,
			       byte[] modulus);

    /**
     * Performs precomputation for the given basis and modulus
     * assuming the given exponent bit length.
     *
     * @param basis Basis element.
     * @param modulus Modulus used during modular exponentiation.
     * @param exponentBitlen Expected bit length of exponents.
     * @param blockWidth Decides how many distinct generators are used
     * when translating an exponentiation into a simultaneous
     * exponentiation.
     * @return Native pointer to a precomputed table.
     */
    native static long fpowm_precomp(byte[] basis,
				     byte[] modulus,
				     int blockWidth,
				     int exponentBitlen);

    /**
     * Performs precomputation for the given basis and modulus
     * assuming the given exponent bit length.
     *
     * @param tablePtr Native pointer to a precomputed table output by
     * {@link #fpowm_precomp(byte[], byte[], int, int)}.
     * @param exponent Exponent given in two's complement.
     * @return Result of modular exponentiation.
     */
    native static byte[] fpowm(long tablePtr, byte[] exponent);

    /**
     * Frees the resources allocated by the native object pointed to
     * by the input.
     *
     * @param tablePtr Native pointer to a precomputed table output by
     * {@link #fpowm_precomp(byte[], byte[], int, int)}.
     */
    native static void fpowm_clear(long tablePtr);

    /**
     * Returns the Legendre symbol of <code>op</code> modulo
     * <code>odd_prime</code>.
     *
     * @param op An integer.
     * @param odd_prime An odd prime modulus.
     * @return Legendre symbol of <code>op</code> modulo
     * <code>odd_prime</code>.
     */
    native static int legendre(byte[] op, byte[] odd_prime);

    /**
     * Allocate and initialize Miller-Rabin state using the given
     * integer.
     *
     * @param n Integer to test.
     * @param search Decides if we are searching for an integer or testing.
     */
    native static long millerrabin_init(byte[] n, boolean search);

    /**
     * Increase the tested number to the next candidate integer.
     *
     * @param statePtr Native pointer to state for testing.
     */
    native static void millerrabin_next_cand(long statePtr);

    /**
     * Executes one round of the Miller-Rabin test and returns 0 or 1
     * depending on if the tested integer is deemed to be composite or
     * not.
     *
     * @param statePtr Native pointer to state for testing.
     * @param base Base element used for testing. This must be
     * non-zero and non-one modulo the tested integer.
     * @return Result of the test as a 0/1 integer.
     */
    native static int millerrabin_once(long statePtr, byte[] base);

    /**
     * Free memory resources allocated for testing.
     *
     * @param statePtr Native pointer to state for testing.
     */
    native static void millerrabin_clear(long statePtr);

    /**
     * Returns the current candidate integer.
     *
     * @param statePtr Native pointer to state for testing.
     */
    native static byte[] millerrabin_current(long statePtr);

    /**
     * Allocate and initialize Miller-Rabin state using the given
     * integer.
     *
     * @param n Integer to test.
     * @param search Decides if we are searching for an integer or testing.
     * @return Native pointer to state for testing.
     */
    native static long millerrabin_safe_init(byte[] n, boolean search);

    /**
     * Increase the tested number to the next candidate integer.
     *
     * @param statePtr Native pointer to state for testing.
     */
    native static void millerrabin_safe_next_cand(long statePtr);

    /**
     * Executes one round of the Miller-Rabin test and returns 0 or 1
     * depending on if the tested integer is deemed to not be a safe
     * prime, or a safe prime.
     *
     * @param statePtr Native pointer to state for testing.
     * @param base Base element used for testing.
     * @param index Must be zero for testing the integer and one for
     * testing m, where n=2m+1.
     * @return Result of test.
     */
    native static int millerrabin_safe_once(long statePtr,
					    byte[] base,
					    int index);

    /**
     * Free memory resources allocated for testing.
     *
     * @param statePtr Native pointer to state for testing.
     */
    native static void millerrabin_safe_clear(long statePtr);

    /**
     * Returns the current safe-prime candidate.
     *
     * @return Safe-prime candidate.
     */
    native static byte[] millerrabin_current_safe(long statePtr);
}
