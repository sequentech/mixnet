
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

import verificatum.crypto.*;
import verificatum.eio.*;

/**
 * Abstract base class of immutable direct powers of a prime order
 * field. The elements of the ring are instances of {@link
 * PRingElement} and arrays of such elements are instances of {@link
 * PRingElementArray}.
 *
 * @author Douglas Wikstrom
 */
public abstract class PRing implements PRingAssociated, ByteTreeConvertible {

    /**
     * Returns the underlying field.
     *
     * @return Underlying field.
     */
    public abstract PField getPField();

    /**
     * Returns the zero element of this ring.
     *
     * @return Zero element of this ring.
     */
    public abstract PRingElement getZERO();

    /**
     * Returns the unit element of this ring.
     *
     * @return Unit element of this ring.
     */
    public abstract PRingElement getONE();

    /**
     * Returns the fixed number of bytes used to map an element
     * injectively to a fixed-size <code>byte[]</code>.
     *
     * @return Number of bytes in outputs from {@link
     * PRingElement#toByteArray()}.
     */
    public abstract int getByteLength();

    /**
     * Outputs a human readable description of the ring. This should
     * only be used for debugging.
     *
     * @return Representation of this ring.
     */
    public abstract String toString();

    /**
     * Number of bytes that can be directly encoded into an element
     * of the ring.
     *
     * @return Number of bytes that can be encoded into an element of
     * the ring.
     */
    public abstract int getEncodeLength();

    /**
     * Recovers a ring element from the input representation.
     *
     * @param btr Representation of a ring element.
     * @return Ring element corresponding to the input.
     *
     * @throws ArithmFormatException If the input does not represent a
     * ring element.
     */
    public abstract PRingElement toElement(ByteTreeReader btr)
        throws ArithmFormatException;

    /**
     * Creates a ring element from a raw array of bytes. This should
     * be used to create an instance from outputs from hash functions
     * etc. The restriction of this map to any subring is
     * injective. This method should be used to create an instance
     * from outputs from hash functions etc. It should never be used
     * to recover stored ring elements. If more than {@link
     * #getEncodeLength()} bytes are input, then the output is
     * undefined.
     *
     * @param bytes An array containing arbitrary bytes.
     * @param offset Index of first byte to use.
     * @param length Maximal number of bytes to use.
     * @return Ring element derived from input.
     */
    public abstract PRingElement toElement(byte[] bytes, int offset,
                                           int length);

    /**
     * Returns a random ring element.
     *
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Ring element chosen at random.
     */
    public abstract PRingElement randomElement(RandomSource rs, int statDist);

    /**
     * Returns an array of random ring elements.
     *
     * @param size Size of resulting array.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Array of random ring elements.
     */
    public abstract PRingElementArray randomElementArray(int size,
                                                         RandomSource rs,
                                                         int statDist);

    /**
     * Creates an instance containing the given elements.
     *
     * @param elements Elements to be contained in the array.
     * @return Array containing the input group elements.
     */
    public abstract PRingElementArray toElementArray(PRingElement[] elements);

    /**
     * Creates an instance of the given size filled with copies of the
     * input element.
     *
     * @param size Number of elements in resulting array.
     * @param element Element to use.
     * @return Array containing the given number of copies of the element.
     */
    public abstract PRingElementArray toElementArray(int size,
                                                     PRingElement element);

    /**
     * Recovers an array of ring elements from its representation and
     * throws an exception if the input is incorrect.
     *
     * @param size Expected number of elements in array.
     * @param btr Representation of instance.
     * @return Array of ring elements represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * an array of ring elements.
     */
    public abstract PRingElementArray toElementArray(int size,
                                                     ByteTreeReader btr)
            throws ArithmFormatException;

    /**
     * Returns true if and only if the input represents the same ring
     * as this instance.
     *
     * @param obj Object compared to this instance.
     * @return true if this instance equals the input and false
     * otherwise.
     */
    public abstract boolean equals(Object obj);


    // Documented in PRingAssociated.java.

    public PRing getPRing() {
        return this;
    }


    // Methods defined in terms of the abstract methods above.

    /**
     * Returns the characteristic of this ring.
     *
     * @return Characteristic of this ring.
     */
    public LargeInteger getCharacteristic() {
        return getPField().getOrder();
    }

    /**
     * Returns true if the input is contained in this ring and false
     * otherwise.
     *
     * @param element Ring element.
     * @return true if the input is contained in this ring and false
     * otherwise.
     */
    public boolean contains(PRingElement element) {
        return element.getPRing().equals(this);
    }

