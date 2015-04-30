
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
import java.lang.reflect.*;
import java.util.*;

import vfork.crypto.*;
import vfork.eio.*;
import vfork.ui.*;
import vfork.util.*;

/**
 * Power of a group. Each element of this group is a wrapper of a
 * {@link PGroupElementArray} over some other underlying group.
 *
 * @author Douglas Wikstrom
 */
public class APGroup extends BPGroup {

    /**
     * Underlying group.
     */
    protected PGroup pGroup;

    /**
     * Power of product.
     */
    protected int size;

    /**
     * Number of bytes needed to injectively map an element of this
     * group to a raw byte[] representation.
     */
    protected int byteLength;

    /**
     * Creates a group with the given associated group.
     *
     * @param size Dimension.
     * @param pGroup Underlying group.
     */
    public APGroup(PGroup pGroup, int size) {
        super(new APRing(pGroup.pRing, size));
        this.size = size;

        // The order of these two lines is important for call to
        // getONE() in getByteLength().
        this.pGroup = pGroup;

        // This exploits the internals of ByteTree.java
        byteLength = 5 + size * pGroup.getByteLength();
    }

    /**
     * Returns the underlying group.
     *
     * @return Underlying group.
     */
    public PGroup getContentPGroup() {
        return pGroup;
    }

    /**
     * Returns the power used to construct this group.
     *
     * @return Power used to construct this group.
     */
    public int size() {
        return size;
    }

    /**
     * Creates an element initialized with the given element from the
     * underlying group.
     *
     * @param element Initialization value.
     * @return Element initialized with the given value.
     */
    public APGroupElement toElement(PGroupElement element) {
        return toElement(pGroup.toElementArray(size, element));
    }

    /**
     * Creates an element from the given array of elements.
     *
     * @param array Underlying array of group elements.
     * @return Element in this group.
     */
    public APGroupElement toElement(PGroupElementArray array) {
        if (array.getPGroup().equals(pGroup) && array.size() == size) {
            return new APGroupElement(this, array);
        }

        throw new ArithmError("Mismatching parameters!");
    }

    // Documented in PGroup.java

    public String toString() {
        return pGroup.toString() + "^" + size;
    }

    public PGroupElement getg() {
        return new APGroupElement(this,
                                  pGroup.toElementArray(size, pGroup.getg()));
    }

    public PGroupElement getONE() {
        return new APGroupElement(this,
                                  pGroup.toElementArray(size, pGroup.getONE()));
    }

    public int getByteLength() {
        return byteLength;
    }

    public int getEncodeLength() {
        return pGroup.getEncodeLength();
    }

    public PGroupElement toElement(ByteTreeReader btr)
            throws ArithmFormatException {
        PGroupElementArray array = pGroup.toElementArray(size, btr);
        return new APGroupElement(this, array);
    }

    public PGroupElement encode(byte[] bytes,
                                int startIndex,
                                int length) {
        PGroupElement el = pGroup.encode(bytes, startIndex, length);
        return new APGroupElement(this, pGroup.toElementArray(size, el));
    }

    public PGroupElement randomElement(RandomSource rs, int statDist) {
        return new APGroupElement(this,
                                  pGroup.randomElementArray(size,
                                                            rs,
                                                            statDist));
    }

    public PGroupElementArray randomElementArray(int size,
                                                 RandomSource rs,
                                                 int statDist) {
        PGroupElement[] res = new PGroupElement[size];

        for (int i = 0; i < size; i++) {
            res[i] = new APGroupElement(this,
                                        pGroup.randomElementArray(this.size,
                                                                  rs,
                                                                  statDist));
        }
        return toElementArray(res);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof APGroup)) {
            return false;
        }
        APGroup aPGroup = (APGroup)obj;
        return size == aPGroup.size && pGroup.equals(aPGroup.pGroup);
    }


    // Documented in Marshalizable.java

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + pGroup.humanDescription(verbose) + "^" + size + ")";
    }

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(ByteTree.intToByteTree(size),
                                     pGroup.toByteTree());
    }
}
