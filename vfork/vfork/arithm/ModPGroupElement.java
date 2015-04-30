
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

/**
 * Implements a group element belonging to a {@link ModPGroup}
 * instance.
 *
 * @author Douglas Wikstrom
 */
public class ModPGroupElement extends BPGroupElement {

    /**
     * Value of this instance.
     */
    protected LargeInteger value;

    /**
     * Creates an element extracted from the input. The input integer
     * must be canonically reduced.
     *
     * @param pGroup Group to which the instance belongs.
     * @param value An integer from which the element is constructed.
     */
    public ModPGroupElement(PGroup pGroup, LargeInteger value) {
        super(pGroup);
        this.value = value;
    }

    /**
     * Creates a <code>PGroupElement</code> instance from its byte
     * tree representation.
     *
     * @param pGroup Group to which the instance belongs.
     * @param btr A representation of an instance.
     * @throws ArithmFormatException If the input does not represent
     * an element in the given group.
     */
    protected ModPGroupElement(ModPGroup pGroup, ByteTreeReader btr)
        throws ArithmFormatException {
        super(pGroup);

        if (btr.getRemaining() != ((ModPGroup)pGroup).modulusByteLength) {
            throw new ArithmFormatException("Incorrect length of data!");
        }
        value = new LargeInteger(((ModPGroup)pGroup).modulusByteLength, btr);

        if (!pGroup.contains(value)) {
            throw new ArithmFormatException("Not a group element!");
        }
    }

    /**
     * Encodes a part of an arbitrary <code>byte[]</code> as an
     * element in the group. The input is truncated if it is longer
     * than {@link PGroup#getEncodeLength()} bytes. The resulting
     * element can be decoded again using {@link
     * PGroupElement#decode(byte[],int)}.
     *
     * @param pGroup Group to which the resulting element belongs.
     * @param byteArray Bytes to be encoded.
     * @param startIndex Starting index.
     * @param length Number of bytes to encode.
     */
    public ModPGroupElement(ModPGroup pGroup,
                            byte[] byteArray,
                            int startIndex,
                            int length) {
        super(pGroup);

        // Make sure that we never use more than the allowed number of
        // bytes.
        int len = Math.min(length, pGroup.getEncodeLength());

        if (pGroup.encoding == ModPGroup.RO_ENCODING) {

            // Encode length in two bits.
            Hashfunction hf = new HashfunctionHeuristic("SHA-256");

            PGroupElement el = pGroup.getONE();

            // We repeatedly hash until we hit the message we intend
            // to encode. We could build a table for this...
            value = null;
            while (value == null) {

                el = el.mul(pGroup.getg());
                byte[] digest = hf.hash(el.toByteTree().toByteArray());

                if ((digest[0] & (byte)0x03) == len) {

                    value = ((ModPGroupElement)el).value;

                    for (int i = 1, j = startIndex; i <= len; i++, j++) {
                        if (digest[i] != byteArray[j]) {
                            value = null;
                            break;
                        }
                    }
                }
            }

        } else {

            // Make room for an array with an int-prefix that says how
            // many bytes are encoded.
            int noBytesToUse = pGroup.getEncodeLength() + 4;
            byte[] bytesToUse = new byte[noBytesToUse];

            // Write the number of bytes.
            ExtIO.writeInt(bytesToUse, 0, len);

            // Write the content.
            System.arraycopy(byteArray, startIndex, bytesToUse, 4, len);

            // For unique encoding we put zeros at the end.
            Arrays.fill(bytesToUse, 4 + len, noBytesToUse, (byte)0);

            // Make sure value is non-zero. This byte is ignored when
            // decoding, since the length is zero.
            if (len == 0) {
                bytesToUse[5] = 1;
            }

            // Turn the resulting byte[] into a LargeInteger. This
            // integer is positive, since len is bounded giving
            // a starting zero-byte.
            value = new LargeInteger(bytesToUse);

            if (pGroup.encoding == ModPGroup.SAFEPRIME_ENCODING) {

                // Multiply by quadratic non-residue if needed.
                if (value.legendre(pGroup.modulus) != 1) {
                    value = value.neg().mod(pGroup.modulus);
                }

            } else if (pGroup.encoding == ModPGroup.SUBGROUP_ENCODING) {

                // Repeatedly add 2^noBytesToUse until we are in the
                // subgroup.
                int i = 0;

                for (; i < pGroup.encodingAttempts; i++) {
                    if (pGroup.contains(value)) {
                        break;
                    } else {
                        value = value.add(pGroup.addNum);
                    }
                }

                if (i == pGroup.encodingAttempts) {

                    // This should never happen. We expect that the
                    // probability that this happens is roughly
                    // 2^(-256).
                    throw new ArithmError("Encoding failed!");
                }

            } else {

                throw new ArithmError("Unknown encoding!");

            }
        }
    }

