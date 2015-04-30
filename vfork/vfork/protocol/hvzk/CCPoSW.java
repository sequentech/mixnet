
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

package vfork.protocol.hvzk;

import java.io.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.ui.*;
import vfork.protocol.*;

/**
 * Implementation of Wikstrom's commitment-consistent proof of a shuffle.
 *
 * @author Douglas Wikstrom
 */
public class CCPoSW extends Protocol implements CCPoS {

    /**
     * Source of challenges.
     */
    protected Challenger challenger;

    /**
     * PRG used for batching.
     */
    protected PRG prg;

    /**
     * Bit-size of the challenge.
     */
    protected int challengeBitLength;

    /**
     * Bit-size of each component when batching.
     */
    protected int batchBitLength;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Creates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param challenger Source of challenges.
     * @param prg PRG used for batching homomorphisms that allow this.
     * @param challengeBitLength Number of bits in challenges.
     * @param batchBitLength Number of bits in each component when batching.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public CCPoSW(String sid,
                  Protocol protocol,
                  Challenger challenger,
                  PRG prg,
                  int challengeBitLength,
                  int batchBitLength,
                  int statDist) {
        super(sid, protocol);
        this.challenger = challenger;
        this.prg = prg;
        this.challengeBitLength = challengeBitLength;
        this.batchBitLength = batchBitLength;
        this.statDist = statDist;
    }


    /**
     * Name of file containing commitment of proof of shuffle.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where permutation commitments are stored.
     */
    protected static File CCPoSCfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("CCPoSCommitment%02d.bt", index));
    }

    /**
     * Name of file containing reply of proof of shuffle.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where permutation commitments are stored.
     */
    protected static File CCPoSRfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("CCPoSReply%02d.bt", index));
    }

    // Documented in PoS.java

    public void prove(Log log,
                      PGroupElement g,
                      PGroupElementArray h,
                      PGroupElementArray u,
                      HomPRingPGroup hom,
                      PGroupElement pkey,
                      PGroupElementArray w,
                      PGroupElementArray wp,
                      PRingElementArray r,
                      Permutation pi,
                      PRingElementArray s,
                      File exportDir) {

        log.info("Prove correctness of shuffle.");
        Log tempLog = log.newChildLog();

        CCPoSBasicW P = new CCPoSBasicW(challengeBitLength,
                                        batchBitLength,
                                        statDist,
                                        prg);

        P.setInstance(g, h, u, hom, w, wp, r, pi, s);

        // Generate a seed to the PRG for batching.
        tempLog.info("Generate batching vector.");
        Log tempLog2 = tempLog.newChildLog();

        ByteTreeContainer challengeData =
            new ByteTreeContainer(g.toByteTree(),
                                  h.toByteTree(),
                                  u.toByteTree(),
                                  pkey.toByteTree(),
                                  w.toByteTree(),
                                  wp.toByteTree());
        byte[] prgSeed = challenger.challenge(tempLog2,
                                              challengeData,
                                              8 * prg.minNoSeedBytes(),
                                              statDist);

        // Compute and publish commitment.
        tempLog.info("Compute commitment.");
        ByteTreeBasic commitment = P.commit(prgSeed, randomSource);

        if (exportDir != null) {
            commitment.unsafeWriteTo(CCPoSCfile(exportDir, j));
        }

        tempLog.info("Publish our commitment.");
        bullBoard.publish("Commitment", commitment, tempLog);

        // Generate a challenge.
        tempLog.info("Generate challenge.");
        tempLog2 = tempLog.newChildLog();
        challengeData = new ByteTreeContainer(new ByteTree(prgSeed),
                                              commitment);
        byte[] challengeBytes = challenger.challenge(tempLog2,
                                                     challengeData,
                                                     challengeBitLength,
                                                     statDist);
        LargeInteger integerChallenge = LargeInteger.toPositive(challengeBytes);

        // Compute and publish reply.
        tempLog.info("Compute reply.");
        ByteTreeBasic reply = P.reply(integerChallenge);

        if (exportDir != null) {
            reply.unsafeWriteTo(CCPoSRfile(exportDir, j));
        }

        tempLog.info("Publish reply.");
        bullBoard.publish("Reply", reply, tempLog);

        P.free();
    }

    public boolean verify(Log log,
                          int l,
                          PGroupElement g,
                          PGroupElementArray h,
                          PGroupElementArray u,
                          HomPRingPGroup hom,
                          PGroupElement pkey,
                          PGroupElementArray w,
                          PGroupElementArray wp,
                          File exportDir) {

        log.info("Verify correctness of shuffle of " +
                 ui.getDescrString(l) + ".");
        Log tempLog = log.newChildLog();

        CCPoSBasicW V = new CCPoSBasicW(challengeBitLength,
                                        batchBitLength,
                                        statDist,
                                        prg);
        V.setInstance(g, h, u, hom, w, wp);

        // Generate a seed to the PRG for batching.
        tempLog.info("Generate batching vector.");
        Log tempLog2 = tempLog.newChildLog();

        ByteTreeContainer challengeData =
            new ByteTreeContainer(g.toByteTree(),
                                  h.toByteTree(),
                                  u.toByteTree(),
                                  pkey.toByteTree(),
                                  w.toByteTree(),
                                  wp.toByteTree());
        byte[] prgSeed = challenger.challenge(tempLog2,
                                              challengeData,
                                              8 * prg.minNoSeedBytes(),
                                              statDist);
        V.setBatchVector(prgSeed);

        // Read and set the commitment of the prover.
        tempLog.info("Read the commitment.");

        ByteTreeReader commitmentReader =
            bullBoard.waitFor(l, "Commitment", tempLog);
        ByteTreeBasic commitment = V.setCommitment(commitmentReader);
        commitmentReader.close();

        if (exportDir != null) {
            commitment.unsafeWriteTo(CCPoSCfile(exportDir, l));
        }

        // Generate a challenge
        tempLog.info("Generate challenge.");
        tempLog2 = tempLog.newChildLog();
        challengeData = new ByteTreeContainer(new ByteTree(prgSeed),
                                              commitment);
        byte[] challengeBytes = challenger.challenge(tempLog2,
                                                     challengeData,
                                                     challengeBitLength,
                                                     statDist);
        LargeInteger integerChallenge = LargeInteger.toPositive(challengeBytes);

        // Set the commitment and challenge.
        V.setChallenge(integerChallenge);


        // Read and verify reply.
        tempLog.info("Read the reply.");
        ByteTreeReader replyReader = bullBoard.waitFor(l, "Reply", tempLog);

        tempLog.info("Perform verification.");
        boolean verdict = V.verify(replyReader);
        replyReader.close();

        if (verdict && exportDir != null) {
            V.getReply().unsafeWriteTo(CCPoSRfile(exportDir, l));
        }

        if (verdict) {
            tempLog.info("Accepted proof.");
        } else {
            tempLog.info("Rejected proof.");
        }

        V.free();

        return verdict;
    }
}
