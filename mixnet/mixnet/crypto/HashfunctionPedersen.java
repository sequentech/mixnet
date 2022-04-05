
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

package mixnet.crypto;

import java.util.*;

import mixnet.arithm.*;
import mixnet.eio.*;
import mixnet.ui.*;

/**
 * Implementation of Pedersen's fixed length hash function. This is
 * collision-free under the discrete logarithm assumption in the
 * underlying group.
 *
 * @author Douglas Wikstrom
 */
public class HashfunctionPedersen implements HashfunctionFixedLength {

    public final static int MAX_WIDTH = 10;

    /**
     * Number of bits that can be input to the function.
     */
    protected int inputLength;

    /**
     * Number of bits output by the function.
     */
    protected int outputLength;

    /**
     * Maximal number of bytes that can be converted injectively into
     * an element of the field associated with the group over which we
     * compute.
     */
    protected int expLength;

    /**
     * Independent generators.
     */
    protected PGroupElement[] generators;

    /**
     * Constructs an instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Hashfunction represented by the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static HashfunctionPedersen newInstance(ByteTreeReader btr,
                                                   RandomSource rs,
                                                   int certainty)
    throws CryptoFormatException {
        try {

            PGroup pGroup =
                Marshalizer.unmarshalAux_PGroup(btr.getNextChild(),
                                                rs,
                                                certainty);
            ByteTreeReader gbtr = btr.getNextChild();
            if (gbtr.getRemaining() > MAX_WIDTH) {
                throw new CryptoFormatException("Too many generators!");
            }
            PGroupElement[] generators =
                pGroup.toElements(gbtr.getRemaining(), gbtr);

            return new HashfunctionPedersen(generators);

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (ArithmFormatException afe) {
            throw new CryptoFormatException("Can not interpret!", afe);
        }
    }

    /**
     * Creates an instance defined by the generators given as
     * input. This does not copy the input.
     *
     * @param generators Generators that define this instance.
     */
    public HashfunctionPedersen(PGroupElement ... generators) {
        this.generators = generators;
        init();
    }

    /**
     * Creates a random instance defined over the given group.
     *
     * @param pGroup Group over which the function is defined.
     * @param width Number of generators.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public HashfunctionPedersen(PGroup pGroup, int width, RandomSource rs,
                                int statDist) {

        // Generate independent group elements.
        generators = new PGroupElement[width];
        for (int i = 0; i < width; i++) {
            generators[i] = pGroup.randomElement(rs, statDist);
        }

        init();
    }

    /**
     * Completes the initialization of this instance.
     */
    protected void init() {
        // Compute input and output byte lengths.
        PGroup pGroup = generators[0].getPGroup();
        expLength = pGroup.getPRing().getPField().getEncodeLength();
        inputLength = 8 * generators.length * expLength;
        outputLength = 8 * pGroup.getByteLength();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < generators.length; i++) {
            sb.append(generators[i].toString());
            if (i > 0) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    // Documented in FixedLengthHashfunction.java.

    public byte[] hash(byte[] input) {
        if (input.length > inputLength / 8) {
            throw new CryptoError("Input has wrong length!");
        }

        PFieldElement[] exponents = new PFieldElement[generators.length];
        PField pField = generators[0].getPGroup().getPRing().getPField();
        int offset = 0;
        for (int i = 0; i < exponents.length; i++) {
            exponents[i] = pField.toElement(LargeInteger.toPositive(input,
                                                                    offset,
                                                                    expLength));
            offset += expLength;
        }

        return generators[0].getPGroup().expProd(generators, exponents).
            toByteArray();
    }

    public int getInputLength() {
        return inputLength;
    }

    public int getOutputLength() {
        return outputLength;
    }

    // Documented in Marshalizable.java

    public ByteTreeBasic toByteTree() {

        PGroup pGroup = generators[0].getPGroup();
        ByteTreeBasic pGroupByteTree = Marshalizer.marshal(pGroup);
        ByteTreeBasic generatorsByteTree = pGroup.toByteTree(generators);

        return new ByteTreeContainer(pGroupByteTree, generatorsByteTree);
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + generators[0].getPGroup().humanDescription(verbose) +
            ", width=" + generators.length + ")";
    }
}
