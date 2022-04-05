
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

import mixnet.crypto.*;
import mixnet.eio.*;

/**
 * This class can be used to wrap {@link PRingElementArray} instances
 * and then treat them as ordinary {@link PRingElement} instances in
 * the corresponding product ring, i.e., it allows us to treat arrays
 * of elements as elements.
 *
 * @author Douglas Wikstrom
 */
public class APRing extends PRing {

    /**
     * Underlying ring.
     */
    protected PRing pRing;

    /**
     * Power of product.
     */
    protected int size;

    /**
     * Number of bytes needed to represent an element of this ring.
     */
    protected int byteLength;

    /**
     * Creates an instance with the given dimension and underlying
     * ring.
     *
     * @param pRing Underlying ring.
     * @param size Dimension of underlying ring.
     */
    public APRing(PRing pRing, int size) {
        this.size = size;

        // The order of these two lines is important for call to
        // getZERO().
        this.pRing = pRing;

        // This exploits the internals of ByteTree.java
        byteLength = 5 + size * pRing.getByteLength();
    }

    /**
     * Returns the underlying ring.
     *
     * @return Underlying ring.
     */
    public PRing getContentPRing() {
        return pRing;
    }

    /**
     * Returns the power used to construct this ring.
     *
     * @return Power used to construct this ring.
     */
    public int size() {
        return size;
    }

    /**
     * Creates an element from the given array of elements.
     *
     * @param array Underlying array of ring elements.
     * @return Element in this ring.
     */
    public APRingElement toElement(PRingElementArray array) {
        if (array.getPRing().equals(pRing) && array.size() == size) {
            return new APRingElement(this, array);
        }
        throw new ArithmError("Mismatching parameters!");
    }

    // Documented in PRing.java

    public PField getPField() {
        return pRing.getPField();
    }

    public PRingElement getZERO() {
        return new APRingElement(this,
                                 pRing.toElementArray(size, pRing.getZERO()));
    }

    public PRingElement getONE() {
        return new APRingElement(this,
                                 pRing.toElementArray(size, pRing.getONE()));
    }

    public int getByteLength() {
        if (byteLength == 0) {
            byteLength = getZERO().toByteArray().length;
        }
        return byteLength;
    }

    public String toString() {
        return pRing.toString() + "^" + size;
    }

    public int getEncodeLength() {
        return pRing.getEncodeLength();
    }

    public PRingElement toElement(ByteTreeReader btr)
        throws ArithmFormatException {
        PRingElementArray array = pRing.toElementArray(size, btr);
        return new APRingElement(this, array);
    }

    public PRingElement toElement(byte[] bytes, int offset, int length) {
        PRingElement encoded = pRing.toElement(bytes, offset, length);
        return new APRingElement(this, pRing.toElementArray(size, encoded));
    }

    public PRingElement randomElement(RandomSource rs, int statDist) {
        return new APRingElement(this,
                                 pRing.randomElementArray(size, rs, statDist));
    }

    public PRingElementArray randomElementArray(int size,
                                                RandomSource rs,
                                                int statDist) {
        return new APRingElementArray(this, size, rs, statDist);
    }

    public PRingElementArray toElementArray(PRingElement[] elements) {
        return new APRingElementArray(this, elements);
    }

    public PRingElementArray toElementArray(int size, PRingElement element) {
        return new APRingElementArray(this, size, element);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof APRing)) {
            return false;
        }
        APRing aPRing = (APRing)obj;
        return size == aPRing.size && pRing.equals(aPRing.pRing);
    }

    public PRingElementArray toElementArray(int size, ByteTreeReader btr)
            throws ArithmFormatException {
        return new APRingElementArray(this, size, btr);
    }

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(ByteTree.intToByteTree(size),
                                     pRing.toByteTree());
    }
}
