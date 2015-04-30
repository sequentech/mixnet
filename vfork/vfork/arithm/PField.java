
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

package vfork.arithm;

import java.io.*;
import java.util.*;

import vfork.crypto.*;
import vfork.eio.*;

/**
 * Implements an immutable field of prime order, i.e., arithmetic
 * modulo a prime. The elements of the field are implemented by the
 * class {@link PFieldElement}.
 *
 * @author Douglas Wikstrom
 */
public class PField extends PRing {

    /**
     * Makes sure that every field with the same order uses the same
     * instance of {@link LargeInteger} to represent it. This speeds
     * up later initializations and checking for equal fields, since
     * it can be done by comparing references.
     */
    protected static TreeMap<LargeInteger, LargeInteger> orders;

    static {
        orders = new TreeMap<LargeInteger, LargeInteger>();
    }

    /**
     * Order of the field.
     */
    protected LargeInteger order;

    /**
     * Number of bits in the order of this field.
     */
    protected int bitLength;

    /**
     * Fixed number of bytes needed to store the order.
     */
    protected int orderByteLength;

    /**
     * Fixed number of bytes needed to injectively map elements to
     * byte[].
     */
    protected int byteLength;

    /**
     * Zero element of the field.
     */
    public final PFieldElement ZERO;

    /**
     * Unit element of the field.
     */
    public final PFieldElement ONE;

    /**
     * Creates an empty uninitialized instance. It is the
     * responsibility of the programmer to initialize this field
     * before usage.
     */
    PField() {
        ZERO = new PFieldElement(this, LargeInteger.ZERO);
        ONE = new PFieldElement(this, LargeInteger.ONE);
    }

    /**
     * Initializes the order of this instance without checking that
     * the order is a positive prime.
     *
     * @param order Order of this field.
     */
    void unsafeInit(LargeInteger order) {
        this.order = order;
        this.bitLength = order.bitLength();
        this.orderByteLength = order.toByteArray().length;
        this.byteLength = ONE.toByteArray().length;
    }

    /**
     * Creates a field with the given order.
     *
     * @param order Order of the field.
     */
    public PField(LargeInteger order) {
        this();
        unsafeInit(order);
    }

    /**
     * Initializes the order of this instance.
     *
     * @param order Order of this field.
     * @param rs Random source used to probabilistically check
     * primality.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     *
     * @throws ArithmFormatException If the order is not a positive prime.
     */
    protected void init(LargeInteger order, RandomSource rs, int certainty)
        throws ArithmFormatException {

        if (orders.containsKey(order)) {

            // This ensures that all fields with the same order
            // represent their order using the same instance of
            // LargeInteger.
            order = orders.get(order);

        } else {

            if (order.compareTo(LargeInteger.ZERO) < 0) {
                throw new ArithmFormatException("Non-positive order!");
            }

            if (!order.isProbablePrime(rs, certainty)) {
                    throw new ArithmFormatException("Non-prime order!");
            }
            orders.put(order, order);
        }
        unsafeInit(order);
    }

    /**
     * Creates a field with the given order.
     *
     * @param order Order of the field.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     *
     * @throws ArithmFormatException If the modulus is not a positive
     * prime number.
     */
    public PField(LargeInteger order, RandomSource rs, int certainty)
        throws ArithmFormatException {
        this();
        init(order, rs, certainty);
    }

    /**
     * Returns the order of the field.
     *
     * @return Order of this field.
     */
    public LargeInteger getOrder() {
        return order;
    }

    /**
     * Returns an array containing the canonical integer
     * representative of each field element in the input. This assumes
     * that all elements in the input belong to the same field.
     *
     * @param elements Array of field elements.
     * @return Array containing the canonical integer representative
     * of each field element in the input.
     */
    public LargeInteger[] toLargeIntegers(PRingElement[] elements) {
        if (elements.length == 0) {

            return new LargeInteger[0];

        } else if (equals(elements[0].getPRing())) {

            LargeInteger[] res = new LargeInteger[elements.length];
            for (int i = 0; i < elements.length; i++) {
                res[i] = ((PFieldElement)elements[i]).toLargeInteger();
            }
            return res;

        }
        throw new ArithmError("Elements of foreign field!");
    }

