package verificatum.arithm;

import java.io.*;
import java.math.*;
import java.util.*;

import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.util.*;


/**
 * Simple wrapper of primitive arrays of {@link LargeInteger}. This
 * works well as long as the arrays are not too large. For very large
 * arrays you should use {@link LargeIntegerArrayF} which is file
 * based, but neither this nor <code>LargeIntegerArrayF</code> should
 * be used directly. Use the initialization and factory methods of the
 * {@link LargeIntegerArray} base class instead.
 *
 * @author Douglas Wikstrom
 */
public class LargeIntegerArrayIM extends LargeIntegerArray {

    /**
     * Representation of the elements of this instance.
     */
    public LargeInteger[] li;

    /**
     * Constructs an instance with the given integers.
     *
     * @param integers Integers of this instance.
     */
    LargeIntegerArrayIM(LargeInteger[] integers) {
        li = Arrays.copyOf(integers, integers.length);
    }

    /**
     * Constructs the concatenation of the inputs.
     *
     * @param arrays Arrays to be concatenated.
     * @return Concatenation of the inputs.
     */
    public LargeIntegerArrayIM(LargeIntegerArray ... arrays) {

        int total = 0;
        for (int i = 0; i < arrays.length; i++) {
            total += arrays[i].size();
        }

        li = new LargeInteger[total];

        int offset = 0;
        for (int i = 0; i < arrays.length; i++) {
            int len = arrays[i].size();
            System.arraycopy(((LargeIntegerArrayIM)arrays[i]).li, 0,
                             li, offset,
                             len);
            offset += len;
        }
    }


    /**
     * Constructs an array of random integers.
     *
     * @param size Number of integers to generate.
     * @param bitLength Number of bits in each integer.
     * @param randomSource Source of random bits used to initialize
     * the array.
     */
    LargeIntegerArrayIM(int size, int bitLength, RandomSource randomSource) {
        li = LargeInteger.random(size, bitLength, randomSource);
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of integers to generate.
     * @param modulus Modulus.
     * @param statDist Decides the statistical distance from the
     * @param randomSource Source of random bits used to initialize
     * the array.
     * @return Array of random integers.
     */
    LargeIntegerArrayIM(int size, LargeInteger modulus, int statDist,
                        RandomSource randomSource) {
        li = LargeInteger.random(size, modulus, statDist, randomSource);
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of elements to generate.
     * @param value
     */
    LargeIntegerArrayIM(int size, LargeInteger value) {
        li = new LargeInteger[size];
        Arrays.fill(li, value);
    }

    /**
     * Returns the array of integers represented by the input. This
     * constructor requires that each integer falls into the given
     * interval, but also that the representation of each integer is
     * of equal size to the byte tree representation of the upper
     * bound.
     *
     * @param size Expected number of elements in array.
     * @param btr Should contain a representation of an array of
     * integers.
     * @param lb Non-negative inclusive lower bound for integers.
     * @param ub Positive exclusive upper bound for integers.
     *
     * @throws ArithmFormatException If the input does not represent a
     * an array of integers satisfying the given bounds.
     */
    LargeIntegerArrayIM(int size, ByteTreeReader btr,
                        LargeInteger lb, LargeInteger ub)
        throws IOException, EIOException, ArithmFormatException {
        li = LargeInteger.toLargeIntegers(size, btr, lb, ub);
    }


    // Documented in LargeIntegerArray.java

    public LargeIntegerIterator getIterator() {
	return new LargeIntegerIteratorIM(this);
    }

    public LargeIntegerArray modInv(LargeInteger modulus)
    throws ArithmException {
	return new LargeIntegerArrayIM(LargeInteger.modInv(li, modulus));
    }

    public LargeIntegerArray copyOfRange(int startIndex, int endIndex) {
        LargeInteger[] cli = Arrays.copyOfRange(li, startIndex, endIndex);
        return new LargeIntegerArrayIM(cli);
    }

    public LargeInteger[] integers() {
        return Arrays.copyOf(li, li.length);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LargeIntegerArrayIM)) {
            return false;
        }

	return Arrays.equals(li, ((LargeIntegerArrayIM)obj).li);
    }

    public LargeIntegerArray extract(boolean[] valid) {
        if (li.length != valid.length) {
            throw new ArithmError("Different lengths!");
        }
        int total = 0;
        for (int i = 0; i < valid.length; i++) {
            if (valid[i]) {
                total++;
            }
        }
        LargeInteger[] res = new LargeInteger[total];
        int i = 0;
        for (int index = 0; index < valid.length; index++) {
            if (valid[index]) {
                res[i] = li[index];
                i++;
            }
        }
        return new LargeIntegerArrayIM(res);
    }

