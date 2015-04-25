
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
import java.util.*;

import verificatum.crypto.*;
import verificatum.eio.*;

/**
 * Implements an immutable direct power of a field. The elements of
 * this ring are implemented by the class {@link PPRingElement}. The
 * implementation keeps track of structure, i.e., taking products in
 * different orders give different rings. Operations such as addition,
 * multiplication, and exponentiations attempts to interpret an input
 * as belonging to the same ring. If this fails, then the operation is
 * mapped to the subrings. This allows us to view a product element as
 * a container of elements from subrings.
 *
 * @author Douglas Wikstrom
 */
public class PPRing extends PRing {

    /**
     * Underlying rings.
     */
    protected PRing[] pRings;

    /**
     * Fixed size of raw representation of an element of this ring.
     */
    protected int byteLength;

    /**
     * Initializes the zero and unit of this ring.
     */
    protected void init() {
        int len = pRings.length;

        // This exploits the internals of ByteTree.java
        byteLength = 5;
        for (int i = 0; i < pRings.length; i++) {
            byteLength += pRings[i].getByteLength();
        }
    }

    /**
     * Creates the direct power ring from the given rings.
     *
     * @param pRings Underlying rings.
     */
    public PPRing(PRing ... pRings) {
        this.pRings = pRings;
        init();
    }

    /**
     * Creates the direct power ring from the given ring.
     *
     * @param pRing Underlying ring.
     * @param degree Degree of power.
     */
    public PPRing(PRing pRing, int degree) {
        pRings = new PRing[degree];
        Arrays.fill(pRings, pRing);
        init();
    }

    /**
     * Initializes the ring.
     *
     * @param btr Representation of the ring.
     * @param pField Underlying field.
     *
     * @throws ArithmFormatException If the modulus is not a positive
     * prime number.
     */
    private PPRing(ByteTreeReader btr, PField pField)
    throws ArithmFormatException {
        try {
            int siblings = btr.getRemaining();
            pRings = new PRing[siblings];

            for (int i = 0; i < siblings; i++) {
                ByteTreeReader btri = btr.getNextChild();

                if (btri.getRemaining() == 0) {
                    pRings[i] = pField;
                } else {
                    pRings[i] = new PPRing(btri, pField);
                }
            }
            init();
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed PRing!", eioe);
        }
    }

    /**
     * Returns the direct product element of the inputs provided that
     * the result is contained in this ring.
     *
     * @param els Elements we take the product of.
     * @return Direct product element of the inputs.
     */
    public PPRingElement product(PRingElement ... els) {
        if (els.length == pRings.length) {
            for (int i = 0; i < pRings.length; i++) {
                if (!els[i].pRing.equals(pRings[i])) {
                    throw new ArithmError("Incompatible underlying ring!");
                }
            }
            return new PPRingElement(this, els);
        }
        throw new ArithmError("Wrong number of elements!");
    }

    /**
     * Returns the direct power element of the given element.
     *
     * @param el Element array we take the product of.
     * @return Direct product element of the inputs.
     */
    public PPRingElement product(PRingElement el) {
        for (int i = 1; i < pRings.length; i++) {
            if (!pRings[i].equals(pRings[0])) {
                throw new ArithmError("Incompatible underlying rings!");
            }
        }
        PRingElement[] res = new PRingElement[pRings.length];
        Arrays.fill(res, el);
        return new PPRingElement(this, res);
    }

    /**
     * Returns the direct power element array of the input element
     * array provided that the result is contained in this ring.
     *
     * @param el Element array we take the power of.
     * @return Direct product element of the inputs.
     */
    public PPRingElementArray product(PRingElementArray el) {
        for (int i = 1; i < pRings.length; i++) {
            if (!pRings[i].equals(pRings[0])) {
                throw new ArithmError("Incompatible underlying rings!");
            }
        }
        PRingElementArray[] res = new PRingElementArray[pRings.length];
        Arrays.fill(res, el);
        return new PPRingElementArray(this, res);
    }

