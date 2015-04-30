




/*
 * Copyright 2011 Niko Farhan
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

import java.util.Arrays;
import java.math.BigInteger;

import vfork.eio.ByteTree;
import vfork.eio.ByteTreeReader;
import vfork.eio.EIOException;
import vfork.util.*;

// Enabled calls to native code begins here.

import jecn.ECN;

// Enabled calls to native code ends here

/**
 * An implementation of a group element belonging to an instance of
 * {@link JECPGroup}.
 *
 * @author Niko Farhan
 * @author Douglas Wikström
 */
public class JECPGroupElement extends BPGroupElement {

    /**
     * Will be used for the "infinity" element of the group
     */
    private static final LargeInteger MINUS_ONE = new LargeInteger(-1);

    /**
     * The x coordinate of this point.
     */
    protected LargeInteger x;

    /**
     * The y coordinate of this point.
     */
    protected LargeInteger y;

    /**
     * Constructs a new group element from its coordinates.
     *
     * @param pGroup Group which contains this element.
     * @param x The x coordinate.
     * @param y The y coordinate
     * @throws ArithmFormatException If the input does not represent a
     * point.
     */
    public JECPGroupElement(JECPGroup pGroup, LargeInteger x, LargeInteger y)
        throws ArithmFormatException {
        super(pGroup);
        if (!pGroup.isPointOnCurve(x, y)) {
            String s =
                String.format("Given point is not on the described " +
                              "curve! (x=%s y=%s)",
                              x.value.toString(),
                              y.value.toString());

            throw new ArithmFormatException(s);
        }
        unsafeInit(x, y);
    }

    /**
     * Creates an element of the group from the given representation.
     *
     * @param pGroup Group to which the created element belongs.
     * @param btr Representation of a group element.
     * @throws ArithmFormatException If the input does not represent
     * an element.
     */
    protected JECPGroupElement(JECPGroup pGroup, ByteTreeReader btr)
        throws ArithmFormatException {
        super(pGroup);
        try {

            int fieldByteLength = pGroup.field.getByteLength();

            ByteTreeReader btrx = btr.getNextChild();
            if (btrx.getRemaining() != fieldByteLength) {
                throw new ArithmFormatException("Wrong byte length!");
            }
            LargeInteger x = new LargeInteger(btrx);

            ByteTreeReader btry = btr.getNextChild();
            if (btry.getRemaining() != fieldByteLength) {
                throw new ArithmFormatException("Wrong byte length!");
            }
            LargeInteger y = new LargeInteger(btry);

            if (!((JECPGroup)pGroup).isPointOnCurve(x, y)) {
                throw new ArithmFormatException("Point is not on curve!");
            }
            unsafeInit(x, y);

        } catch (EIOException eioe) {
            throw new ArithmFormatException("Invalid byte tree format", eioe);
        }
    }

    // public JECPGroupElement(JECPGroup pGroup, byte[] data)
    //     throws ArithmException {
    //     super(pGroup);

    //     if (data.length != pGroup.getByteLength()) {
    //         throw new ArithmException("Invalid number of bytes");
    //     }
    //     int i = 0;
    //     while (i < data.length && data[i] == (byte)0xFF) {
    //         i++;
    //     }
    //     if (i == data.length) {
    //         unsafeInit(MINUS_ONE, MINUS_ONE);
    //         return;
    //     }
    //     byte[] xb = Arrays.copyOfRange(data, 0, data.length/2);
    //     byte[] yb = Arrays.copyOfRange(data, data.length/2, data.length);
    //     BigInteger x = new BigInteger(1, xb);
    //     BigInteger y = new BigInteger(1, yb);
    //     unsafeInit(new LargeInteger(x), new LargeInteger(y));
    // }