    public LargeIntegerArray permute(Permutation permutation) {
        LargeInteger[] permInts = new LargeInteger[li.length];
        permutation.applyPermutation(li, permInts);
        return new LargeIntegerArrayIM(permInts);
    }

    public LargeIntegerArray modAdd(LargeIntegerArray termsArray,
                                    LargeInteger modulus) {
	LargeInteger[] terms = ((LargeIntegerArrayIM)termsArray).li;
	return new LargeIntegerArrayIM(LargeInteger.modAdd(li,
							   terms,
							   modulus));
    }
    public LargeIntegerArray modNeg(LargeInteger modulus) {
	return new LargeIntegerArrayIM(LargeInteger.modNeg(li,
							   modulus));
    }

    public LargeIntegerArray modMul(LargeIntegerArray factorsArray,
                                    LargeInteger modulus) {
	LargeInteger[] factors = ((LargeIntegerArrayIM)factorsArray).li;
	return new LargeIntegerArrayIM(LargeInteger.modMul(li,
							   factors,
							   modulus));
    }

    public LargeIntegerArray modMul(LargeInteger scalar, LargeInteger modulus) {
	return new LargeIntegerArrayIM(LargeInteger.modMul(li,
							   scalar,
							   modulus));
    }

    public LargeIntegerArray modPow(LargeIntegerArray exponentsArray,
                                    LargeInteger modulus) {
        LargeInteger[] exponents = ((LargeIntegerArrayIM)exponentsArray).li;
        LargeInteger[] res = LargeInteger.modPow(li, exponents, modulus);
        return new LargeIntegerArrayIM(res);
    }

    public LargeIntegerArray modPow(LargeInteger exponent,
                                    LargeInteger modulus) {
        LargeInteger[] res =
            LargeInteger.modPow(li, exponent, modulus);
        return new LargeIntegerArrayIM(res);
    }

    public LargeIntegerArray modPowVariant(LargeInteger basis,
					   LargeInteger modulus) {
        LargeInteger[] res = basis.modPow(li, modulus);
        return new LargeIntegerArrayIM(res);
    }

    public LargeInteger modPowProd(LargeIntegerArray exponentsArray,
                                   LargeInteger modulus) {
        LargeInteger[] exponents = ((LargeIntegerArrayIM)exponentsArray).li;
        return LargeInteger.modPowProd(li, exponents, modulus);
    }

    public LargeIntegerArray modProds(LargeInteger modulus) {
        LargeInteger[] res =
            LargeInteger.modProds(LargeInteger.ONE, li, modulus);
        return new LargeIntegerArrayIM(res);
    }

    public LargeInteger get(int index) {
        return li[index];
    }

    public LargeIntegerArray shiftPush(LargeInteger integer) {
        LargeInteger[] res = new LargeInteger[li.length];
        res[0] = integer;
        System.arraycopy(li, 0, res, 1, li.length - 1);
        return new LargeIntegerArrayIM(res);
    }

    public Pair<LargeIntegerArray,LargeInteger>
        modRecLin(LargeIntegerArray array, LargeInteger modulus) {
	LargeInteger[] integers1 = ((LargeIntegerArrayIM)array).li;

        LargeInteger[] res = new LargeInteger[li.length];
        res[0] = li[0];
        for (int i = 1; i < res.length; i++) {
            res[i] = res[i - 1].mul(integers1[i]).add(li[i]).mod(modulus);
        }
        return new Pair<LargeIntegerArray,LargeInteger>
            (new LargeIntegerArrayIM(res), res[res.length - 1]);
    }

    public ByteTreeBasic toByteTree() {
        return LargeInteger.toByteTree(li);
    }

    public ByteTreeBasic toByteTree(int expectedByteLength) {
        return LargeInteger.toByteTree(expectedByteLength, li);
    }

    public boolean quadraticResidues(LargeInteger prime) {
        return LargeInteger.quadraticResidues(li, prime);
    }

    public int size() {
        return li.length;
    }

    public void free() {

        // Allow garbage collection of the underlying primitive array.
        li = null;
    }

    public int compareTo(LargeIntegerArray array) {
	return LargeInteger.compareTo(li, ((LargeIntegerArrayIM)array).li);
    }

    public boolean[] equalsAll(LargeIntegerArray array) {
	return LargeInteger.equalsAll(li, ((LargeIntegerArrayIM)array).li);
    }

    public LargeInteger modSum(LargeInteger modulus) {
        return LargeInteger.modSum(li, modulus);
    }

    public LargeInteger modProd(LargeInteger modulus) {
        return LargeInteger.modProd(li, modulus);
    }

    public LargeInteger modInner(LargeIntegerArray vectorArray,
                                 LargeInteger modulus) {
	return LargeInteger.modInner(li,
				     ((LargeIntegerArrayIM)vectorArray).li,
				     modulus);
    }
}