    /**
     * Returns an array containing the canonical integer
     * representative of each field element in the input. This assumes
     * that all elements in the input belongs to the same field.
     *
     * @param elements Array of field elements.
     * @return Array containing the canonical integer representative
     * of each field element in the input.
     */
    public LargeIntegerArray toLargeIntegerArray(PRingElementArray elements) {
        if (equals(elements.getPRing())) {
            return ((PFieldElementArray)elements).values;
        }
        throw new ArithmError("Elements of foreign field!");
    }

    /**
     * Returns an element corresponding to the equivalence class of
     * the input integer.
     *
     * @return Field element corresponding to the input
     * representative.
     * @param li Representative of a field element.
     */
    public PFieldElement toElement(LargeInteger li) {
        return new PFieldElement(this, li.mod(order));
    }

    /**
     * Returns an element corresponding to the equivalence class of
     * the input integer.
     *
     * @param i Non-negative integer representative of a field
     * element.
     * @return Field element corresponding to the input.
     */
    public PFieldElement toElement(int i) {
        if (i < 0) {
            throw new ArithmError("Negative integer!");
        }
        return toElement(new LargeInteger(i));
    }

    /**
     * Returns an array of the field elements corresponding to the
     * integers in the input.
     *
     * @param integers Integer representatives of field elements.
     * @return Field elements corresponding to the input integers.
     */
    public PFieldElementArray toElementArray(LargeIntegerArray integers) {
        LargeIntegerArray reduced = integers.mod(order);
        return new PFieldElementArray(this, reduced);
    }

    /**
     * Returns an array of the field elements corresponding to the
     * integers in the input. WARNING! This method does not reduce the
     * integers in the input to canonical representatives and simply
     * absorbs the input array. Do not call {@link
     * #LargeIntegerArray.free()} directly on the input array.
     *
     * @param integers Integer representatives of field elements.
     * @return Field elements corresponding to the input integers.
     */
    public PFieldElementArray unsafeToElementArray(LargeIntegerArray integers) {
        return new PFieldElementArray(this, integers);
    }


    // Documented in PRing.java

    public PField getPField() {
        return this;
    }

    public PFieldElement getZERO() {
        return ZERO;
    }

    public PFieldElement getONE() {
        return ONE;
    }

    public int getByteLength() {
        return byteLength;
    }

    public String toString() {
        return order.toString(16);
    }

    public int getEncodeLength() {
        return (order.bitLength() - 1) / 8;
    }

    public PFieldElement toElement(ByteTreeReader btr)
        throws ArithmFormatException {
        return new PFieldElement(this, btr);
    }

    public PFieldElement toElement(byte[] bytes) {
        return new PFieldElement(this,
                                 LargeInteger.toPositive(bytes).mod(order));
    }

    public PFieldElement toElement(byte[] bytes, int offset, int length) {
        return new PFieldElement(this,
                                 LargeInteger.toPositive(bytes,
                                                         offset,
                                                         length).mod(order));
    }

    public PFieldElement randomElement(RandomSource rs, int statDist) {
        LargeInteger li = new LargeInteger(bitLength + statDist, rs);
        return new PFieldElement(this, li.mod(order));
    }

    public PFieldElementArray randomElementArray(int size,
                                                 RandomSource rs,
                                                 int statDist) {
        return new PFieldElementArray(this, size, rs, statDist);
    }

    public PRingElementArray toElementArray(PRingElement[] elements) {
        return new PFieldElementArray(this, elements);
    }

    public PRingElementArray toElementArray(int size, PRingElement element) {
        LargeIntegerArray values =
            LargeIntegerArray.fill(size, ((PFieldElement)element).value);
        return new PFieldElementArray(this, values);
    }

    public PFieldElementArray toElementArray(int size, ByteTreeReader btr)
            throws ArithmFormatException {
        return new PFieldElementArray(this, size, btr);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PField)) {
            return false;
        }
        PField pField = (PField)obj;
        return pField.order == order || pField.order.equals(order);
    }

    public boolean contains(PRingElement el) {
        return equals(el.getPRing());
    }


    // Documented in ByteTreeConvertible.java.

    public ByteTree toByteTree() {
        return new ByteTree(order.toByteArray());
    }
}