    /**
     * Returns the integer representating this group element.
     *
     * @return Integer representing this group element.
     */
    public LargeInteger toLargeInteger() {
        return value;
    }

    // Documented in PGroupElement.java.

    public ByteTreeBasic toByteTree() {
        byte[] temp = value.toByteArray();
        byte[] result = new byte[((ModPGroup)pGroup).modulusByteLength];

        // We know that temp.length <= modulusByteLength
        Arrays.fill(result,
                    0,
                    ((ModPGroup)pGroup).modulusByteLength - temp.length,
                    (byte)0);

        System.arraycopy(temp,
                         0,
                         result,
                         ((ModPGroup)pGroup).modulusByteLength - temp.length,
                         temp.length);

        return new ByteTree(result);
    }

    public String toString() {
        return value.toString(16);
    }

    public int decode(byte[] array, int startIndex) {

        byte[] raw = null;

        if (((ModPGroup)pGroup).encoding == ModPGroup.RO_ENCODING) {

            // Length is embedded in two bits.
            Hashfunction hf = new HashfunctionHeuristic("SHA-256");
            raw = hf.hash(toByteTree().toByteArray());

            int length = (int)(raw[0] & 0x03);
            length = Math.min(length, pGroup.getEncodeLength());

            System.arraycopy(raw, 1, array, startIndex, length);
            return length;

        } else {

            if (((ModPGroup)pGroup).encoding == ModPGroup.SAFEPRIME_ENCODING) {

                LargeInteger negValue =
                    value.neg().mod(((ModPGroup)pGroup).modulus);

                if (negValue.compareTo(value) < 0) {

                    raw = negValue.toByteArray();

                } else {

                    raw = value.toByteArray();

                }

            } else if (((ModPGroup)pGroup).encoding
                       == ModPGroup.SUBGROUP_ENCODING) {

                raw = value.toByteArray();

            }

            // Make sure we have sufficiently many bytes.
            if (raw.length < pGroup.getEncodeLength() + 4) {
                byte[] tmp = new byte[pGroup.getEncodeLength() + 4];
                System.arraycopy(raw, 0,
                                 tmp, tmp.length - raw.length,
                                 raw.length);
                raw = tmp;
            }


            // We jump over potential encoding bits.
            int offset = raw.length - (pGroup.getEncodeLength() + 4);

            // If the length is illegal, then we view it as zero.
            int len = ExtIO.readInt(raw, offset);
            if (!(0 <= len && len <= pGroup.getEncodeLength())) {
                return 0;
            }

            // We know that there are always len bytes to copy.
            System.arraycopy(raw, offset + 4, array, startIndex, len);

            return len;
        }
    }

    public PGroupElement mul(PGroupElement el) {
        if (!pGroup.equals(el.pGroup)) {
            throw new ArithmError("Distinct groups!");
        }
        LargeInteger li =
            value.mul(((ModPGroupElement)el).value)
            .mod(((ModPGroup)pGroup).modulus);
        return new ModPGroupElement((ModPGroup)pGroup, li);
    }

    public PGroupElement inv() throws ArithmError {
        try {

            LargeInteger li = value.modInv(((ModPGroup)pGroup).modulus);
            return new ModPGroupElement((ModPGroup)pGroup, li);

        } catch (ArithmException ae) {

            // This should never happen since the modulus is positive
            // and every element in the group is invertible.
            throw new ArithmError("The modulus " +
                                  ((ModPGroup)pGroup).modulus.toString()
                                  + " is non-positive or the element "
                                  + toString()
                                  + "is not invertible!",
                                  ae);
        }
    }

    public PGroupElement exp(PRingElement exponent) {
        if (!pGroup.pRing.equals(exponent.pRing)) {
            throw new ArithmError("Mismatching group and field!");
        }
        LargeInteger res = value.modPow(((PFieldElement)exponent).value,
                                        ((ModPGroup)pGroup).modulus);

        return new ModPGroupElement((ModPGroup)pGroup, res);
    }

    public PGroupElementArray exp(PRingElementArray exponents) {
        if (!pGroup.pRing.equals(exponents.pRing)) {
            throw new ArithmError("Mismatching elements!");
        }
        LargeIntegerArray integers = ((PFieldElementArray)exponents).values;

        LargeIntegerArray res =
            integers.modPowVariant(value, ((ModPGroup)pGroup).modulus);

        return new ModPGroupElementArray(pGroup, res);
    }

    public int compareTo(PGroupElement el) {
        ModPGroupElement mel = (ModPGroupElement)el;
        if (!pGroup.equals(mel.pGroup)) {
            throw new ArithmError("Distinct groups!");
        }
        return value.compareTo(mel.value);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModPGroupElement)) {
            return false;
        }
        ModPGroupElement el = (ModPGroupElement)obj;
        return value.compareTo(el.value) == 0 && pGroup.equals(el.pGroup);
    }
}
