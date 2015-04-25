
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

import verificatum.arithm.*;
import verificatum.eio.*;
import verificatum.ui.*;

/**
 * Public key of a Cramer-Shoup cryptosystem.
 *
 * @author Douglas Wikstrom
 */
public class CryptoPKeyCramerShoup implements CryptoPKey {

    /* Notation used here matches that of the original Cramer-Shoup
     * paper.
     */

    /**
     * Underlying collision-resistant hashfunction.
     */
    protected Hashfunction crhf;

    /**
     * First basis element.
     */
    protected PGroupElement g1;

    /**
     * Second basis element.
     */
    protected PGroupElement g2;

    /**
     * Encryption element.
     */
    protected PGroupElement h;

    /**
     * First proof element.
     */
    protected PGroupElement c;

    /**
     * Second proof element.
     */
    protected PGroupElement d;

    /**
     * Creates a public from the given parameters.
     *
     * @param crhf Underlying collision-resistant hashfunction.
     * @param g1 First basis element.
     * @param g2 Second basis element.
     * @param h Encryption element.
     * @param c First proof element.
     * @param d Second proof element.
     */
    public CryptoPKeyCramerShoup(Hashfunction crhf,
                                 PGroupElement g1,
                                 PGroupElement g2,
                                 PGroupElement h,
                                 PGroupElement c,
                                 PGroupElement d) {
        this.crhf = crhf;
        this.g1 = g1;
        this.g2 = g2;
        this.h = h;
        this.c = c;
        this.d = d;
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
    public static CryptoPKeyCramerShoup newInstance(ByteTreeReader btr,
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
            PGroupElement g1 = pGroup.toElement(btr.getNextChild());
            PGroupElement g2 = pGroup.toElement(btr.getNextChild());
            PGroupElement h = pGroup.toElement(btr.getNextChild());
            PGroupElement c = pGroup.toElement(btr.getNextChild());
            PGroupElement d = pGroup.toElement(btr.getNextChild());

            return new CryptoPKeyCramerShoup(crhf, g1, g2, h, c, d);

        } catch (ArithmFormatException afe) {
            throw new CryptoFormatException("Malformed key!", afe);
        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed key!", eioe);
        }
    }

    // Documented in CryptoPKey.java

    public byte[] encrypt(byte[] label, byte[] message,
                          RandomSource randomSource,
                          int statDist) {

        if (message.length == 0) {
            return new byte[0];
        }

        // Convert into a list of group elements.
        PGroupElement[] els =
            g1.getPGroup().encode(ExtIO.lengthEmbedded(message), randomSource);

        // Map to product group.
        PPGroup pPGroup = new PPGroup(g1.getPGroup(), els.length);

        PGroupElement m = pPGroup.product(els);
        PGroupElement pg1 = pPGroup.product(g1);
        PGroupElement pg2 = pPGroup.product(g2);
        PGroupElement ph = pPGroup.product(h);
        PGroupElement pc = pPGroup.product(c);
        PGroupElement pd = pPGroup.product(d);

        // Perform encryption in product group.
        PRingElement r =
            pg1.getPGroup().getPRing().randomElement(randomSource, statDist);
        PGroupElement u1 = pg1.exp(r);
        PGroupElement u2 = pg2.exp(r);
        PGroupElement e = ph.exp(r).mul(m);

        // It is safe to hash group elements in raw format, since they
        // have fixed size.
        byte[] digest = crhf.hash(ExtIO.lengthEmbedded(label),
                                  u1.toByteArray(),
                                  u2.toByteArray(),
                                  e.toByteArray());
        PFieldElement digestElement =
            r.getPRing().getPField().toElement(LargeInteger.toPositive(digest));

        PGroupElement w = pc.exp(r).mul(pd.exp(r.mul(digestElement)));

        // Pack the result and return as byte[]
        ByteTreeBasic btb =
            new ByteTreeContainer(ByteTree.intToByteTree(els.length),
                                  u1.toByteTree(),
                                  u2.toByteTree(),
                                  e.toByteTree(),
                                  w.toByteTree());
        return btb.toByteArray();
    }


    // Documented in ByteTreeConvertible.java

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(crhf),
                                     Marshalizer.marshal(g1.getPGroup()),
                                     g1.toByteTree(),
                                     g2.toByteTree(),
                                     h.toByteTree(),
                                     c.toByteTree(),
                                     d.toByteTree());
    }

    // Documented in Marshalizable.java

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + crhf.humanDescription(verbose) + ","
            + g1.getPGroup().humanDescription(verbose) + ")";
    }
}
