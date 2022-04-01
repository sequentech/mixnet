
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

import java.io.*;
import java.util.*;

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.util.*;

/**
 * Abstract class representing an immutable group element in a group
 * where each element has prime order. The group is represented by (a
 * subclass of) the class {@link PGroup}.
 *
 * @author Douglas Wikstrom
 */
public abstract class PGroupElement
    implements Comparable<PGroupElement>,
               ByteTreeConvertible, PGroupAssociated {

    /**
     * Group to which this element belongs.
     */
    protected PGroup pGroup;

    /**
     * Initializes this instance.
     *
     * @param pGroup Group to which this element belongs.
     */
    protected PGroupElement(PGroup pGroup) {
        this.pGroup = pGroup;
    }

    /**
     * Returns a {@link String} representation of this element. This
     * is mainly useful for debugging. It should not be used to store
     * elements.
     *
     * @return Representation of this element.
     */
    public abstract String toString();

    /**
     * Recovers a <code>byte[]</code> from its encoding as an element
     * in the group, i.e., the output of {@link
     * PGroup#encode(byte[],int,int)}. At most {@link
     * PGroup#getEncodeLength()} bytes are written. Every group
     * element must decode to some <code>byte[]</code>, so this method
     * never fails.
     *
     * @param array Where the bytes are stored.
     * @param startIndex Start index where to put the decoded bytes.
     * @return Number of bytes written to <code>array</code>.
     */
    public abstract int decode(byte[] array, int startIndex);

    /**
     * Returns the product of the input and this instance.
     *
     * @param el Element with which this instance is multiplied.
     * @return Product of this element and the input.
     */
    public abstract PGroupElement mul(PGroupElement el);

    /**
     * Returns the inverse of this instance.
     *
     * @return Inverse of this element.
     */
    public abstract PGroupElement inv();

    /**
     * Returns this element to the power of the input. Note that this
     * may be more general than simple exponentiation if, e.g., this
     * is an instance of {@link PPGroupElement}.
     *
     * @param exponent Exponent to which power we take this
     * instance.
     * @return This element to the power of the input.
     */
    public abstract PGroupElement exp(PRingElement exponent);

    /**
     * Returns the array of element-wise powers of this element.
     *
     * @param exponents Exponents to which power we take this
     * instance.
     * @return This element to the powers of the input.
     */
    public abstract PGroupElementArray exp(PRingElementArray exponents);

    /**
     * Orders the elements in the group "lexicographically". The
     * ordering is obviously not compatible with the binary group
     * operator in any interesting way, but it is useful to have some
     * ordering to be able to sort elements.
     *
     * @param el Instance to which this element is compared.
     * @return -1, 0, or 1 depending on if this element comes before,
     * is equal to, or comes after the input.
     */
    public abstract int compareTo(PGroupElement el);

    /**
     * Returns true if and only if the input represents the same group
     * element as this instance.
     *
     * @param obj Element compared to this instance.
     * @return true if this element equals the input and false
     * otherwise.
     */
    public abstract boolean equals(Object obj);


    // ############ Implemented in terms of the above. ###########

    /**
     * Returns this instance to the powers of the elements in
     * <code>exponents</code>.
     *
     * @param exponents Powers to be taken.
     * @return Basis to the powers of the given exponents.
     */
    public PGroupElement[] naiveExp(final PRingElement[] exponents) {
        final PGroupElement[] res = new PGroupElement[exponents.length];

        ArrayWorker worker =
            new ArrayWorker(res.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = exp(exponents[i]);
                    }
                }
            };
        worker.work(pGroup.expThreadThreshold);
        return res;
    }

    /**
     * Returns this instance to the powers of the elements in
     * <code>exponents</code>.
     *
     * @param exponents Powers to be taken.
     * @return Basis to the powers of the given exponents.
     */
    public PGroupElement[] exp(final PRingElement[] exponents) {

        // Extract integer exponents and determine maximal bitLength.
        final LargeInteger[] integers = new LargeInteger[exponents.length];
        int bitLength = 0;

        for (int i = 0; i < exponents.length; i++) {

            integers[i] = ((PFieldElement)exponents[i]).toLargeInteger();
            bitLength = Math.max(integers[i].bitLength(), bitLength);
        }

        // Optimal width for the given bit length and number of
        // exponents.
        int width = PGroupFixExpTab.optimalWidth(bitLength, integers.length);

        final PGroupFixExpTab tab = new PGroupFixExpTab(this, bitLength, width);

        // Compute result.
        final PGroupElement[] res = new PGroupElement[integers.length];

        ArrayWorker worker =
            new ArrayWorker(res.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {

                        res[i] = tab.exp(integers[i]);
                    }
                }
            };
        worker.work(pGroup.expThreadThreshold);

        return res;
    }

    /**
     * Returns a raw <b>fixed-size</b> representation of the
     * instance. It should only be used as input to hash functions
     * etc. In particular it should not be used for storing
     * elements. The method is an injective map from the set of group
     * elements to the set of <code>byte[]</code> of length {@link
     * PGroup#getByteLength()}.
     *
     * @return Raw fixed size representation of this element as a
     * <code>byte[]</code>.
     */
    public byte[] toByteArray() {
        return toByteTree().toByteArray();
    }

    /**
     * Returns this instance divided by the input.
     *
     * @param el Divisor.
     * @return This instance divided by the input.
     */
    public PGroupElement div(PGroupElement el) {
        PGroupElement inverted = el.inv();
        PGroupElement res = mul(inverted);
        inverted.free();
        return res;
    }

    /**
     * Returns this element to the power of the input.
     *
     * @param exponent Exponent to which power we take this
     * instance.
     * @return This element to the power of the input.
     */
    public PGroupElement exp(int exponent) {
        LargeInteger liExponent = new LargeInteger(exponent);
        return exp(pGroup.pRing.getPField().toElement(liExponent));
    }

    /**
     * Raises this element to the given scalar and multiplies the
     * result by the factor.
     *
     * @param scalar Scalar exponent.
     * @param factor Multiplier.
     * @return This instance raised to the given scalar power and
     * multiplied by factor.
     */
    public PGroupElement expMul(PRingElement scalar, PGroupElement factor) {
        PGroupElement expd = exp(scalar);
        PGroupElement res = expd.mul(factor);
        expd.free();
        return res;
    }

    /**
     * Recovers a <code>byte[]</code> from its encoding as an element
     * in the group, i.e., the output of {@link
     * PGroup#encode(byte[],int,int)}.
     *
     * @return Decoded bytes.
     */
    public byte[] decode() {
        byte[] tmp = new byte[pGroup.getByteLength()];
        int len = decode(tmp, 0);
        return Arrays.copyOf(tmp, len);
    }

    /**
     * Frees resources allocated by this instance. Continued use of a
     * freed instance is undefined.
     */
    public void free() {}


    // Documented in PGroupAssociated.java

    public PGroup getPGroup() {
        return pGroup;
    }
}
