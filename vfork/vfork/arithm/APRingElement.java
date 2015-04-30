
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
 * An immutable element of an {@link APRing} instance, where each
 * element is a wrapper of a {@link PRingElementArray} instance.
 *
 * @author Douglas Wikstrom
 */
public class APRingElement extends PRingElement {

    /**
     * Underlying array of elements.
     */
    protected PRingElementArray value;

    /**
     * Creates an instance.
     *
     * @param pRing Ring to which the resulting element should
     * belong.
     * @param value Underlying array of elements.
     */
    protected APRingElement(PRing pRing, PRingElementArray value) {
        super(pRing);
        this.value = value;
    }

    /**
     * Returns the underlying array of ring elements.
     *
     * @return Underlying array of ring elements.
     */
    public PRingElementArray getContent() {
        return value;
    }

    // Documented in PRingElement.java

    public String toString() {
        return value.toString();
    }

    public ByteTreeBasic toByteTree() {
        return value.toByteTree();
    }

    /**
     * Adds the input to this instance. If the input belongs to the
     * same ring as this instance, then the sum is defined in the
     * natural way and otherwise an attempt is made to add the input
     * to the components of this instance.
     *
     * @param el Element to add to this instance.
     * @return Sum of this instance and the input.
     */
    public PRingElement add(PRingElement term) {
        return new APRingElement(pRing,
                                 value.add(((APRingElement)term).value));
    }

    public PRingElement neg() {
        return new APRingElement(pRing, value.neg());
    }

    /**
     * Multiplies the input with this instance. If the input belongs
     * to the same ring as this instance, then the product is defined
     * in the natural way and otherwise an attempt is made to multiply
     * the input with the components of this instance.
     *
     * @param el Element to multiply with this instance.
     * @return Product of this instance and the input.
     */
    public PRingElement mul(PRingElement factor) {
        if (factor instanceof APRingElement) {
            return new APRingElement(pRing,
                                     value.mul(((APRingElement)factor).value));
        } else {
            return new APRingElement(pRing, value.mul(factor));
        }
    }

    public PRingElement inv() throws ArithmException {
        return new APRingElement(pRing, value.inv());
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof APRingElement)) {
            return false;
        }
        APRingElement el = (APRingElement)obj;

        return value.equals(el.value);
    }

    public void free() {
        value.free();
        value = null;
    }
}
