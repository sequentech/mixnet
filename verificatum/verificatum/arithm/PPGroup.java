
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
import java.lang.reflect.*;
import java.util.*;

import verificatum.crypto.*;
import verificatum.eio.*;

/**
 * Class representing a direct product of some {@link
 * PGroup}-instances. This implementation keeps track of the order in
 * which product groups are formed. Operations such as addition,
 * multiplication, and exponentiations attempts to interpret an input
 * as belonging to the same group or the associated ring. If this
 * fails, then the operation is mapped to the subgroups. This allows
 * us to view a product element as a container of elements from
 * subgroups.
 *
 * @author Douglas Wikstrom
 */
public class PPGroup extends PGroup {

    /**
     * Underlying groups.
     */
    protected PGroup[] pGroups;

    /**
     * Fixed number of bytes needed to map an element bijectively to a
     * raw byte[] representation.
     */
    protected int byteLength;

    /**
     * Maximal number of bytes that can be encoded into an element of
     * this group.
     */
    protected int encodeLength;

    /**
     * Constructs an instance corresponding to the input representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Group represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * valid instructions for creating an instance.
     * @see ByteTreeConverter
     */
    public static PGroup newInstance(ByteTreeReader btr, RandomSource rs,
                                     int certainty)
        throws ArithmFormatException {
        try {
            ByteTreeReader tmp = btr.getNextChild();

            // Recover the underlying basic groups.
            PGroup[] bPGroups = new PGroup[tmp.getRemaining()];
            for (int i = 0; i < tmp.getRemaining(); i++) {
                bPGroups[i] =
                    Marshalizer.unmarshalAux_PGroup(tmp.getNextChild(),
                                                    rs,
                                                    certainty);
            }

            LargeInteger characteristic =
                bPGroups[0].getPRing().getCharacteristic();
            for (int i = 1; i < bPGroups.length; i++) {
                if (bPGroups[i].getPRing().getCharacteristic()
                    != characteristic) {
                    String s = "Mismatching characteristics!";
                    throw new ArithmFormatException(s);
                }
            }


            boolean[] touchArray = new boolean[bPGroups.length];
            Arrays.fill(touchArray, false);

            // Recover the structured product group.
            tmp = btr.getNextChild();
            if (tmp.isLeaf()) {
                throw new ArithmFormatException("Malformed structure!");
            }
            PGroup res = newInstanceInner(tmp, bPGroups, touchArray);
            for (int i = 0; i < touchArray.length; i++) {
                if (!touchArray[i]) {
                    throw new ArithmFormatException("Untouched basic group!");
                }
            }
            return res;

        } catch (EIOException eioe) {
            throw new ArithmFormatException("Invalid modulus!", eioe);
        }
    }

    /**
     * Unpacks the structure of this instance.
     *
     * @param btr Representation of structure of groups.
     * @param bPGroups Underlying groups.
     * @param touchArray Array keeping track of which groups are used
     * somewhere.
     * @return Instance corresponding to the given representation.
     *
     * @throws EIOException If the input does not represent an instance.
     * @throws ArithmFormatException If the input does not represent
     * an instance.
     */
    public static PGroup newInstanceInner(ByteTreeReader btr,
                                          PGroup[] bPGroups,
                                          boolean[] touchArray)
        throws EIOException, ArithmFormatException {

        if (btr.isLeaf()) {

            int index = btr.readInt();
            if (0 <= index && index < bPGroups.length) {
                touchArray[index] = true;
                return bPGroups[index];
            } else {
                throw new ArithmFormatException("Invalid index!");
            }

        } else {

            PGroup[] pGroups = new PGroup[btr.getRemaining()];
            for (int i = 0; i < pGroups.length; i++) {
                pGroups[i] =
                    newInstanceInner(btr.getNextChild(), bPGroups, touchArray);
            }
            return new PPGroup(pGroups);

        }
    }


