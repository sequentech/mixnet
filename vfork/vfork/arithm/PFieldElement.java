
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
 * Implements an immutable element of a prime order field. The field
 * is implemented by {@link PField}.
 *
 * @author Douglas Wikstrom
 */
public class PFieldElement extends PRingElement {

    /**
     * Internal canonically reduced representation of this field element.
     */
    LargeInteger value;

    /**
     * Creates an instance.
     *
     * <p>
     *
     * WARNING! This constructor assumes that the integer
     * representative is already canonically reduced.
     *
     * @param pField Field to which the resulting element should
     * belong.
     * @param value Representative of the created field element.
     */
    public PFieldElement(PField pField, LargeInteger value) {
        super(pField);
        this.value = value;
    }

    /**
     * Creates an instance from the input representation.
     *
     * @param pField Field to which the resulting element should
     * belong.
     * @param btr A representation of an instance.
     *
     * @throws ArithmFormatException If the input does not represent a
     * field element.
     */
    protected PFieldElement(PField pField, ByteTreeReader btr)
        throws ArithmFormatException {
        super(pField);
        if (btr.getRemaining() != pField.orderByteLength) {
            throw new ArithmFormatException("Incorrect length of data!");
        }
        value = new LargeInteger(pField.orderByteLength, btr);
        if (value.compareTo(LargeInteger.ZERO) < 0
            || pField.order.compareTo(value) <= 0) {
            throw new ArithmFormatException("Non-canonical representative!");
        }
    }

    /**
     * Returns the field to which this instance belongs.
     *
     * @return Field to which this instance belongs.
     */
    public PField getPField() {
        return (PField)pRing;
    }

    /**
     * Returns this instance expressed by its unique non-negative
     * representatitive smaller than the modulus of the field to which
     * it belongs.
     *
     * @return Canonical integer representative of this instance.
     */
    public LargeInteger toLargeInteger() {
        return value;
    }


    // Documented in PRingElement.java

    public String toString() {
        return value.toString(16);
    }

    public PFieldElement add(PRingElement el) {
        if (pRing.equals(el.pRing)) {
            return new PFieldElement((PField)pRing,
                                     value.add(((PFieldElement)el).value).
                                     mod(((PField)pRing).order));
        }
        throw new ArithmError("Mismatching fields!");
    }

    public PFieldElement neg() {
        return new PFieldElement((PField)pRing,
                                 value.neg().mod(((PField)pRing).order));
    }

    public PFieldElement mul(PRingElement el) {
        if (pRing.equals(el.pRing)) {
            return new PFieldElement((PField)pRing,
                                     value.mul(((PFieldElement)el).value).
                                     mod(((PField)pRing).order));
        }
        throw new ArithmError("Mismatching fields!");
    }

    public PFieldElement inv() throws ArithmException {
        try {
            return new PFieldElement((PField)pRing,
                                     value.modInv(((PField)pRing).order));
        } catch (ArithmException ae) {
            throw new ArithmException("Zero element is not invertible!", ae);
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PFieldElement) {
            PFieldElement el = (PFieldElement)obj;
            return value.compareTo(el.value) == 0 && pRing.equals(el.pRing);
        }
        return false;
    }

    // Documented in ByteTreeConvertible.java

    public byte[] toByteArray() {
        byte[] temp = value.toByteArray();
        byte[] data = new byte[((PField)pRing).orderByteLength];

        int offset = data.length - temp.length;
        Arrays.fill(data, 0, offset, (byte)0);
        System.arraycopy(temp, 0, data, offset, temp.length);

        return data;
    }

    public ByteTree toByteTree() {
        return new ByteTree(toByteArray());
    }
}
