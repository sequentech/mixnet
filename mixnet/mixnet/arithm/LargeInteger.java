




/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 *
 * This file is part of Vfork.
 *
 * Vfork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Vfork is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Vfork.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package mixnet.arithm;

import java.math.*;
import java.util.*;

// Enabled calls to native code begins here.

import jgmpmee.*;
import jgmpmee.MillerRabin; // This is essential to pick the right
                            // version of Miller-Rabin. DO NOT TOUCH!
// Enabled calls to native code ends here

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.util.*;


/**
 * Implements arbitrarily large immutable integers with threading of
 * computationally expensive operations to make use of multiple
 * cores. In pure Java mode, this is a wrapper of {@link
 * java.math.BigInteger}.
 *
 * <p>
 *
 * This file is derived from a source file
 * <code>LargeInteger.magic</code> either to use pure Java code or to
 * map some routines to native code. Consult the source code for more
 * information.
 *
 * @author Douglas Wikstrom
 */
// This files contains two types of preprocessor tags to choose to use
// pure Java code or optimized C code based on the GMP library. We use
// the following tags to indicate the start and end of each type of
// code.
//
// PURE_JAVA_BEGIN
// PURE_JAVA_END
// Enabled calls to native code begins here.
// Enabled calls to native code ends here
//
public class LargeInteger implements Comparable<LargeInteger>,
                                     ByteTreeConvertible {

    /**
     * Determines the default probability that a composite is accepted
     * as a prime by {@link #isProbablePrime(RandomSource,int)}. The
     * probability is bounded by 2<sup>-<code>CERTAINTY</code></sup>.
     */
    public final static int CERTAINTY = 100;

    /**
     * An instance representing the integer zero.
     */
    public static final LargeInteger ZERO = new LargeInteger(BigInteger.ZERO);

    /**
     * An instance representing the integer one.
     */
    public static final LargeInteger ONE = new LargeInteger(BigInteger.ONE);

    /**
     * An instance representing the integer two.
     */
    public static final LargeInteger TWO =
        new LargeInteger(BigInteger.ONE.add(BigInteger.ONE));

    /**
     * Value of this instance.
     */
    protected BigInteger value;

    /**
     * Creates an instance corresponding to the input.
     *
     * @param value Representation of this integer.
     */
    public LargeInteger(BigInteger value) {
        this.value = value;
    }

    /**
     * Creates an instance corresponding to the input.
     *
     * @param value Representation of this integer.
     */
    public LargeInteger(int value) {
        this.value = new BigInteger(Integer.toString(value, 16), 16);
    }

    /**
     * Interprets the input as an integer in two's complement.
     *
     * @param val Representation of this integer.
     * @param startIndex Index of first byte to read.
     * @param length Number of bytes to read.
     *
     * @throws ArithmError If the input array has zero length.
     */
    public LargeInteger(byte[] val, int startIndex, int length) {

        byte[] theVal =
            Arrays.copyOfRange(val, startIndex, startIndex + length);
        try {
            value = new BigInteger(theVal);
        } catch (NumberFormatException nfe) {
            throw new ArithmError("Array of zero length!", nfe);
        }
    }

    /**
     * Interprets the input as an integer in two's complement.
     *
     * @param val Representation of this integer.
     *
     * @throws ArithmError If the input array has zero length.
     */
    public LargeInteger(byte[] val) {
        try {
            value = new BigInteger(val);
        } catch (NumberFormatException nfe) {
            throw new ArithmError("Array of zero length!", nfe);
        }
    }

    /**
     * Creates an instance corresponding to the input subject to the
     * given bound.
     *
     * @param maxByteLength Maximal number of bytes read.
     * @param btr Representation of this integer.
     *
     * @throws ArithmFormatException If the input does not represent
     * an integer or is too large.
     */
    public LargeInteger(int maxByteLength, ByteTreeReader btr)
        throws ArithmFormatException {
        try {
            int len = btr.getRemaining();
            if (maxByteLength < len) {
                throw new ArithmFormatException("Too small max length!");
            } else {
                value = new BigInteger(btr.read(len));
            }
        } catch (EIOException eioe) {
            throw new ArithmFormatException("No value!", eioe);
        } catch (NumberFormatException nfe) {
            throw new ArithmFormatException("Malformed integer!", nfe);
        }
    }

    /**
     * Creates an instance corresponding to the input subject to the
     * given bound.
     *
     * @param expectedByteLength Expected number of bytes in the array
     * embedded into the byte tree input.
     * @param btr Representation of this integer.
     * @param obj Dummy parameter used to overload {@link
     * LargeInteger(int,ByteTreeReader)}.
     *
     * @throws ArithmFormatException If the input does not represent
     * an integer or has the wrong size.
     */
    public LargeInteger(int expectedByteLength, ByteTreeReader btr,
                        Object obj)
        throws ArithmFormatException {
        try {
            if (btr.getRemaining() == expectedByteLength) {
                value = new BigInteger(btr.read());
            } else {
                throw new ArithmFormatException("Too small max length!");
            }
        } catch (EIOException eioe) {
            throw new ArithmFormatException("No value!", eioe);
        } catch (NumberFormatException nfe) {
            throw new ArithmFormatException("Malformed integer!", nfe);
        }
    }

    /**
     * Creates an instance corresponding to the input.
     *
     * @param btr Representation of this integer.
     *
     * @throws ArithmFormatException If the input does not represent
     * an integer.
     */
    public LargeInteger(ByteTreeReader btr)
        throws ArithmFormatException {
        try {
            value = new BigInteger(btr.read());
        } catch (EIOException eioe) {
            throw new ArithmFormatException("No value!", eioe);
        } catch (NumberFormatException nfe) {
            throw new ArithmFormatException("Malformed integer!", nfe);
        }
    }

    /**
     * Creates an instance corresponding to the input. If the input is
     * incorrectly formatted, the result is an instance representing
     * zero, i.e., no exception is thrown.
     *
     * @param expectedByteLength Number of bytes in the array embedded
     * into the byte tree input.
     * @param btr Representation of this integer.
     * @return Integer represented by the input.
     */
    public static LargeInteger safeLargeInteger(int expectedByteLength,
                                                ByteTreeReader btr) {
        try {
            return new LargeInteger(expectedByteLength, btr);
        } catch (ArithmFormatException nfe) {
            return LargeInteger.ZERO;
        }
    }

    /**
     * Creates an instance corresponding to the input. If the input is
     * incorrectly formatted, then an error is thrown. This method
     * should only be used if the input is known to be correct.
     *
     * @param expectedByteLength Maximal number of bytes read.
     * @param btr Representation of this integer.
     * @return Integer represented by the input.
     *
     * @throws ArithmError If the input does not represent an instance.
     */
    public static LargeInteger unsafeLargeInteger(int expectedByteLength,
                                                  ByteTreeReader btr) {
        try {
            return new LargeInteger(expectedByteLength, btr);
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Fatal error!", afe);
        }
    }

    /**
     * Returns an instance corresponding to the input interpreted as
     * an integer in two's complement with an additional leading zero,
     * i.e., the input is interpreted as a positive integer.
     *
     * @param data Representation of positive integer.
     * @return Integer represented by the input.
     */
   public static LargeInteger toPositive(byte[] data) {
        byte[] posData = new byte[data.length + 1];
        posData[0] = 0;
        System.arraycopy(data, 0, posData, 1, data.length);
        return new LargeInteger(posData);
    }

    /**
     * Returns an instance corresponding to the input interpreted as
     * an integers in two's complement with an additional leading
     * zero, i.e., the input is interpreted as a positive integer.
     *
     * @param data Representation of positive integer.
     * @param offset Offset in input.
     * @param len Number of bytes to read.
     * @return Integer represented by the input.
     */
    public static LargeInteger toPositive(byte[] data, int offset, int len) {
        byte[] posData = new byte[len + 1];
        posData[0] = 0;
        System.arraycopy(data, 0, posData, 1, len);
        return new LargeInteger(posData);
    }

    /**
     * Creates an instance representing a non-negative random
     * integer. Note that if the most significant bits happen to be
     * zero, then the integer will effectively have fewer bits.
     *
     * @param bitLength Potential number of bits in constructed integer.
     * @param randomSource Source of random bits.
     *
     * @throws ArithmError If the bit-length is non-positive.
     */
    public LargeInteger(int bitLength, RandomSource randomSource) {
        if (bitLength <= 0) {
            throw new ArithmError("Non-positive bit-length!");
        }
        try {
            int len = (bitLength + 7) / 8;
            byte[] bits = new byte[len + 1];

            bits[0] = 0;
            randomSource.getBytes(bits, 1, len);
            if (bitLength % 8 != 0) {
                bits[1] &= (0xFF >>> (8 - (bitLength % 8)));
            }
            value = new BigInteger(bits);
        } catch (IllegalArgumentException iae) {
            throw new ArithmError("Non-positive bit length!", iae);
        }
    }

    /**
     * Creates an instance representing a non-negative random integer
     * between zero (inclusive) and the given modulus (exclusive).
     *
     * @param modulus Modulus.
     * @param statDist Statistical distance.
     * @param randomSource Source of random bits.
     *
     * @throws ArithmError If the bit-length is non-positive.
     */
    public LargeInteger(LargeInteger modulus, int statDist,
                        RandomSource randomSource) {
        int bitLength = modulus.bitLength() + statDist;
        LargeInteger tmp = new LargeInteger(bitLength, randomSource);
        value = tmp.mod(modulus).value;
    }

    /**
     * Creates an instance corresponding to the input.
     *
     * @param str A decimal representation of this integer.
     */
    public LargeInteger(String str) {
        try {
            value = new BigInteger(str);
        } catch (NumberFormatException nfe) {
            throw new ArithmError("Not an integer!", nfe);
        }
    }

    /**
     * Creates an instance corresponding to the input.
     *
     * @param str Representation of this integer in the given radix.
     * @param radix Basis in which the integer is given.
     */
    public LargeInteger(String str, int radix) {
        try {
            value = new BigInteger(str, radix);
        } catch (NumberFormatException nfe) {
            throw new ArithmError("Not an integer!", nfe);
        }
    }

    /**
     * Adds this instance to the input.
     *
     * @param li Integer added to this instance.
     * @return Sum of this instance and the input.
     */
    public LargeInteger add(LargeInteger li) {
        return new LargeInteger(value.add(li.value));
    }

    /**
     * Returns the negative of this instance.
     *
     * @return Negative of this instance.
     */
    public LargeInteger neg() {
        return new LargeInteger(value.negate());
    }

    /**
     * Computes the element-wise modular negative of the input array.
     *
     * @param integers Integers.
     * @param modulus Modulus relative to which we take negatives.
     * @return Modular negatives.
     */
    public static LargeInteger[] modNeg(LargeInteger[] integers,
					LargeInteger modulus) {
	LargeInteger[] res = new LargeInteger[integers.length];
	for (int i = 0; i < res.length; i++) {
	    res[i] = modulus.sub(integers[i]);
	}
	return res;
    }

    /**
     * Subtracts the input from this instance.
     *
     * @param li Integer subtracted from this instance.
     * @return Difference between this instance and the input.
     */
    public LargeInteger sub(LargeInteger li) {
        return new LargeInteger(value.subtract(li.value));
    }

    /**
     * Multiplies the input with this instance.
     *
     * @param li Integer multiplied with this instance.
     * @return Product of this instance and the input.
     */
    public LargeInteger mul(LargeInteger li) {
        return new LargeInteger(value.multiply(li.value));
    }

    /**
     * Divides this instance by the input.
     *
     * @param li Integer used to divide this instance.
     * @return Quotient of this instance and the input.
     *
     * @throws ArithmException If the input is zero.
     */
    public LargeInteger divide(LargeInteger li)
    throws ArithmException {
        try {
            return new LargeInteger(value.divide(li.value));
        } catch (ArithmeticException ae) {
            throw new ArithmException("Division by zero!", ae);
        }
    }

    /**
     * Shifts this instance to the left.
     *
     * @param pos Number of positions to shift.
     * @return This integer shifted to the left.
     */
    public LargeInteger shiftLeft(int pos) {
        return new LargeInteger(value.shiftLeft(pos));
    }

    /**
     * Shifts this instance to the right, keeping the sign.
     *
     * @param pos Number of positions to shift.
     * @return This integer shifted to the right.
     */
    public LargeInteger shiftRight(int pos) {
        return new LargeInteger(value.shiftRight(pos));
    }

    /**
     * Outputs -1, 0, or 1 depending on if this instance is less than,
     * equal to, or larger than the input, respectively.
     *
     * @param li Integer compared to this instance.
     * @return -1, 0, or 1 depending on if this instance is
     * smaller, equal, or greater than the input.
     */
    public int compareTo(LargeInteger li) {
        return value.compareTo(li.value);
    }

    /**
     * Performs a comparison of the two inputs.
     *
     * @param integers1 Left side integers.
     * @param integers2 Right side integers.
     * @return Result of comparison.
     */
    public static int compareTo(LargeInteger[] integers1,
				LargeInteger[] integers2) {
	if (integers1.length == integers2.length) {
            for (int i = 0; i < integers1.length; i++) {
                int cmp = integers1[i].compareTo(integers2[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        }
        throw new ArithmError("Illegal comparison!");
    }

    /**
     * Tests if the integer represented by this instance equals the
     * integer represented by the input.
     *
     * @param obj Instance to which this instance is compared.
     * @return <code>true</code> or <code>false</code> depending on if
     * this instance and the input represent the same integer or not.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof LargeInteger
            && compareTo((LargeInteger)obj) == 0;
    }

    /**
     * Computes the element-wise equality of the two inputs.
     *
     * @param integers1 Left side integers.
     * @param integers2 Right side integers.
     * @return Element-wise equality of the two inputs.
     */
    public static boolean[] equalsAll(LargeInteger[] integers1,
				      LargeInteger[] integers2) {
	if (integers1.length == integers2.length) {

	    boolean[] res = new boolean[integers1.length];
	    for (int i = 0; i < res.length; i++) {
		res[i] = integers1[i].equals(integers2[i]);
	    }
	    return res;
	}
	throw new ArithmError("Illegal comparison!");
    }

    /**
     * Returns true or false depending on if the addressed bit is set
     * or not.
     *
     * @param index Index of a bit.
     * @return Value of the designated bit interpreted as a boolean.
     */
    public boolean testBit(int index) {
        try {
            return value.testBit(index);
        } catch (ArithmeticException ae) {
            throw new ArithmError("Invalid index!", ae);
        }
    }

    /**
     * Sets a bit to one.
     *
     * @param index Index of a bit.
     * @return An instance identical to this instance except that the
     * designated bit is set to one.
     */
    public LargeInteger setBit(int index) {
        try {
            return new LargeInteger(value.setBit(index));
        } catch (ArithmeticException ae) {
            throw new ArithmError("Invalid index!", ae);
        }
    }

    /**
     * Sets a bit to zero.
     *
     * @param index Index of a bit.
     * @return An instance identical to this instance except that the
     * designated bit is zero.
     */
    public LargeInteger clearBit(int index) {
        try {
            return new LargeInteger(value.clearBit(index));
        } catch (ArithmeticException ae) {
            throw new ArithmError("Invalid index!", ae);
        }
    }

    /**
     * Computes this instance to the power of the exponent modulo the
     * modulus.
     *
     * @param exponent Exponent used to compute power.
     * @param modulus Modulus.
     * @return This instance to the power of <code>exponent</code>
     * modulo <code>modulus</code>.
     */
    public LargeInteger modPow(LargeInteger exponent, LargeInteger modulus) {
        if (modulus.compareTo(LargeInteger.ZERO) <= 0) {
            throw new ArithmError("Non-positive modulus!");
        } else {

// Removed pure java code here.
// Enabled calls to native code begins here.
            return new LargeInteger(JGMPMEE.powm(value,
                                                 exponent.value,
                                                 modulus.value));
// Enabled calls to native code ends here
        }
    }

    /**
     * Returns an array of the given size where all elements equal the
     * given value.
     *
     * @param size Size of array.
     * @param value Value to be used.
     * @return Array of integers.
     */
    public static LargeInteger[] fill(int size, LargeInteger value) {
        LargeInteger[] result = new LargeInteger[size];
        Arrays.fill(result, value);
        return result;
    }

    /**
     * Takes the modular power of each basis to its corresponding exponent.
     *
     * @param bases Array of bases.
     * @param exponents Array of exponents.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public static LargeInteger[] modPow(final LargeInteger[] bases,
                                        final LargeInteger[] exponents,
                                        final LargeInteger modulus) {
        final LargeInteger[] result = new LargeInteger[bases.length];

        ArrayWorker worker =
            new ArrayWorker(result.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {
                        result[i] = bases[i].modPow(exponents[i], modulus);
                    }
                }
            };
        worker.work();
        return result;
    }

    /**
     * Takes the modular power of each basis to the exponent.
     *
     * @param bases Array of bases.
     * @param exponent Exponent.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public static LargeInteger[] modPow(final LargeInteger[] bases,
                                        final LargeInteger exponent,
                                        final LargeInteger modulus) {
        final LargeInteger[] result = new LargeInteger[bases.length];

        ArrayWorker worker =
            new ArrayWorker(result.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {
                        result[i] = bases[i].modPow(exponent, modulus);
                    }
                }
            };
        worker.work();
        return result;
    }

    /**
     * Takes the modular power of this integer to the given exponents.
     *
     * @param exponents Exponents.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public LargeInteger[] naiveModPow(final LargeInteger[] exponents,
                                      final LargeInteger modulus) {
        final LargeInteger[] result = new LargeInteger[exponents.length];

        ArrayWorker worker =
            new ArrayWorker(exponents.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {
                        result[i] = modPow(exponents[i], modulus);
                    }
                }
            };
        worker.work();
        return result;
    }

    /**
     * Takes the modular power of this integer to the given exponents.
     *
     * @param exponents Exponents.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public LargeInteger[] modPow(final LargeInteger[] exponents,
                                 final LargeInteger modulus) {

        final LargeInteger[] result = new LargeInteger[exponents.length];

        // Extract integer exponents and determine maximal bitLength.
        int bitLength = 0;
        for (int i = 0; i < exponents.length; i++) {
            bitLength = Math.max(exponents[i].bitLength(), bitLength);
        }

        // Optimal width for the given bit length and number of
        // exponents.
        int width = LargeIntegerFixModPowTab.optimalWidth(bitLength,
                                                          exponents.length);

        final LargeIntegerFixModPowTab tab =
            new LargeIntegerFixModPowTab(this, bitLength, width, modulus);

        ArrayWorker worker =
            new ArrayWorker(result.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {

                        result[i] = tab.modPow(exponents[i]);
                    }
                }
            };
        worker.work();
        tab.free();

        return result;
    }

    /**
     * Returns the modular product of the elements in the input array.
     *
     * @param factors Array of integers.
     * @param modulus Modulus.
     * @return Modular product of integers of array.
     */
    public static LargeInteger modProd(LargeInteger[] factors,
                                       LargeInteger modulus) {
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < factors.length; i++) {
            result = result.multiply(factors[i].value).mod(modulus.value);
        }
        return new LargeInteger(result);
    }

    /**
     * Returns an array containing all partial modular products of
     * <code>agg</code> and the first to the <i>i</i>th element of the
     * array, for all <i>i</i>.
     *
     * @param agg Initial value of partial products.
     * @param factors Array of integers.
     * @param modulus Modulus.
     * @return Array of all partial modular products of the elements
     * in the array.
     */
    public static LargeInteger[] modProds(LargeInteger agg,
                                          LargeInteger[] factors,
                                          LargeInteger modulus) {
        LargeInteger[] res = new LargeInteger[factors.length];
        for (int i = 0; i < factors.length; i++) {
            res[i] = agg.mul(factors[i]).mod(modulus);
            agg = res[i];
        }
        return res;
    }

    /**
     * Returns the modular sum of the elements in the input array.
     *
     * @param terms Array of integers.
     * @param modulus Modulus.
     * @return Modular sum of integers of array.
     */
    public static LargeInteger modSum(LargeInteger[] terms,
                                      LargeInteger modulus) {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < terms.length; i++) {
            result = result.add(terms[i].value);
        }
        return new LargeInteger(result.mod(modulus.value));
    }

    /**
     * Computes the element-wise modular sum of the two inputs.
     *
     * @param integers1 First array of terms.
     * @param integers2 Second array of terms.
     * @param modulus Modulus.
     * @return Array of element-wise modular sums.
     */
    public static LargeInteger[] modAdd(LargeInteger[] integers1,
                                        LargeInteger[] integers2,
                                        LargeInteger modulus) {
        LargeInteger[] res = new LargeInteger[integers1.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = integers1[i].add(integers2[i]).mod(modulus);
        }
        return res;
    }

    /**
     * Returns the modular element-wise product of the integers in the
     * input arrays.
     *
     * @param integers Array of integers.
     * @param factors Array of integers.
     * @param modulus Modulus.
     * @return Modular element-wise product of the integers in the
     * input arrays.
     */
    public static LargeInteger[] modMul(final LargeInteger[] integers,
                                        final LargeInteger[] factors,
                                        final LargeInteger modulus) {
        final LargeInteger[] res = new LargeInteger[integers.length];

        ArrayWorker worker =
            new ArrayWorker(integers.length) {
                public void work(int start, int end) {

                    for (int i = start; i < end; i++) {
                        res[i] = integers[i].mul(factors[i]).mod(modulus);
                    }
                }
            };
        worker.work();

        return res;
    }

    /**
     * Computes the element-wise modular product of the first input
     * times the second scalar input.
     *
     * @param integers Array of integers.
     * @param scalar Scalar integer.
     * @param modulus Modulus.
     * @return Array of element-wise modular scaled products.
     */
    public static LargeInteger[] modMul(LargeInteger[] integers,
                                        LargeInteger scalar,
                                        LargeInteger modulus) {
        LargeInteger[] res = new LargeInteger[integers.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = integers[i].mul(scalar).mod(modulus);
        }
        return res;
    }

    /**
     * Computes the element-wise modular inverse.
     *
     * @param integers Array of integers to invert.
     * @param modulus Modulus.
     * @return Array of element-wise modular inverses.
     * @throws ArithmException If any integer is not invertible.
     */
    public static LargeInteger[] modInv(final LargeInteger[] integers,
					final LargeInteger modulus)
    throws ArithmException {

        final LargeInteger[] res = new LargeInteger[integers.length];

        final List<ArithmException> exceptions =
            Collections.synchronizedList(new LinkedList<ArithmException>());

        ArrayWorker worker =
            new ArrayWorker(integers.length) {
                public void work(int start, int end) {

                    try {
                        for (int i = start; i < end; i++) {
                            res[i] = integers[i].modInv(modulus);
                        }
                    } catch (ArithmException ae) {
                        exceptions.add(ae);
                    }
                }
            };
        worker.work();

        if (exceptions.size() > 0) {
            throw exceptions.get(0);
        }

	return res;
    }

    /**
     * Computes the modular inner product of the two input arrays.
     *
     * @param integers1 First array of integers.
     * @param integers2 Second array of integers.
     * @param modulus Modulus.
     * @return Modular inner product of the two inputs.
     */
    public static LargeInteger modInner(LargeInteger[] integers1,
					LargeInteger[] integers2,
					LargeInteger modulus) {
	LargeInteger res = LargeInteger.ZERO;
        for (int i = 0; i < integers1.length; i++) {
            res = res.add(integers1[i].mul(integers2[i]));
        }
        return res.mod(modulus);
    }

    /**
     * Each basis in the array of bases is taken to the modular power
     * of the corresponding exponent modulo the modulus and the
     * modular product of the resulting integers is returned.
     *
     * @param bases Array of basis integers.
     * @param exponents Array of exponents.
     * @param modulus Modulus.
     * @return Modular power product of the input arrays.
     */
    public static LargeInteger naiveModPowProd(final LargeInteger[] bases,
                                               final LargeInteger[] exponents,
                                               final LargeInteger modulus) {
        final List<LargeInteger> results =
            Collections.synchronizedList(new LinkedList<LargeInteger>());

        ArrayWorker worker =
            new ArrayWorker(bases.length) {
                public void work(int start, int end) {

                    LargeInteger res = LargeInteger.ONE;

                    for (int i = start; i < end; i++) {
                        LargeInteger factor =
                            bases[i].modPow(exponents[i], modulus);
                        res = res.mul(factor).mod(modulus);
                    }
                    results.add(res);
                }
            };
        worker.work();

        LargeInteger result = ONE;
        for (LargeInteger li : results) {
            result = result.mul(li).mod(modulus);
        }
        return result;
    }
    /**
     * Each basis in the array of bases is taken to the modular power
     * of the corresponding exponent modulo the modulus and the
     * modular product of the resulting integers is returned.
     *
     * @param bases Array of basis integers.
     * @param exponents Array of exponents.
     * @param modulus Modulus.
     * @return Modular power product of the input arrays.
     */
    public static LargeInteger modPowProd(final LargeInteger[] bases,
                                          final LargeInteger[] exponents,
                                          final LargeInteger modulus) {

// Removed pure java code here.

        final List<LargeInteger> results =
            Collections.synchronizedList(new LinkedList<LargeInteger>());

        ArrayWorker worker =
            new ArrayWorker(bases.length) {
                public void work(int start, int end) {

// Removed pure java code here.
// Enabled calls to native code begins here.

                    BigInteger[] biBases = new BigInteger[end - start];
                    BigInteger[] biExponents = new BigInteger[end - start];

                    for (int i = start, j = 0; i < end; i++, j++) {
                        biBases[j] = bases[i].value;
                    }
                    for (int i = start, j = 0; i < end; i++, j++) {
                        biExponents[j] = exponents[i].value;
                    }

                    results.add(new LargeInteger(JGMPMEE.spowm(biBases,
                                                               biExponents,
                                                               modulus.value)));
// Enabled calls to native code ends here
                }
            };
        worker.work();

        LargeInteger result = ONE;
        for (LargeInteger li : results) {
            result = result.mul(li).mod(modulus);
        }
        return result;
    }

    /**
     * Computes the modular inverse of this instance.
     *
     * @param modulus Modulus.
     * @return Inverse of this instance modulo
     * <code>modulus</code>.
     *
     * @throws ArithmException If <code>modulus</code> is not positive
     * or if this instance does not have a multiplicative inverse
     * modulo <code>modulus</code>.
     */
    public LargeInteger modInv(LargeInteger modulus)
        throws ArithmException {
        try {
            return new LargeInteger(value.modInverse(modulus.value));
        } catch (ArithmeticException ae) {
            throw new ArithmException("Modulus non-positive or "
                                      + "inverse does not exist!", ae);
        }
    }

    /**
     * Reduces this instance modulo the input positive modulus. The
     * result is always non-negative.
     *
     * @param modulus Modulus.
     * @return This instance modulo <code>modulus</code>.
     */
    public LargeInteger mod(LargeInteger modulus) {
        try {
            return new LargeInteger(value.mod(modulus.value));
        } catch (ArithmeticException ae) {
            throw new ArithmError("Non-positive modulus!", ae);
        }
    }

    /**
     * Returns the number of bits needed to represent this integer in
     * two's complement. Note that a single bit is needed to represent
     * zero, so this is not the index of the left-most bit plus one.
     *
     * @return Number of bits in this integer.
     */
    public int bitLength() {
        return value.bitLength();
    }

    /**
     * Returns a string representation, in radix 16, of this instance.
     *
     * @return Representation of this instance.
     */
    public String toString() {
        return toString(16);
    }

    /**
     * Returns a string representation, in the given radix, of this instance.
     *
     * @param radix Basis used for the output.
     * @return Representation of this instance.
     */
    public String toString(int radix) {
        String res = value.toString(radix);

        if (radix == 16 && res.length() % 2 != 0) {
            return "0" + res;
        } else {
            return res;
        }
    }

    /**
     * Returns a <code>byte[]</code> representation of this instance
     * in two's complement. This instance can be recovered using
     * {@link LargeInteger(byte[])}.
     *
     * @return Representation of this instance.
     */
    public byte[] toByteArray() {
        return value.toByteArray();
    }

    /**
     * Returns this instance represented as a {@link BigInteger}.
     *
     * @return Representation of this instance.
     */
    public BigInteger toBigInteger() {
        return value;
    }

    /**
     * Returns a {@link mixnet.eio.ByteTree} representation of
     * this instance.
     *
     * @return Representation of this instance.
     */
    public ByteTree toByteTree() {
        return new ByteTree(value.toByteArray());
    }

    /**
     * Returns a fixed-size {@link mixnet.eio.ByteTree}
     * representation of this instance. The result is undefined if
     * this integer does not fit into a byte array of the given
     * length.
     *
     * @param expectedByteLength Number of bytes stored in byte tree
     * representing this instance.
     * @return Representation of this instance.
     */
    public ByteTree toByteTree(int expectedByteLength) {
        byte[] tmp = value.toByteArray();
        byte[] res = new byte[expectedByteLength];

        // Check if this instance is a negative number.
        if (compareTo(ZERO) < 0) {
            Arrays.fill(res, (byte)0xFF);
        }

        int len = Math.min(expectedByteLength, tmp.length);
        System.arraycopy(tmp, 0, res, expectedByteLength - len, len);

        return new ByteTree(res);
    }

    /**
     * Returns a {@link mixnet.eio.ByteTree} representation of the input
     * <code>LargeInteger[]</code> that can be given as input to
     * {@link #toLargeIntegers(ByteTree)}.
     *
     * @param array Array of integers.
     * @return Representation of the input array.
     */
    public static ByteTreeBasic toByteTree(LargeInteger[] array) {
        ByteTreeBasic[] children = new ByteTreeBasic[array.length];

        for (int i = 0; i < array.length; i++) {
            children[i] = array[i].toByteTree();
        }
        return new ByteTreeContainer(children);
    }

    /**
     * Returns a fixed-size {@link mixnet.eio.ByteTree}
     * representation of the input <code>LargeInteger[]</code> that
     * can be given as input to {@link
     * #toLargeIntegers(ByteTree)}. The result is undefined if this
     * integer does not fit into a byte array of the given length.
     *
     * @param expectedByteLength Number of bytes stored in byte tree
     * representing this instance.
     * @param array Array of integers.
     * @return Representation of the input array.
     */
    public static ByteTreeBasic toByteTree(int expectedByteLength,
                                           LargeInteger[] array) {
        ByteTreeBasic[] children = new ByteTreeBasic[array.length];

        for (int i = 0; i < array.length; i++) {
            children[i] = array[i].toByteTree(expectedByteLength);
        }
        return new ByteTreeContainer(children);
    }

    /**
     * Returns the array of integers represented by the input, that
     * supposedly is output by {@link
     * #toByteTree(LargeInteger[])}. This requires that each integer
     * falls into the given interval. If the expected size (number of
     * elements) is set to zero, then the input can have any size.
     *
     * @param size Expected number of elements.
     * @param btr A representation of an array of integers.
     * @param lb Non-negative inclusive lower bound for the integers.
     * @param ub Positive exclusive upper bound for the integers.
     * @return Array of integers represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent a
     * <code>LargeInteger[]</code> with integers within the given bounds.
     */
    public static LargeInteger[] toLargeIntegers(int size,
                                                 ByteTreeReader btr,
                                                 LargeInteger lb,
                                                 LargeInteger ub)
    throws ArithmFormatException {

        if (size == 0) {
            size = btr.getRemaining();
        }

        if (btr.getRemaining() != size) {
            throw new ArithmFormatException("Unexpected number of integers!");
        }

        int expectedByteLength = ub.toByteArray().length;

        try {

            LargeInteger[] array = new LargeInteger[size];

            for (int i = 0; i < size; i++) {
                array[i] = new LargeInteger(expectedByteLength,
                                            btr.getNextChild(),
                                            null); // null is a dummy
                                                   // variable
                if (array[i].compareTo(lb) < 0
                    || ub.compareTo(array[i]) <= 0) {
                    String s = "Integer is outside permitted interval!";
                    throw new ArithmFormatException(s);
                }
            }
            return array;

        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Calls the given Miller-Rabin instance the given number of
     * times, each time sampling a random base from the given random
     * source. A composite is accepted with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     *
     * @param mr Miller-Rabin testing instance.
     * @param rs Source of randomness.
     * @param certainty Certainty with which the verdict is correct,
     * i.e., the decision is wrong with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     * @return Returns <code>true</code> or <code>false</code>
     * depending on if the tested integer is (probably) a prime or
     * not.
     */
    protected boolean reps(MillerRabin mr, RandomSource rs, int certainty) {
        LargeInteger m = sub(ONE).shiftRight(1);
        boolean res = true;
        LargeInteger base;

        // Make sure that the statistical error plus the failure
        // probability of the test add up to less than 2^(-certainty).
        int reps = (certainty + 1) / 2;    // Prob. goes down as 4^(-reps)
        int l = MathExt.log2c(reps);       // reps = 2^l
        int statDist = l + certainty + 1;  // 2^l2^(-statDist) < 2^(-certainty)

        for (int i = 0; i < reps; i++) {

            // Pick a random base in [2,value-1].
            do {
                base = new LargeInteger(this, statDist, rs);
            } while (base.equals(ZERO) || base.equals(ONE));

            if (!mr.once(base.value)) {
                res = false;
                break;
            }
        }
        return res;
    }

    /**
     * Returns <code>true</code> if and only if the input is
     * (probably) prime. A composite is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     *
     * @param rs Source of randomness used for the bases used in the
     * Miller-Rabin test.
     * @param certainty Certainty with which the verdict is correct,
     * i.e., the decision is wrong with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     * @return <code>true</code> if this is probably a prime and
     * <code>false</code> otherwise.
     */
    public boolean isProbablePrime(RandomSource rs, int certainty) {
        boolean res = true;
        MillerRabin mr = new MillerRabin(value, true, false);

        if (!mr.trial()) {
            return false;
        }

        res = reps(mr, rs, certainty);

        mr.done();

        return res;
    }

    /**
     * Returns <code>true</code> if and only if the input is
     * (probably) prime. A composite is accepted with probability
     * at most 2<sup>-<code>{@link #CERTAINTY}</code></sup>.
     *
     * @param rs Source of randomness used for the bases used in the
     * Miller-Rabin test.
     * @return <code>true</code> if this is probably a prime and
     * <code>false</code> otherwise.
     */
    public boolean isProbablePrime(RandomSource rs) {
        return isProbablePrime(rs, CERTAINTY);
    }

    /**
     * Returns the smallest prime larger than this integer.
     *
     * @param randomSource Source of randomness used in primality
     * testing.
     * @param certainty Certainty with which the verdict is correct,
     * i.e., the decision is wrong with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     * @return Smallest (probable) prime larger than this integer.
     */
    public LargeInteger nextPrime(RandomSource randomSource, int certainty) {
        MillerRabin mr = new MillerRabin(value, true, true);

        // It is unlikely that we need to check more than 2^l
        // integers. The probability of a failure is then bounded by
        // 2^(-certainty).
        int l = 2 * MathExt.log2c(mr.getCurrentCandidate().bitLength());

        while (!reps(mr, randomSource, l + certainty)) {
            mr.nextCandidate();
        }

        BigInteger current = mr.getCurrentCandidate();
        mr.done();

        return new LargeInteger(current);
    }

    /**
     * Calls the given Miller-Rabin instance the given number of
     * times, each time sampling a random base from the given random
     * source. A non-safeprime is accepted with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     *
     * @param mr Miller-Rabin testing instance.
     * @param rs Source of randomness.
     * @param certainty Certainty with which the verdict is correct,
     * i.e., the decision is wrong with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     * @return Returns <code>true</code> or <code>false</code>
     * depending on if the tested integer is (probably) a safe prime
     * or not.
     */
    protected boolean safeReps(MillerRabin mr, RandomSource rs, int certainty) {
        LargeInteger m = sub(ONE).shiftRight(1);
        boolean res = true;
        LargeInteger base;

        // Make sure that the statistical error plus the failure
        // probability of the test add up to less than 2^(-certainty).
        int reps = (certainty + 1) / 2;    // Prob. goes down as 4^(-reps)
        int l = MathExt.log2c(2 * reps);   // 2 * reps = 2^l
        int statDist = l + certainty + 1;  // 2^l2^(-statDist) < 2^(-certainty)

        // We interlace the primality tests of the two integers we
        // hope to be primes.

        for (int i = 0; i < reps; i++) {

            // Pick a random base in [2,value-1].
            do {
                base = new LargeInteger(this, statDist, rs);
            } while (base.equals(ZERO) || base.equals(ONE));

            if (!mr.once(base.value, 0)) {
                res = false;
                break;
            }

            // Pick a random base in [2,m-1]
            do {
                base = new LargeInteger(m, statDist, rs);
            } while (base.equals(ZERO) || base.equals(ONE));

            if (!mr.once(base.value, 1)) {
                res = false;
                break;
            }
        }
        return res;
    }

    /**
     * Returns <code>true</code> if and only if the input is
     * (probably) a safe prime. A non-safe prime is accepted with
     * probability at most 2<sup>-<code>certainty</code></sup>.
     *
     * @param rs Source of randomness used for the bases used in the
     * Miller-Rabin test.
     * @param certainty Certainty with which the verdict is correct,
     * i.e., the decision is wrong with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     * @return <code>true</code> if this is probably a safe prime and
     * <code>false</code> otherwise.
     */
    public boolean isSafePrime(RandomSource rs, int certainty) {
        boolean res = true;
        MillerRabin mr = new MillerRabin(value, false, false);
        if (!mr.trial()) {
            return false;
        }

        res = safeReps(mr, rs, certainty);

        mr.done();
        return res;
    }

    /**
     * Returns <code>true</code> if and only if the input is
     * (probably) a safe prime. A non-safe prime is accepted with
     * probability 2<sup>-{@link #CERTAINTY}</sup>.
     *
     * @param rs Source of randomness used for the bases used in the
     * Miller-Rabin test.
     * @return <code>true</code> if this is probably a safe prime and
     * <code>false</code> otherwise.
     */
    public boolean isSafePrime(RandomSource rs) {
        return isSafePrime(rs, CERTAINTY);
    }

    /**
     * Picks a random integer of the requested bitsize and returns the
     * next (probable) safe prime. The returned integer is not a safe
     * prime with probability at most
     * 2<sup>-<code>certainty</code></sup>. Note that the particular
     * prime that is output has the given number of bits.
     *
     * @param bitLength Number of bits in the output prime.
     * @param randomSource Source of random bits used to
     * initialize the instance.
     * @param certainty Certainty with which the verdict is correct,
     * i.e., the decision is wrong with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     * @return A random safe prime.
     */
    public static LargeInteger randomSafePrime(int bitLength,
                                               RandomSource randomSource,
                                               int certainty) {
        LargeInteger candidate;
        do {
            candidate = new LargeInteger(bitLength, randomSource);
            candidate = candidate.setBit(bitLength - 1);
            candidate = candidate.nextSafePrime(randomSource, certainty);
        } while (candidate.bitLength() > bitLength);

        return candidate;
    }

    /**
     * Returns a safe prime of a given size. The output is composite
     * with probability at most 2<sup>-{@link #CERTAINTY}</sup>.
     *
     * @param bitLength Number of bits in the output prime.
     * @param randomSource Source of random bits used to
     * initialize the instance.
     * @return A random safe prime.
     */
    public static LargeInteger randomSafePrime(int bitLength,
                                               RandomSource randomSource) {
        return randomSafePrime(bitLength, randomSource, CERTAINTY);
    }

    /**
     * Random prime with a fixed bit length.
     *
     * @param bitLength Number of bits in the output prime.
     * @param randomSource Source of random bits used to
     * initialize the instance.
     * @param certainty Certainty with which the verdict is correct,
     * i.e., the decision is wrong with probability at most
     * @return Random prime with a given number of bits.
     */
    public static LargeInteger randomPrimeExact(int bitLength,
                                                RandomSource randomSource,
                                                int certainty) {

        LargeInteger candidate;
        do {

            candidate = new LargeInteger(bitLength, randomSource);
            candidate = candidate.setBit(bitLength - 1);

        } while (!candidate.isProbablePrime(randomSource, certainty));

        return candidate;
    }

    /**
     * Returns the smallest safe prime larger than the input. The
     * output is a composite or non-safe prime with probability with
     * probability at most 2<sup>-<code>certainty</code></sup>.
     *
     * @param randomSource Source of random bits used to
     * initialize the instance.
     * @param certainty Certainty with which the verdict is correct,
     * i.e., the decision is wrong with probability at most
     * 2<sup>-<code>certainty</code></sup>.
     * @return Safe prime integer.
     */
    public LargeInteger nextSafePrime(RandomSource randomSource,
                                      int certainty) {
        MillerRabin mr = new MillerRabin(value, false, true);

        while (!safeReps(mr, randomSource, certainty)) {

            mr.nextCandidate();
        }

        BigInteger current = mr.getCurrentCandidate();
        mr.done();

        return new LargeInteger(current);
    }

    /**
     * Returns the smallest safe prime larger than the input. The
     * output is a composite or non-safe prime with probability with
     * probability at most 2<sup>-{@link #CERTAINTY}</sup>.
     *
     * @param randomSource Source of random bits used to
     * initialize the instance.
     * @return Safe prime integer.
     */
    public LargeInteger nextSafePrime(RandomSource randomSource) {
        return nextSafePrime(randomSource, CERTAINTY);
    }

    /**
     * Generates an array of random integers.
     *
     * @param size Number of integers to generate.
     * @param bitLength Potential number of bits in each integer (if
     * leading bits are zero the actual bit length will be smaller).
     * @param randomSource Source of random bits used to initialize
     * the array.
     * @return Array of random integers.
     */
    public static LargeInteger[] random(int size, int bitLength,
                                        RandomSource randomSource) {
        if (size < 0) {
            throw new ArithmError("Negative size!");
        }
        LargeInteger[] randoms = new LargeInteger[size];
        for (int i = 0; i < size; i++) {
            randoms[i] = new LargeInteger(bitLength, randomSource);
        }
        return randoms;
    }

    /**
     * Generates an array of random integers modulo the given modulus.
     *
     * @param size Number of integers to generate.
     * @param modulus Modulus.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @param randomSource Source of random bits used to initialize
     * the array.
     * @return Array of random integers.
     */
    public static LargeInteger[] random(int size,
                                        LargeInteger modulus,
					int statDist,
					RandomSource randomSource) {
        if (size < 0) {
            throw new ArithmError("Negative size!");
        }
        LargeInteger[] randomArray = new LargeInteger[size];
        for (int i = 0; i < size; i++) {
            randomArray[i] = new LargeInteger(modulus, statDist, randomSource);
        }
        return randomArray;
    }

    /**
     * Returns the Legendre symbol of this instance.
     *
     * @param prime Modulus relative which we compute the legendre
     * symbol of this integer.
     * @return Legendre symbol.
     */
    public int legendre(LargeInteger prime) {
// Removed pure java code here.
// Enabled calls to native code begins here.
        return JGMPMEE.legendre(value, prime.value);
// Enabled calls to native code ends here
    }

    /**
     * Returns the Jacobi symbol of this instance modulo the
     * input. This is an implementation of the binary Jacobi-symbol
     * algorithm.
     *
     * @param value Integer to test.
     * @param modulus An odd modulus.
     * @return Jacobi symbol of this instance modulo the input.
     */
    public static int jacobiSymbol(BigInteger value, BigInteger modulus) {

        if (!modulus.testBit(0)) {
            throw new ArithmError("Jacobi symbol can not be computed!");
        }

        if (modulus.compareTo(BigInteger.ZERO) < 0) {
            throw new ArithmError("Negative modulus!");
        }

        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new ArithmError("Negative value!");
        }

        BigInteger a = value;
        BigInteger n = modulus;

        int sa = 1;
        for (;;) {

            if (a.equals(BigInteger.ZERO)) {

                return 0;

            } else if (a.equals(BigInteger.ONE)) {

                return sa;

            } else {

                int e = a.getLowestSetBit();
                a = a.shiftRight(e);
                int intn = n.intValue() & 0x000000FF;
                int s = 1;
                if (e % 2 == 1 && (intn % 8 == 3 || intn % 8 == 5)) {
                    s = -1;
                }

                int inta = a.intValue() & 0x000000FF;
                if (intn % 4 == 3 && inta % 4 == 3) {
                    s = -s;
                }

                sa *= s;
                if (a.equals(BigInteger.ONE)) {
                    return sa;
                }
                n = n.mod(a);
                BigInteger t = a;
                a = n;
                n = t;
            }
        }
    }

    /**
     * Returns true or false depending on if all integers are
     * quadratic residues modulo the given prime or not.
     *
     * @param integers Integers to test.
     * @param prime Prime modulus.
     * @return True or false depending on if the input integers are
     * quadratic residues modulo the given modulus or not.
     */
    public static boolean quadraticResidues(final LargeInteger[] integers,
                                            final LargeInteger prime) {

        final List<Boolean> results =
            Collections.synchronizedList(new LinkedList<Boolean>());

        ArrayWorker worker =
            new ArrayWorker(integers.length) {
                public void work(int start, int end) {

                    for (int i = start; i < end; i++) {
                        if (integers[i].legendre(prime) != 1) {
                            results.add(Boolean.FALSE);
                            break;
                        }
                    }
                    results.add(Boolean.TRUE);
                }
            };
        worker.work();

        for (Boolean part : results) {
            if (!part.booleanValue()) {
                return false;
            }
        }
        return true;
    }
}
