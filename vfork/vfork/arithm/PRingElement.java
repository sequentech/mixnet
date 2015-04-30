
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

import java.util.*;

import vfork.crypto.*;
import vfork.eio.*;

/**
 * Implements an immutable ring element of a {@link PRing} instance.
 *
 * @author Douglas Wikstrom
 */
public abstract class PRingElement
    implements PRingAssociated, ByteTreeConvertible {

    /**
     * Ring to which this element belongs.
     */
    protected PRing pRing;

    /**
     * Creates an instance.
     *
     * @param pRing Ring to which the resulting element should
     * belong.
     */
    protected PRingElement(PRing pRing) {
        this.pRing = pRing;
    }

    /**
     * Returns a human readable representation of this instance. This
     * should only be used for debugging.
     *
     * @return Representation of this instance.
     */
    public abstract String toString();

    /**
     * Returns the sum of this instance and the input.
     *
     * @param term Element added to this instance.
     * @return Sum of this instance and the input.
     */
    public abstract PRingElement add(PRingElement term);

    /**
     * Returns the negative of this instance (additive inverse).
     *
     * @return Negative of this instance.
     */
    public abstract PRingElement neg();

    /**
     * Returns the product of the input and this instance.
     *
     * @param factor Element multiplied with this instance.
     * @return Product of this instance and the input.
     */
    public abstract PRingElement mul(PRingElement factor);

    /**
     * Returns the multiplicative inverse of this instance. If this
     * instance is not invertible an exception is thrown.
     *
     * @return Inverse of this instance.
     *
     * @throws ArithmException If this element is not invertible.
     */
    public abstract PRingElement inv() throws ArithmException;

    /**
     * Returns true if and only if the input represents the same ring
     * element as this instance.
     *
     * @param obj Element compared to this instance.
     * @return true if this instance equals the input and false
     * otherwise.
     */
    public abstract boolean equals(Object obj);


    // Documented in PRingAssociated.java

    public PRing getPRing() {
        return pRing;
    }


    // Implemented in terms of the above.

    /**
     * Returns a raw <b>fixed-size</b> representation of the
     * instance. It should only be used as input to hash functions
     * etc. In particular it should not be used for storing
     * elements. The method is an injective map from the set of ring
     * elements to the set of <code>byte[]</code> of length {@link
     * PRing#getByteLength()}.
     *
     * @return Raw representation of this instance.
     */
    public byte[] toByteArray() {
        return toByteTree().toByteArray();
    }

    /**
     * Multiplies this instance with the scalar, adds the term, and
     * returns the result.
     *
     * @param scalar Integer scalar.
     * @param term Term to add.
     * @return Product of this instance and scalar plus the term.
     */
    public PRingElement mulAdd(PRingElement scalar, PRingElement term) {
        return mul(scalar).add(term);
    }

    /**
     * Returns the difference between this instance and the input.
     *
     * @param term Element subtracted from this instance.
     * @return Difference between this instance and the input.
     */
    public PRingElement sub(PRingElement term) {
        return add(term.neg());
    }

    /**
     * Returns the product of this instance and the inverse of the
     * input. If the input is not invertible an exception is
     * thrown.
     *
     * @param el Divisor.
     * @return This instance divided by the input.
     *
     * @throws ArithmException If input is not invertible.
     */
    public PRingElement div(PRingElement el) throws ArithmException {
        return mul(el.inv());
    }

    /**
     * Frees resources allocated by this instance. Continued use of a
     * freed instance is undefined.
     */
    public void free() {}
}
