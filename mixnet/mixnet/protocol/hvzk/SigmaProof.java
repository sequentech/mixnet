
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
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;
import mixnet.protocol.*;
import mixnet.protocol.coinflip.*;

/**
 * Sigma proof for a homomorphism with automatic batching if possible.
 *
 * @author Douglas Wikstrom
 */
public class SigmaProof extends Protocol {

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
    public SigmaProof(String sid,
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
     * Name of file containing the commitment of a proof.
     *
     * @param exportDir Export directory for universal verifiability.
     * @return File where the secret key is stored.
     */
    public static File Cfile(File exportDir) {
        return new File(exportDir, "C.bt");
    }

    /**
     * Name of file containing the reply of a proof.
     *
     * @param exportDir Export directory for universal verifiability.
     * @return File where the secret key is stored.
     */
    public static File Rfile(File exportDir) {
        return new File(exportDir, "R.bt");
    }

    /**
     * Executes the prover.
     *
     * @param log Logging context.
     * @param hom Underlying homomorphism.
     * @param key Key data defining the homomorphism.
     * @param commonInput Common inputs of all parties.
     * @param privateInput Private input of this party.
     * @param exportDir Export directory for universal verifiability.
     */
    public void prove(Log log,
                      HomPRingPGroup hom,
                      PGroupElement key,
                      PGroupElement commonInput,
                      PRingElement privateInput,
                      File exportDir) {

        log.info("Execute prover of sigma proof.");
        Log tempLog = log.newChildLog();

        // Initialize basic prover.
        SigmaProofBasic P = new SigmaProofBasic(hom,
                                                challengeBitLength,
                                                statDist);
        P.setInstance(commonInput, privateInput);

        // If the homomorphism is batchable we batch before executing
        // the Sigma proof.
        byte[] seed = null;
        if (hom instanceof Batchable) {

            // Generate a seed.
            tempLog.info("Generate seed for batching.");
            Log tempLog2 = tempLog.newChildLog();

            ByteTreeBasic seedData =
                new ByteTreeContainer(key.toByteTree(),
                                      commonInput.toByteTree());

            seed = challenger.challenge(tempLog2,
                                        seedData,
                                        8 * prg.minNoSeedBytes(),
                                        statDist);
            // Batch our prover.
            prg.setSeed(seed);
            P.batch(prg, batchBitLength);
        }

        // Compute and publish commitment.
        tempLog.info("Compute our commitment.");
        ByteTreeBasic commitment = P.commit(randomSource);

        if (exportDir != null) {
            commitment.unsafeWriteTo(Cfile(exportDir));
        }

        tempLog.info("Publish our commitent.");
        bullBoard.publish("Commitment", commitment, tempLog);


        // Generate a challenge
        tempLog.info("Generate challenge.");
        Log tempLog2 = tempLog.newChildLog();

        ByteTreeContainer challengeData = null;
        if (hom instanceof Batchable) {
            challengeData = new ByteTreeContainer(new ByteTree(seed),
                                                  commitment);
        } else {
            challengeData = new ByteTreeContainer(key.toByteTree(),
                                                  commonInput.toByteTree(),
                                                  commitment);
        }
        byte[] challengeBytes = challenger.challenge(tempLog2,
                                                     challengeData,
                                                     challengeBitLength,
                                                     statDist);
        LargeInteger integerChallenge = LargeInteger.toPositive(challengeBytes);

        // Compute and publish reply.
        tempLog.info("Compute reply.");
        ByteTreeBasic reply = P.reply(integerChallenge);

        if (exportDir != null) {
            reply.unsafeWriteTo(Rfile(exportDir));
        }

        tempLog.info("Publish reply.");
        bullBoard.publish("Reply", reply, tempLog);
        P.free();
    }

    /**
     * Executes the verifier.
     *
     * @param log Logging context.
     * @param l Index of prover.
     * @param hom Underlying homomorphism.
     * @param key Key data defining the homomorphism.
     * @param commonInput Public input of this party.
     * @param exportDir Export directory for universal verifiability.
     * @return Verdict about the proof.
     */
    public boolean verify(Log log,
                          int l,
                          HomPRingPGroup hom,
                          PGroupElement key,
                          PGroupElement commonInput,
                          File exportDir) {

        log.info("Verify the proof of " + ui.getDescrString(l) + ".");
        Log tempLog = log.newChildLog();

        SigmaProofBasic V = new SigmaProofBasic(hom,
                                                challengeBitLength,
                                                statDist);
        V.setInstance(commonInput);

        // If the homomorphism is batchable we batch before executing
        // the Sigma proof.
        byte[] seed = null;
        if (hom instanceof Batchable) {

            // Generate a seed.
            tempLog.info("Generate seed for batching.");
            Log tempLog2 = tempLog.newChildLog();

            ByteTreeBasic seedData =
                new ByteTreeContainer(key.toByteTree(),
                                      commonInput.toByteTree());
            seed = challenger.challenge(tempLog2,
                                        seedData,
                                        8 * prg.minNoSeedBytes(),
                                        statDist);
            // Batch our verifier.
            prg.setSeed(seed);
            V.batch(prg, batchBitLength);
        }

        // Read and set the commitment of the prover.
        tempLog.info("Read the commitment.");
        ByteTreeReader commitmentReader =
            bullBoard.waitFor(l, "Commitment", tempLog);

        ByteTreeBasic commitment = V.setCommitment(commitmentReader);
        commitmentReader.close();

        if (exportDir != null) {
            commitment.unsafeWriteTo(Cfile(exportDir));
        }

        // Generate a challenge
        tempLog.info("Generate challenge.");
        Log tempLog2 = tempLog.newChildLog();
        ByteTreeContainer challengeData = null;

        if (hom instanceof Batchable) {
            challengeData = new ByteTreeContainer(new ByteTree(seed),
                                                  commitment);
        } else {
            challengeData = new ByteTreeContainer(key.toByteTree(),
                                                  commonInput.toByteTree(),
                                                  commitment);
        }

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
            V.getReply().unsafeWriteTo(Rfile(exportDir));
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