    /**
     * Creates a product group from the given groups.
     *
     * @param pGroups Groups contained in this group.
     */
    public PPGroup(PGroup ... pGroups) {
        super(jointPRing(pGroups));

        this.pGroups = pGroups;

        // This exploits the internals of ByteTree.java
        byteLength = 5;
        for (int i = 0; i < pGroups.length; i++) {
            byteLength += pGroups[i].getByteLength();
        }

        encodeLength = 0;
        for (int i = 0; i < pGroups.length; i++) {
            encodeLength += pGroups[i].getEncodeLength();
        }
    }

    /**
     * Creates a product group from the given group with the given
     * power.
     *
     * @param pGroup Underlying group.
     * @param width Power of product.
     */
    public PPGroup(PGroup pGroup, int width) {
        this(fill(pGroup, width));
    }

    /**
     * Returns the number of underlying groups.
     *
     * @return Number of underlying groups.
     */
    public int getWidth() {
        return pGroups.length;
    }

    /**
     * Creates an array containing the given number of copies of the
     * given group.
     *
     * @param pGroup Underlying group.
     * @param width Power of product.
     * @return Array containing the given number of copies of the
     * group.
     */
    protected static PGroup[] fill(PGroup pGroup, int width) {
        PGroup[] pGroups = new PGroup[width];
        Arrays.fill(pGroups, pGroup);
        return pGroups;
    }

    /**
     * Creates the ring associated with the product of the input
     * groups.
     *
     * @param pGroups Underlying groups.
     * @return Ring associated with this group.
     */
    protected static PRing jointPRing(PGroup ... pGroups) {
        PRing[] pRings = new PRing[pGroups.length];

        LargeInteger order = pGroups[0].getElementOrder();
        for (int i = 0; i < pGroups.length; i++) {
            if (pGroups[i].getElementOrder().equals(order)) {
                pRings[i] = pGroups[i].getPRing();
            } else {
                throw new ArithmError("Groups of different orders!");
            }
        }
        return new PPRing(pRings);
    }

