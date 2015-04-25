
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

package verificatum.crypto;

import java.util.*;

import verificatum.arithm.*;
import verificatum.eio.*;
import verificatum.ui.*;

/**
 * Secret key of a Cramer-Shoup cryptosystem.
 *
 * @author Douglas Wikstrom
 */
public class CryptoSKeyCramerShoup implements CryptoSKey {

    /**
     * Underlying group.
     */
    protected PGroup pGroup;

    /**
     * Underlying collision-resistant hashfunction.
     */
    protected Hashfunction crhf;

    /**
     * Encryption exponent.
     */
    protected PRingElement z;

    /**
     * First proof exponent.
     */
    protected PRingElement x1;

    /**
     * Second proof exponent.
     */
    protected PRingElement x2;

    /**
     * Third proof exponent.
     */
    protected PRingElement y1;

    /**
     * Fourth proof exponent.
     */
    protected PRingElement y2;

    /**
     * Creates a secret with the given components.
     *
     * @param pGroup Underlying group.
     * @param crhf Underlying collision-resistant hashfunction.
     * @param z Encryption exponent.
     * @param x1 First proof exponent.
     * @param x2 Second proof exponent.
     * @param y1 Third proof exponent.
     * @param y2 Fourth proof exponent.
     */
    public CryptoSKeyCramerShoup(PGroup pGroup,
                                 Hashfunction crhf,
                                 PRingElement z,
                                 PRingElement x1,
                                 PRingElement x2,
                                 PRingElement y1,
                                 PRingElement y2) {
        this.pGroup = pGroup;
        this.crhf = crhf;
        this.z = z;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

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
     * @throws CryptoFormatException If the input does not represent
     * valid instructions for creating an instance.
     */
    public static CryptoSKeyCramerShoup newInstance(ByteTreeReader btr,
                                                    RandomSource rs,
                                                    int certainty)
    throws CryptoFormatException {
        try {

            Hashfunction crhf =
                Marshalizer.unmarshalAux_Hashfunction(btr.getNextChild(),
                                                      rs, certainty);
            PGroup pGroup =
                Marshalizer.unmarshalAux_PGroup(btr.getNextChild(),
                                                rs, certainty);
            PRing pRing = pGroup.getPRing();
            PRingElement z = pRing.toElement(btr.getNextChild());
            PRingElement x1 = pRing.toElement(btr.getNextChild());
            PRingElement x2 = pRing.toElement(btr.getNextChild());
            PRingElement y1 = pRing.toElement(btr.getNextChild());
            PRingElement y2 = pRing.toElement(btr.getNextChild());

            return new CryptoSKeyCramerShoup(pGroup, crhf, z, x1, x2, y1, y2);

        } catch (ArithmFormatException afe) {
            throw new CryptoFormatException("Malformed key!", afe);
        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed key!", eioe);
        }
    }



    // Documented in CryptoSKey.java

    public byte[] decrypt(byte[] label, byte[] ciphertext) {


        if (ciphertext.length == 0) {
            return new byte[0];
        }

        try {
            ByteTreeReader btr =
                (new ByteTree(ciphertext, null)).getByteTreeReader();

            int width = btr.getNextChild().readInt();

            PGroup pPGroup = new PPGroup(pGroup, width);
            PGroupElement u1 = pPGroup.toElement(btr.getNextChild());
            PGroupElement u2 = pPGroup.toElement(btr.getNextChild());
            PGroupElement e = pPGroup.toElement(btr.getNextChild());
            PGroupElement w = pPGroup.toElement(btr.getNextChild());
            btr.close();

            // It is safe to hash group elements in raw format, since
            // they have fixed size.
            byte[] digest = crhf.hash(ExtIO.lengthEmbedded(label),
                                      u1.toByteArray(),
                                      u2.toByteArray(),
                                      e.toByteArray());
            PFieldElement digestElement = y2.getPRing().getPField().
                toElement(LargeInteger.toPositive(digest));

            if (u1.exp(x1.add(y1.mul(digestElement))).
                mul(u2.exp(x2.add(y2.mul(digestElement)))).equals(w)) {

                PPGroupElement mel = (PPGroupElement)e.mul(u1.exp(z.neg()));
                PGroupElement[] mels = mel.getFactors();
                byte[] lem = pGroup.decode(mels);
                int len = ExtIO.readInt(lem, 0);

                if (len <= lem.length - 4) {
                    return Arrays.copyOfRange(lem, 4, len + 4);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (ArithmFormatException afe) {
            return null;
        } catch (EIOException eioe) {
            return null;
        }
    }


    // Documented in Marshalizable.java

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(crhf),
                                     Marshalizer.marshal(pGroup),
                                     z.toByteTree(),
                                     x1.toByteTree(),
                                     x2.toByteTree(),
                                     y1.toByteTree(),
                                     y2.toByteTree());
    }

    // Documented in Marshalizable.java

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + crhf.humanDescription(verbose) + ","
            + pGroup.humanDescription(verbose) + ")";
    }

}