    /**
     * Returns an array of random ring elements.
     *
     * @param size Size of resulting array.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Array of random ring elements.
     */
    public PRingElement[] randomElements(int size,
                                         RandomSource rs,
                                         int statDist) {
        PRingElement[] res = new PRingElement[size];
        for (int i = 0; i < size; i++) {
            res[i] = randomElement(rs, statDist);
        }
        return res;
    }

    /**
     * Creates a ring element from a raw array of bytes. The
     * restriction of this map to any subring is injective. This
     * method should be used to create an instance from outputs from
     * hash functions etc. It should never be used to recover stored
     * ring elements. If more than {@link #getEncodeLength()} bytes
     * are input, then the output is undefined.
     *
     * @param bytes An array containing arbitrary bytes.
     * @return Ring element derived from the input.
     */
    public PRingElement toElement(byte[] bytes) {
        return toElement(bytes, 0, bytes.length);
    }

    /**
     * Recovers an array of ring elements from the given
     * representation.
     *
     * @param maxSize Maximal number of elements read.
     * @param btr Representation of an array of elements.
     * @return Array of ring elements represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * an array of ring elements.
     */
    public PRingElement[] toElements(int maxSize, ByteTreeReader btr)
    throws ArithmFormatException {
        try {
            if (btr.getRemaining() > maxSize) {
                throw new ArithmFormatException("Too many elements!");
            }

            PRingElement[] res = new PRingElement[btr.getRemaining()];
            for (int i = 0; i < res.length; i++) {
                res[i] = toElement(btr.getNextChild());
            }
            return res;
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed byte tree!", eioe);
        }
    }

    /**
     * Returns the inner product of the input arrays.
     *
     * @param elements1 Array of ring elements.
     * @param elements2 Array of ring elements.
     * @return Inner product of inputs.
     */
    public PRingElement innerProduct(PRingElement[] elements1,
                                     PRingElement[] elements2) {
        if (elements1.length != elements2.length) {
            throw new ArithmError("Different lengths!");
        }
        PRingElement innerProduct = getZERO();
        for (int i = 0; i < elements1.length; i++) {
            innerProduct =
                innerProduct.add(elements1[i].mul(elements2[i]));
        }
        return innerProduct;
    }

    /**
     * Returns a byte tree representation of the elements in the
     * input.
     *
     * @param elements Ring elements.
     * @return Representation of the input <code>PRingElement[]</code>.
     */
    public ByteTreeBasic toByteTree(PRingElement[] elements) {
        ByteTreeBasic[] byteTrees = new ByteTreeBasic[elements.length];
        for (int i = 0; i < byteTrees.length; i++) {
            byteTrees[i] = elements[i].toByteTree();
        }
        return new ByteTreeContainer(byteTrees);
    }

    /**
     * Returns the result of scaling each element of
     * <code>factors</code> by <code>factor</code> and adding the
     * corresponding integer of <code>terms</code>.
     *
     * @param factors Array of ring elements.
     * @param factor Scaling element.
     * @param terms Terms added after scaling.
     * @return Array of results.
     */
    public PRingElement[] mulAdd(PRingElement[] factors,
                                 PRingElement factor,
                                 PRingElement[] terms) {
            if (factors.length != terms.length) {
                throw new ArithmError("Different lengths!");
            }

            PRingElement[] res = new PRingElement[factors.length];
            for (int i = 0; i < factors.length; i++) {
                res[i] = factors[i].mul(factor).add(terms[i]);
            }
            return res;
    }

    /**
     * Verifies that all input instances are associated with the same
     * ring instance.
     *
     * @param els Ring associated instances to be tested.
     * @return true or false depending on if all elements are
     * compatible or not.
     */
    public static boolean compatible(PRingAssociated ... els) {
        if (els.length == 0) {

            return true;

        } else {

            PRing pRing = els[0].getPRing();
            for (int i = 1; i < els.length; i++) {
                if (!els[i].getPRing().equals(pRing)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Verifies that all input instances are associated with the same
     * ring or with a group compatible with the ring to which the
     * other instances are associated.
     *
     * @param g Group associated instance to be tested.
     * @param els Ring associated instances to be tested.
     * @return true or false depending on if all elements are
     * compatible or not.
     */
    public static boolean compatible(PGroupAssociated g,
                                     PRingAssociated ... els) {
            return compatible(els)
                && (els.length == 0
                    || g.getPGroup().getPRing().equals(els[0].getPRing()));
    }
}
