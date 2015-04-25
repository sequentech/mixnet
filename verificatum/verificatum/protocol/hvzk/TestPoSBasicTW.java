
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

package verificatum.protocol.hvzk;

import java.io.*;
import java.security.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;

import verificatum.test.*;

/**
 * Tests some of the functionality of {@link SigmaProofBasic}.
 *
 * @author Douglas Wikstrom
 */
public class TestPoSBasicTW {

    public static int runTest(TestParameters tp, boolean correct)
        throws Exception {

        // Set up context

        PGroup modPGroup = new ModPGroup(tp.testSize);

        PGroup pGroup = modPGroup;

        // PGroup pGroup = new PPGroup(new PPGroup(modPGroup, modPGroup),
        //                              modPGroup);

        int size = 100;

        int batchBitLength = 100;
        int challengeBitLength = 100;
        int statDist = 50;
        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());
        PRG prg = new PRGHeuristic();

        byte[] prgSeed = new byte[prg.minNoSeedBytes()];
        Arrays.fill(prgSeed, (byte)0);

        // Create instance

        PGroupElement g = pGroup.getg();
        PGroupElementArray h =
            g.exp(pGroup.getPRing().randomElementArray(size, rs, statDist));

        PRingElementArray r =
            pGroup.getPRing().randomElementArray(size, rs, statDist);

        PGroupElementArray u = g.exp(r).mul(h);

        Permutation pi = new Permutation(size, rs, statDist);

        u = u.permute(pi);

        // Execute the protocol

        PoSBasicTW P = new PoSBasicTW(challengeBitLength,
                                      batchBitLength,
                                      statDist,
                                      prg);

        PoSBasicTW V = new PoSBasicTW(challengeBitLength,
                                      batchBitLength,
                                      statDist,
                                      prg);
        if (!correct) {
            r = r.add(r);
        }

        P.setInstance(g, h, u, r, pi);
        V.setInstance(g, h, u);

        ByteTreeBasic commitment = P.commit(prgSeed, rs);

        V.setBatchVector(prgSeed);

        ByteTreeReader btrCommit = commitment.getByteTreeReader();
        V.setCommitment(btrCommit);
        btrCommit.close();

        LargeInteger integerChallenge =
            new LargeInteger(P.getChallengeBitLength(), rs);

        V.setChallenge(integerChallenge);

        ByteTreeBasic reply = P.reply(integerChallenge);

        ByteTreeReader btrReply = reply.getByteTreeReader();
        boolean verdict = V.verify(btrReply);
        btrReply.close();

        return (verdict ? 1 : 0);
    }

    public static boolean acceptingTranscript(TestParameters tp)
        throws Exception {
        return runTest(tp, true) == 1;
    }

    public static boolean rejectingTranscript(TestParameters tp)
        throws Exception {
        return runTest(tp, false) == 0;
    }
}
