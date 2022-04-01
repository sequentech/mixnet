
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

package mixnet.protocol.hvzk;

import java.io.*;
import java.security.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;

import mixnet.test.*;

/**
 * Tests some of the functionality of {@link SigmaProofBasic}.
 *
 * @author Douglas Wikstrom
 */
public class TestSigmaProofBasic {

    public static int runTest(TestParameters tp, boolean rejecting)
        throws Exception {

        // Set up context

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());
        PGroup modPGroup = new ModPGroup(tp.testSize);

        PGroup pGroup = new PPGroup(new PPGroup(modPGroup, modPGroup),
                                     modPGroup);

        BiPRingPGroup bi = new BiExp(pGroup);

        int challengeBitLength = 100;
        int statDist = 50;

        PRingElement privateInput =
            bi.getPRingDomain().randomElement(rs, statDist);

        PGroupElement basis =
            bi.getPGroupDomain().randomElement(rs, statDist);
        PGroupElement commonInput = bi.map(privateInput, basis);

        if (rejecting) {
            PFieldElement two =
                bi.getPGroupDomain().getPRing().getPField().
                toElement(LargeInteger.TWO);
            commonInput = commonInput.exp(two);
        }

        HomPRingPGroup hom = bi.restrict(basis);


        // Initialize both parties with the instance.

        SigmaProofBasic P = new SigmaProofBasic(hom,
                                                challengeBitLength,
                                                statDist);
        P.setInstance(commonInput, privateInput);

        SigmaProofBasic V = new SigmaProofBasic(hom,
                                                challengeBitLength,
                                                statDist);
        V.setInstance(commonInput);

        // Execute the protocol

        ByteTreeBasic commitment = P.commit(rs);
        LargeInteger challenge =
            V.challenge(commitment.getByteTreeReader(), rs);

        ByteTreeBasic reply = P.reply(challenge);

        return (V.verify(reply.getByteTreeReader()) ? 1 : 0);
    }

    public static boolean acceptingTranscript(TestParameters tp)
        throws Exception {
        return runTest(tp, false) == 1;
    }

    public static boolean rejectingTranscript(TestParameters tp)
        throws Exception {
        return runTest(tp, true) == 0;
    }

    public static int runBatchTest(TestParameters tp, boolean rejecting)
        throws Exception {

        // Set up context

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());
        PGroup modPGroup = new ModPGroup(tp.testSize);


        PGroup pGroup = new PPGroup(new PPGroup(modPGroup, modPGroup),
                                     modPGroup);

        BiPRingPGroup bi = new BiFixedBaseExp(pGroup, 10);

        int batchBitLength = 100;
        int challengeBitLength = 100;
        int statDist = 50;

        PRingElement privateInput =
            bi.getPRingDomain().randomElement(rs, statDist);

        PGroupElement basis =
            bi.getPGroupDomain().randomElement(rs, statDist);

        PGroupElement commonInput = bi.map(privateInput, basis);

        if (rejecting) {
            PFieldElement two = bi.getPGroupDomain().getPRing().getPField().
                toElement(LargeInteger.TWO);
            commonInput = commonInput.exp(two);
        }

        HomPRingPGroup hom = bi.restrict(basis);

        // Initialize both parties with the instance.

        SigmaProofBasic P = new SigmaProofBasic(hom,
                                                challengeBitLength,
                                                statDist);
        P.setInstance(commonInput, privateInput);

        SigmaProofBasic V = new SigmaProofBasic(hom,
                                                challengeBitLength,
                                                statDist);
        V.setInstance(commonInput);

        // Both parties batch
        PRG prg = new PRGHeuristic(tp.prgseed.getBytes());
        P.batch(prg, batchBitLength);

        prg = new PRGHeuristic(tp.prgseed.getBytes());
        V.batch(prg, batchBitLength);


        // Execute the protocol

        ByteTreeBasic commitment = P.commit(rs);

        LargeInteger challenge =
            V.challenge(commitment.getByteTreeReader(), rs);

        ByteTreeBasic reply = P.reply(challenge);

        return (V.verify(reply.getByteTreeReader()) ? 1 : 0);
    }

    public static boolean acceptingBatchTranscript(TestParameters tp)
        throws Exception {
        return runBatchTest(tp, false) == 1;
    }

    public static boolean rejectingBatchTranscript(TestParameters tp)
        throws Exception {
        return runTest(tp, true) == 0;
    }
}