    /**
     * Returns the projection of this element to the subgroup defined
     * by the chosen indices.
     *
     * @param indices Indices of chosen components.
     * @return Projection of this element.
     */
    public PGroup project(boolean[] indices) {
        if (indices.length != pGroups.length) {
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
                    return pGroups[i];
                }
            }
            throw new ArithmError("Indices are empty! (this can not happen)");

        } else {

            PGroup[] newPGroups = new PGroup[count];
            for (int i = 0, j = 0; i < pGroups.length; i++) {
                if (indices[i]) {
                    newPGroups[j++] = pGroups[i];
                }
            }
            return new PPGroup(newPGroups);
        }
    }

    /**
     * Returns the projection of this group at the given index.
     *
     * @param i Index on which to project
     * @return Group at the given index.
     */
    public PGroup project(int i) {
        return pGroups[i];
    }

    /**
     * Returns the factors of this group.
     *
     * @return Factors of this group.
     */
    public PGroup[] getFactors() {
        return Arrays.copyOfRange(pGroups, 0, pGroups.length);
    }

    /**
     * Changes the order of the dimensions.
     *
     * @param arrays Array to be decomposed.
     * @return Decomposed array.
     */
    protected PGroupElement[][] decompose(PGroupElement[] arrays) {
        if (arrays.length == 0) {

            return new PGroupElement[0][];

        } else if (equals(arrays[0].pGroup)) {

            PGroupElement[][] res = new PGroupElement[pGroups.length][];
            for (int i = 0; i < pGroups.length; i++) {
                res[i] = new PGroupElement[arrays.length];
            }
            for (int i = 0; i < pGroups.length; i++) {
                for (int j = 0; j < arrays.length; j++) {
                    res[i][j] = ((PPGroupElement)arrays[j]).values[i];
                }
            }
            return res;
        }
        throw new ArithmError("Can not decompose!");
    }


    /**
     * Changes the order of the dimensions.
     *
     * @param arrays Array to be composed.
     * @return Composed array.
     */
    protected PGroupElement[] compose(PGroupElement[][] arrays) {
        if (arrays.length == pGroups.length) {

            for (int i = 1; i < arrays.length; i++) {
                if (arrays[i].length != arrays[0].length) {
                    throw new ArithmError("Different lengths!");
                }
            }

            PGroupElement[] res = new PGroupElement[arrays[0].length];

            for (int j = 0; j < res.length; j++) {
                PGroupElement[] tmp = new PGroupElement[pGroups.length];
                for (int i = 0; i < pGroups.length; i++) {
                    tmp[i] = arrays[i][j];
                }
                res[j] = new PPGroupElement(this, tmp);
            }
            return res;
        }
        throw new ArithmError("Wrong width!");
    }

    /**
     * Returns the direct product element of the inputs provided that
     * the result is contained in this group.
     *
     * @param els Elements we take the product of.
     * @return Direct product element of the inputs.
     */
    public PPGroupElement product(PGroupElement ... els) {
        if (els.length == pGroups.length) {
            for (int i = 0; i < pGroups.length; i++) {
                if (!els[i].pGroup.equals(pGroups[i])) {
                    throw new ArithmError("Incompatible underlying group!");
                }
            }
            return new PPGroupElement(this, els);
        }
        throw new ArithmError("Wrong number of elements!");
    }

    /**
     * Returns the direct power element array of the input element
     * array provided that the result is contained in this group.
     *
     * @param el Element we take the product of.
     * @return Direct product element of the input.
     */
    public PPGroupElement product(PGroupElement el) {
        for (int i = 1; i < pGroups.length; i++) {
            if (!pGroups[i].equals(pGroups[0])) {
                throw new ArithmError("Incompatible underlying groups!");
            }
        }
        PGroupElement[] res = new PGroupElement[pGroups.length];
        Arrays.fill(res, el);
        return new PPGroupElement(this, res);
    }

    /**
     * Returns the direct product element array of the input element
     * arrays provided that the result is contained in this group.
     *
     * @param els Element arrays we take the product of.
     * @return Direct product element array of the inputs.
     */
    public PPGroupElementArray product(PGroupElementArray ... els) {
        if (els.length == pGroups.length) {
            for (int i = 0; i < pGroups.length; i++) {
                if (!els[i].pGroup.equals(pGroups[i])) {
                    throw new ArithmError("Incompatible underlying group!");
                }
            }
            return new PPGroupElementArray(this, els);
        }
        throw new ArithmError("Wrong number of elements!");
    }

    /**
     * Returns the direct power element array of the input element
     * array provided that the result is contained in this group.
     *
     * @param el Element array we take the product of.
     * @return Direct product element array of the inputs.
     */
    public PPGroupElementArray product(PGroupElementArray el) {
        for (int i = 1; i < pGroups.length; i++) {
            if (!pGroups[i].equals(pGroups[0])) {
                throw new ArithmError("Incompatible underlying groups!");
            }
        }
        PGroupElementArray[] res = new PGroupElementArray[pGroups.length];
        Arrays.fill(res, el);
        return new PPGroupElementArray(this, res);
    }

    /**
     * Computes a list of underlying basic groups.
     *
     * @param pGroup Group to investigate.
     * @param grps List storing the found underlying groups.
     */
    public void findBasicPGroups(PGroup pGroup, ArrayList<PGroup> grps) {
        if (pGroup instanceof PPGroup) {
            PGroup[] pGroups = ((PPGroup)pGroup).pGroups;
            for (int i = 0; i < pGroups.length; i++) {
                findBasicPGroups(pGroups[i], grps);
            }
        } else {
            if (!grps.contains(pGroup)) {
                grps.add(pGroup);
            }
        }
    }

    /**
     * Converts the given values into an element. This does not copy
     * the input array.
     *
     * @param values Values underlying this instance.
     * @return Element constructed from the given values.
     */
    public PPGroupElement toElement(PGroupElement[] values) {
        return new PPGroupElement(this, values);
    }

    /**
     * Computes a representation of the structure of this group.
     *
     * @param pGroup Group to investigate.
     * @param grps List storing the found underlying groups.
     * @return Representation of the structure of this instance.
     */
    protected ByteTreeBasic toByteTreeStructure(PGroup pGroup,
                                                ArrayList<PGroup> grps) {
        if (pGroup instanceof PPGroup) {

            PGroup[] pGroups = ((PPGroup)pGroup).pGroups;
            ByteTreeBasic[] btbs = new ByteTreeBasic[pGroups.length];

            for (int i = 0; i < pGroups.length; i++) {
                btbs[i] = toByteTreeStructure(pGroups[i], grps);
            }
            return new ByteTreeContainer(btbs);

        } else {

            int grpIndex = grps.indexOf(pGroup);
            return ByteTree.intToByteTree(grpIndex);

        }
    }

    public ByteTreeBasic toByteTree() {

        // Find basic groups.
        ArrayList<PGroup> grps = new ArrayList<PGroup>();
        findBasicPGroups(this, grps);

        // Pack the basic groups.
        ByteTreeBasic[] bbts = new ByteTreeBasic[grps.size()];
        int i = 0;
        for (PGroup pGroup : grps) {
            bbts[i++] = Marshalizer.marshal(pGroup);
        }

        // Pack both basic groups and structure.
        return new ByteTreeContainer(new ByteTreeContainer(bbts),
                                     toByteTreeStructure(this, grps));
    }


    // Documented in PGroup.java


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PPGroup(");
        sb.append(pGroups[0].toString());
        for (int i = 1; i < pGroups.length; i++) {
            sb.append(",");
            sb.append(pGroups[i].toString());
        }
        sb.append(")");
        return sb.toString();
    }

    public String humanDescription(boolean verbose) {
        StringBuilder sb = new StringBuilder();
        sb.append("Product(");
        for (int i = 0; i < pGroups.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(pGroups[i].humanDescription(verbose));
        }
        sb.append(")");
        return sb.toString();
    }

    public PPGroupElement getg() {
        PGroupElement[] gs = new PGroupElement[pGroups.length];
        for (int i = 0; i < pGroups.length; i++) {
            gs[i] = pGroups[i].getg();
        }
        return new PPGroupElement(this, gs);
    }

    public PPGroupElement getONE() {
        PGroupElement[] ONEs = new PGroupElement[pGroups.length];
        for (int i = 0; i < pGroups.length; i++) {
            ONEs[i] = pGroups[i].getONE();
        }
        return new PPGroupElement(this, ONEs);
    }

    public int getByteLength() {
        return byteLength;
    }

    public int getEncodeLength() {
        return encodeLength;
    }

    public PPGroupElement toElement(ByteTreeReader btr)
        throws ArithmFormatException {
        if (btr.getRemaining() != pGroups.length) {
            throw new ArithmFormatException("Wrong number of subelements!");
        }
        PGroupElement[] elements = new PGroupElement[pGroups.length];
        try {
            for (int i = 0; i < pGroups.length; i++) {
                elements[i] = pGroups[i].toElement(btr.getNextChild());
            }
            return new PPGroupElement(this, elements);
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed data!", eioe);
        }
    }

    public PPGroupElement encode(byte[] bytes, int startIndex, int length) {

        PGroupElement[] res = new PGroupElement[pGroups.length];

        for (int i = 0; i < pGroups.length; i++) {

            int len = Math.min(pGroups[i].getEncodeLength(), length);
            res[i] = pGroups[i].encode(bytes, startIndex, len);
            startIndex += len;
            length -= len;
        }
        return new PPGroupElement(this, res);
    }

    public PGroupElement randomElement(RandomSource rs, int statDist) {
        PGroupElement[] elements = new PGroupElement[pGroups.length];
        for (int i = 0; i < pGroups.length; i++) {
            elements[i] = pGroups[i].randomElement(rs, statDist);
        }
        return new PPGroupElement(this, elements);
    }

    public PGroupElementArray toElementArray(int size, PGroupElement element) {
        PGroupElementArray[] res = new PGroupElementArray[pGroups.length];

        for (int i = 0; i < pGroups.length; i++) {
            res[i] =
                pGroups[i].toElementArray(size,
                                          ((PPGroupElement)element).values[i]);
        }
        return new PPGroupElementArray(this, res);
    }

    public PPGroupElementArray toElementArray(PGroupElement[] elements) {
        PGroupElement[][] dec = decompose(elements);
        PGroupElementArray[] res = new PGroupElementArray[pGroups.length];
        for (int i = 0; i < pGroups.length; i++) {
            res[i] = pGroups[i].toElementArray(dec[i]);
        }
        return new PPGroupElementArray(this, res);
    }

    public PGroupElementArray toElementArray(PGroupElementArray ... arrays) {

        for (int i = 0; i < arrays.length; i++) {
            if (!equals(arrays[i].getPGroup())) {
                throw new ArithmError("Mismatching groups!");
            }
        }

        PGroupElementArray[] res = new PGroupElementArray[pGroups.length];

        for (int i = 0; i < pGroups.length; i++) {
            PGroupElementArray[] slice = new PGroupElementArray[arrays.length];
            for (int j = 0; j < arrays.length; j++) {
                slice[j] = ((PPGroupElementArray)arrays[j]).values[i];
            }
            res[i] = pGroups[i].toElementArray(slice);
        }

        return new PPGroupElementArray(this, res);
    }

    public PPGroupElementArray toElementArray(int size, ByteTreeReader btr)
        throws ArithmFormatException {

        if (btr.getRemaining() != pGroups.length) {
            throw new ArithmFormatException("Wrong number of groups! (" +
                                            btr.getRemaining() +
                                            " instead of " + pGroups.length +
                                            ")");
        }
        try {
            PGroupElementArray[] res = new PGroupElementArray[pGroups.length];
            for (int i = 0; i < pGroups.length; i++) {
                res[i] = pGroups[i].toElementArray(size, btr.getNextChild());
            }
            return new PPGroupElementArray(this, res);
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed array!", eioe);
        }
    }

    public PPGroupElementArray randomElementArray(int size,
                                                  RandomSource rs,
                                                  int statDist) {
        PGroupElementArray[] res = new PGroupElementArray[pGroups.length];

        for (int i = 0; i < pGroups.length; i++) {
            res[i] = pGroups[i].randomElementArray(size, rs, statDist);
        }
        return new PPGroupElementArray(this, res);
    }

    public PPGroupElement expProd(PGroupElement[] bases,
                                  PRingElement[] exponents) {
        if (bases.length != exponents.length) {
            throw new ArithmError("Different lengths!");
        }
        PGroupElement[][] decBases = decompose(bases);
        PRingElement[][] decExponents = null;

        if (bases[0].pGroup.pRing.equals(exponents[0].pRing)) {
            decExponents = ((PPRing)pRing).decompose(exponents);
        }

        PGroupElement[] res = new PGroupElement[pGroups.length];

        if (decExponents != null) {
            for (int i = 0; i < pGroups.length; i++) {
                res[i] = pGroups[i].expProd(decBases[i], decExponents[i]);
            }
        } else {
            for (int i = 0; i < pGroups.length; i++) {
                res[i] = pGroups[i].expProd(decBases[i], exponents);
            }
        }
        return new PPGroupElement(this, res);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PPGroup)) {
            return false;
        }

        PPGroup pPGroup = (PPGroup)obj;

        if (pPGroup.pGroups.length != pGroups.length) {
            return false;
        }

        for (int i = 0; i < pGroups.length; i++) {
            if (!pPGroup.pGroups[i].equals(pGroups[i])) {
                return false;
            }
        }
        return true;
    }
}