    /**
     * Returns the projection of this ring to the chosen indices.
     *
     * @param indices Indices of chosen components.
     * @return Projection of this ring to the chosen subring.
     */
    public PRing project(boolean[] indices) {
        if (indices.length != pRings.length) {
            throw new ArithmError("Wrong length!");
        }

        int count = 0;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i]) {
                count++;
            }
        }

        if (count < 1) {

            throw new ArithmError("Empty projection!");

        } if (count == 1) {

            for (int i = 0; i < indices.length; i++) {

                if (indices[i]) {
                    return pRings[i];
                }
            }
            throw new ArithmError("Indices are empty! (this can not happen)");

        } else {

            PRing[] newPRings = new PRing[count];
            for (int i = 0, j = 0; i < pRings.length; i++) {
                if (indices[i]) {
                    newPRings[j++] = pRings[i];
                }
            }
            return new PPRing(newPRings);
        }
    }

    /**
     * Returns the projection of this ring at the given index.
     *
     * @param i Index on which to project
     * @return Ring at the given index.
     */
    public PRing project(int i) {
        return pRings[i];
    }

    /**
     * Returns the factors of this ring.
     *
     * @return Factors of this ring.
     */
    public PRing[] getFactors() {
        return Arrays.copyOfRange(pRings, 0, pRings.length);
    }

    /**
     * Returns the number of subrings wrapped by this ring. This does
     * not necessarily give the dimension of the ring viewed as a
     * direct product of the underlying field.
     *
     * @return Number of subrings.
     */
    public int getWidth() {
        return pRings.length;
    }

    // Documented in PRing.java

    public PField getPField() {
        return pRings[0].getPField();
    }

    public PRingElement getZERO() {
        PRingElement[] ZEROs = new PRingElement[pRings.length];
        for (int i = 0; i < ZEROs.length; i++) {
            ZEROs[i] = pRings[i].getZERO();
        }
        return new PPRingElement(this, ZEROs);
    }

    public PRingElement getONE() {
        PRingElement[] ONEs = new PRingElement[pRings.length];
        for (int i = 0; i < ONEs.length; i++) {
            ONEs[i] = pRings[i].getONE();
        }
        return new PPRingElement(this, ONEs);
    }

    public int getByteLength() {
        return byteLength;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PPRing(");
        sb.append(pRings[0].toString());
        for (int i = 1; i < pRings.length; i++) {
            sb.append(",");
            sb.append(pRings[i].toString());
        }
        sb.append(")");
        return sb.toString();
    }

    public int getEncodeLength() {
        return getPField().getEncodeLength();
    }

    public PPRingElement toElement(ByteTreeReader btr)
        throws ArithmFormatException {
        PRingElement[] elements = new PRingElement[pRings.length];
        try {
            for (int i = 0; i < pRings.length; i++) {
                elements[i] = pRings[i].toElement(btr.getNextChild());
            }
            return new PPRingElement(this, elements);
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed data!", eioe);
        }
    }

    public PPRingElement toElement(byte[] bytes, int offset, int length) {
        PRingElement[] values = new PRingElement[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            values[i] = pRings[i].toElement(bytes, offset, length);
        }
        return new PPRingElement(this, values);
    }

    public PPRingElement randomElement(RandomSource rs, int statDist) {
        PRingElement[] values = new PRingElement[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            values[i] = pRings[i].randomElement(rs, statDist);
        }
        return new PPRingElement(this, values);
    }

    public PPRingElementArray randomElementArray(int size, RandomSource rs,
                                                 int statDist) {
        PRingElementArray[] values = new PRingElementArray[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            values[i] = pRings[i].randomElementArray(size, rs, statDist);
        }
        return new PPRingElementArray(this, values);
    }

    /**
     * Changes the order of the dimensions.
     *
     * @param arrays Array to be decomposed.
     * @return Decomposed array.
     */
    protected PRingElement[][] decompose(PRingElement[] arrays) {
        if (arrays.length == 0) {

            return new PRingElement[0][];

        } else if (equals(arrays[0].pRing)) {

            PRingElement[][] res = new PRingElement[pRings.length][];
            for (int i = 0; i < pRings.length; i++) {
                res[i] = new PRingElement[arrays.length];
            }
            for (int i = 0; i < pRings.length; i++) {
                for (int j = 0; j < arrays.length; j++) {
                    res[i][j] = ((PPRingElement)arrays[j]).values[i];
                }
            }
            return res;
        }
        throw new ArithmError("Can not decompose!");
    }

    public PRingElementArray toElementArray(PRingElement[] elements) {
        PRingElement[][] decomposed = decompose(elements);

        PRingElementArray[] arrays = new PRingElementArray[decomposed.length];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = pRings[i].toElementArray(decomposed[i]);
        }
        return new PPRingElementArray(this, arrays);
    }

    public PRingElementArray toElementArray(int size, PRingElement element) {
        PRingElementArray[] arrays = new PRingElementArray[pRings.length];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] =
                pRings[i].toElementArray(size,
                                         ((PPRingElement)element).values[i]);
        }
        return new PPRingElementArray(this, arrays);
    }

    public PPRingElementArray toElementArray(int size, ByteTreeReader btr)
            throws ArithmFormatException {
        if (btr.getRemaining() != pRings.length) {
            throw new ArithmFormatException("Wrong number of rings!");
        }
        try {
            PRingElementArray[] res = new PRingElementArray[pRings.length];
            for (int i = 0; i < pRings.length; i++) {
                res[i] = pRings[i].toElementArray(size, btr.getNextChild());
            }
            return new PPRingElementArray(this, res);
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed array!", eioe);
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PPRing)) {
            return false;
        }

        PPRing pPRing = (PPRing)obj;

        if (pPRing.pRings.length != pRings.length) {
            return false;
        }

        for (int i = 0; i < pRings.length; i++) {
            if (!pRings[i].equals(pPRing.pRings[i])) {
                return false;
            }
        }
        return true;
    }

    public ByteTree toByteTree() {
        return new ByteTree(getPField().toByteTree(), toByteTreeInner());
    }

    /**
     * Packs the internal structure of this instance, i.e., the tree
     * of products of fields.
     *
     * @return Representation of the structure of this instance.
     */
    protected ByteTree toByteTreeInner() {
        ByteTree[] children = new ByteTree[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            if (pRings[i] instanceof PField) {
                children[i] = new ByteTree(new ByteTree[0]);
            } else {
                children[i] = ((PPRing)pRings[i]).toByteTreeInner();
            }
        }
        return new ByteTree(children);
    }

    public PRing getPRing() {
        return this;
    }
}
