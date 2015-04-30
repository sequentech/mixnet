
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
import vfork.util.*;


/**
 * Element belonging to a group represented by {@link APGroup}.
 *
 * @author Douglas Wikstrom
 */
public class APGroupElement extends BPGroupElement {

    /**
     * Underlying array of elements.
     */
    protected PGroupElementArray value;

    /**
     * Creates an instance.
     *
     * @param pGroup Group to which the resulting element should
     * belong.
     * @param value Underlying array of elements.
     */
    protected APGroupElement(PGroup pGroup, PGroupElementArray value) {
        super(pGroup);
        this.value = value;
    }

    /**
     * Returns the underlying array of group elements.
     *
     * @return Underlying array of group elements.
     */
    public PGroupElementArray getContent() {
        return value;
    }

    // Documented in PGroupElement.java

    public String toString() {
        return value.toString();
    }

    public int decode(byte[] array, int startIndex) {
        return value.get(0).decode(array, startIndex);
    }

    public PGroupElement mul(PGroupElement el) {
        return new APGroupElement(pGroup,
                                  value.mul(((APGroupElement)el).value));
    }

    public PGroupElement inv() {
        return new APGroupElement(pGroup, value.inv());
    }

    public PGroupElement exp(PRingElement exponent) {
        if (exponent instanceof APRingElement) {
            PRingElementArray exponents = ((APRingElement)exponent).value;
            return new APGroupElement(pGroup, value.exp(exponents));
        } else {
            return new APGroupElement(pGroup, value.exp(exponent));
        }
    }

    public int compareTo(PGroupElement obj) {
        APGroupElement el = (APGroupElement)obj;
        return value.compareTo(el.value);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof APGroupElement)) {
            return false;
        }
        return value.equals(((APGroupElement)obj).value);
    }

    public void free() {
        value.free();
        value = null;
    }

    // Documented in ByteTreeConvertible.java

    public ByteTreeBasic toByteTree() {
        return value.toByteTree();
    }
}
