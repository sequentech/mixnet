
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

package vfork.crypto;

import vfork.arithm.*;
import vfork.eio.*;
import vfork.ui.*;

/**
 * Interface representing a key generation algorithm of a CramerShoup
 * cryptosystem over a given group and using a given hashfunction.
 *
 * @author Douglas Wikstrom
 */
public class CryptoKeyGenCramerShoup implements CryptoKeyGen {

    /**
     * Underlying group.
     */
    protected PGroup pGroup;

    /**
     * Underlying hashfunction.
     */
    protected Hashfunction crhf;

    /**
     * Constructs an instance corresponding to the input representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Key generator represented by the input.
     *
     * @throws CryptoFormatException If the input does not represent
     * valid instructions for creating an instance.
     */
    public static CryptoKeyGenCramerShoup newInstance(ByteTreeReader btr,
                                                      RandomSource rs,
                                                      int certainty)
    throws CryptoFormatException {
        try {

            PGroup pGroup =
                Marshalizer.unmarshalAux_PGroup(btr.getNextChild(),
                                                rs, certainty);

            Hashfunction crhf =
                Marshalizer.unmarshalAux_Hashfunction(btr.getNextChild(),
                                                      rs, certainty);

            return new CryptoKeyGenCramerShoup(pGroup, crhf);

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed key!", eioe);
        }
    }

    /**
     * Creates an instance with the given underlying group and hashfunction.
     *
     * @param pGroup Underlying group.
     * @param crhf Underlying collision-resistant hashfunction.
     */
    public CryptoKeyGenCramerShoup(PGroup pGroup, Hashfunction crhf) {
        this.pGroup = pGroup;
        this.crhf = crhf;
    }

    /**
     * Prints a string representation of this instance. This should
     * only be used for debugging.
     *
     * @return String representation of this instance.
     */
    public String toString() {
        return pGroup.toString() + ":" + crhf.toString();
    }


    // Documented in CryptoKeyGen.java

    public CryptoKeyPair gen(RandomSource randomSource, int statDist) {
        PRing pRing = pGroup.getPRing();
        PRingElement z = pRing.randomElement(randomSource, statDist);
        PRingElement x1 = pRing.randomElement(randomSource, statDist);
        PRingElement x2 = pRing.randomElement(randomSource, statDist);
        PRingElement y1 = pRing.randomElement(randomSource, statDist);
        PRingElement y2 = pRing.randomElement(randomSource, statDist);

        PGroupElement g1 = pGroup.getg();
        PRingElement r = pRing.randomElement(randomSource, statDist);
        PGroupElement g2 = pGroup.getg().exp(r);

        PGroupElement h = g1.exp(z);
        PGroupElement c = g1.exp(x1).mul(g2.exp(x2));
        PGroupElement d = g1.exp(y1).mul(g2.exp(y2));

        CryptoPKey pkey =
            new CryptoPKeyCramerShoup(crhf, g1, g2, h, c, d);
        CryptoSKey skey =
            new CryptoSKeyCramerShoup(pGroup, crhf, z, x1, x2, y1, y2);

        return new CryptoKeyPair(pkey, skey);
    }


    // Documented in Marshalizable.java

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(pGroup),
                                     Marshalizer.marshal(crhf));
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + pGroup.humanDescription(verbose) + "," +
            crhf.humanDescription(verbose) + ")";
    }
}