    /**
     * Initializes the element without verifing correctness of the
     * parameters.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    private void unsafeInit(LargeInteger x, LargeInteger y) {
        this.x = x;
        this.y = y;
    }


    // Implemented in PGroupElement.java.

    @Override
    public String toString() {
        if (x.equals(MINUS_ONE)) {
            return "(INFINITY)";
        }
        return String.format("(%s, %s)", x.toString(), y.toString());
    }

    @Override
    public int decode(byte[] array, int startIndex) {

        // Unit encodes nothing.
        if (x.equals(MINUS_ONE)) {
            return 0;
        }

        // Ignore padding.
        LargeInteger encoded = getX().shiftRight(16);

        // Extract length.
        int length =
            encoded.value.and(BigInteger.valueOf(0xFFFFFFFFL)).intValue();

        // If the length is invalid, then this element encodes
        // nothing.
        if (length < 0 || pGroup.getEncodeLength() <= length) {
            return 0;
        }

        // Remove length from data.
        encoded = encoded.shiftRight(32);

        // Copy the given amount of data to the destination array.
        byte[] data = encoded.toByteArray();

        System.arraycopy(data, data.length - length,
                         array, startIndex,
                         length);
        return length;
    }

    @Override
    public PGroupElement mul(PGroupElement el) {
        if (!(el instanceof JECPGroupElement)) {
            throw new ArithmError("Type mismatch. Received " +
                                  el.getClass().getCanonicalName());
        }
        JECPGroupElement e = (JECPGroupElement) el;

        // If this instance is the unit element, then we return the
        // input.
        if (x.equals(MINUS_ONE)) {
            return e;
        }

        // If the input is the unit element, then we return this
        // instance.
        if (e.x.equals(MINUS_ONE)) {
            return this;
        }

        // If the input is the inverse of this element, then we return
        // the unit element.
        if (x.equals(e.x) && y.add(e.y).equals(getModulus())) {
            return getPGroup().getONE();
        }

        // If the input is equal to this element, then we square this
        // instance.
        if (this.equals(e)) {
            return square();
        }

        // Otherwise we perform multiplication of two points in
        // general position.
        LargeInteger s;
        try {

            // s = (y-e.y)/(x-e.x)
            s = this.y.sub(e.y).
                mul(this.x.sub(e.x).modInv(getModulus())).mod(getModulus());

            // rx = s^2 - (x + e.x)
            LargeInteger rx = s.mul(s).sub(this.x).sub(e.x).mod(getModulus());

            // ry = -y - s(rx - x)
            LargeInteger ry =
                this.y.neg().sub(s.mul(rx.sub(this.x))).mod(getModulus());

            return new JECPGroupElement(getPGroup(), rx, ry);

        } catch (ArithmException ae) {
            throw new ArithmError("Unexpected exception while inverting!", ae);
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Unexpected exception while inverting!", afe);
        }
    }

    @Override
    public PGroupElement inv() {
        try {

            // If this is the unit element, then we return this
            // element.
            if (x.equals(MINUS_ONE)) {
                return this;
            }

            // If this element equals its inverse, then we return this
            // element.
            if (y.equals(LargeInteger.ZERO)) {
                return this;
            }

            // Otherwise we mirror along the y-axis.
            return new JECPGroupElement(getPGroup(), x,
                                        y.neg().mod(getModulus()));

        } catch (ArithmFormatException afe) {
            throw new ArithmError("Result not on curve!", afe);
        }
    }

    // This method is used directly if we are compiled as pure java
    // code and otherwise we keep it for testing purposes under the
    // name "naiveExp".

// Removed pure java code here.
// Enabled calls to native code begins here.
    public PGroupElement naiveExp(PRingElement exponent) {
// Enabled calls to native code ends here

        LargeInteger e = ((PFieldElement)exponent).toLargeInteger();

        PGroupElement res = getPGroup().getONE();

        for (int i = e.bitLength(); i >= 0; i--) {
            res = res.mul(res);
            if (e.testBit(i)) {
                res = mul(res);
            }
        }
        return res;
    }

// Enabled calls to native code begins here.
    public PGroupElement exp(PRingElement exponent) {

        byte[] exponenta =
            ((PFieldElement)exponent).toLargeInteger().toByteArray();

        byte[] xa = x.toByteArray();
        byte[] ya = y.toByteArray();

        JECPGroup jECPGroup = (JECPGroup)pGroup;

        byte[][] res = ECN.exp(jECPGroup.fieldOrdera,
                               jECPGroup.aa,
                               jECPGroup.ba,
                               xa,
                               ya,
                               exponenta);

        try {
            return new JECPGroupElement(jECPGroup,
                                        new LargeInteger(res[0]),
                                        new LargeInteger(res[1]));
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Unable to create elliptic curve point!",
                                  afe);
        }
    }

    public PGroupElement[] exp(final PRingElement[] exponents) {

        // Extract integers and determine the maximal bit length.
        int bitLength = 0;
        final LargeInteger[] integers = new LargeInteger[exponents.length];
        for (int i = 0; i < exponents.length; i++) {
            integers[i] = ((PFieldElement)exponents[i]).toLargeInteger();
            bitLength = Math.max(bitLength, integers[i].bitLength());
        }

        final JECPGroup jECPGroup = (JECPGroup)pGroup;

        // Perform precomputation.
        final long tablePtr =
            ECN.fexp_precompute(jECPGroup.fieldOrdera,
                                jECPGroup.aa,
                                jECPGroup.ba,
                                x.toByteArray(), y.toByteArray(),
                                bitLength, exponents.length);

        // Compute result.
        final PGroupElement[] res = new PGroupElement[integers.length];

        ArrayWorker worker =
            new ArrayWorker(res.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {

                        byte[][] ress =
                            ECN.fexp(tablePtr, integers[i].toByteArray());
                        try {
                            res[i] =
                                new JECPGroupElement(jECPGroup,
                                                     new LargeInteger(ress[0]),
                                                     new LargeInteger(ress[1]));
                        } catch (ArithmFormatException afe) {
                            throw new ArithmError("Failed to create element!",
                                                  afe);
                        }
                    }
                }
            };
        worker.work(pGroup.expThreadThreshold);

        ECN.fexp_clear(tablePtr);

        return res;
    }
// Enabled calls to native code ends here

    @Override
    public int compareTo(PGroupElement other) {
        JECPGroupElement jother = (JECPGroupElement)other;
        int cmp = x.compareTo(jother.x);
        if (cmp != 0) {
            return cmp;
        }
        return y.compareTo(jother.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JECPGroupElement)) {
            return false;
        }

        JECPGroupElement other = (JECPGroupElement) obj;

        return x.equals(other.x) && y.equals(other.y);
    }

    /**
     * Represents the input integer in two's complement with fixed
     * size.
     *
     * @param len Fixed length.
     * @param x Integer to be represented.
     * @return Representation of input integer.
     */
    protected byte[] innerToByteArray(int len, LargeInteger x) {

        byte[] res = new byte[len];

        if (x.equals(MINUS_ONE)) {
            Arrays.fill(res, (byte)0xFF);
            res[len - 1] = (byte)0xFE;
        } else {
            byte[] tmp = x.toByteArray();
            System.arraycopy(tmp, 0, res, res.length - tmp.length, tmp.length);
        }
        return res;
    }

    public byte[] toByteArray() {

        PField field = ((JECPGroup)pGroup).field;

        // We add one byte and use point compression.
        byte[] res = innerToByteArray(field.getByteLength() + 1, x);

        if (!x.equals(MINUS_ONE)) {

            if (y.neg().compareTo(y) < 0) {
                res[0] = 1;
            }
        }
        return res;
    }

    @Override
    public ByteTree toByteTree() {

        PField field = ((JECPGroup)pGroup).field;
        int byteLength = field.getByteLength();

        return new ByteTree(new ByteTree(innerToByteArray(byteLength, x)),
                            new ByteTree(innerToByteArray(byteLength, y)));
    }

    // Helper functions from here on.

    @Override
    public JECPGroup getPGroup() {
        return (JECPGroup) super.getPGroup();
    }

    private LargeInteger getModulus() {
        return getPGroup().getModulusField().getOrder();
    }

    private LargeInteger getGroupOrder() {
        return getPGroup().getElementOrder();
    }

    // @Override
    // public int hashCode() {
    //     final int prime = 31;
    //     int result = 1;
    //     result = prime * result + ((x == null) ? 0 : x.hashCode());
    //     result = prime * result + ((y == null) ? 0 : y.hashCode());
    //     return result;
    // }


    /**
     * Doubling of a point on the curve. Since we are using
     * multiplicative notation throughout this is called squaring
     * here.
     *
     * @return Square of this element.
     */
    public PGroupElement square() {

        // If this element is the unit element, then we return the
        // unit element.
        if (x.equals(MINUS_ONE)) {
            return getPGroup().getONE();
        }

        // If this element equals its inverse then we return the unit
        // element.
        if (y.equals(LargeInteger.ZERO)) {
            return getPGroup().getONE();
        }

        try {

            // s = (3x^2 + a) / 2y
            LargeInteger THREE = new LargeInteger(BigInteger.valueOf(3));
            LargeInteger s = x.mul(x).mod(getModulus());
            s = THREE.mul(s).mod(getModulus());
            s = s.add(getPGroup().getA()).mod(getModulus());

            LargeInteger tmp = y.add(y).modInv(getModulus());
            s = s.mul(tmp).mod(getModulus());

            // rx = s^2 - 2x
            LargeInteger rx = s.mul(s).mod(getModulus());
            rx = rx.sub(x.add(x)).mod(getModulus());

            // ry = s(x - rx) - y
            LargeInteger ry = s.mul(x.sub(rx)).sub(y).mod(getModulus());

            return new JECPGroupElement(getPGroup(), rx, ry);

        } catch (ArithmException ae) {
            throw new ArithmError("Failed to invert!", ae);
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Result not on curve?", afe);
        }
    }

    /**
     * Creates the unit element.
     *
     * @param Group of which the unit element is created.
     * @return Unit element of the given group.
     */
    protected static JECPGroupElement one(JECPGroup group) {
        try {
            return new JECPGroupElement(group, MINUS_ONE, MINUS_ONE);
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Unable to create unit element!");
        }
    }

    /**
     * Checks whether the input represents the unit element in the
     * group.
     *
     * @param x The x-coordinate of a point.
     * @param y The y-coordinate of a point.
     * @return True or false depending on the input represents the
     * unit element or not.
     */
    protected static boolean isUnity(LargeInteger x, LargeInteger y) {
        return x.equals(MINUS_ONE) && y.equals(MINUS_ONE);
    }

    /**
     * Returns the x coordinate of this element.
     *
     * @return The x coordinate of this element.
     */
    public LargeInteger getX() {
        return x;
    }

    /**
     * Returns the y coordinate of this element.
     *
     * @return The y coordinate of this element.
     */
    public LargeInteger getY() {
        return y;
    }
}
