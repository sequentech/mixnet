
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
 * Mutual execution of sigma proofs with automatic batching if possible.
 *
 * @author Douglas Wikstrom
 */
public class SigmaProofSequential extends Protocol {

    /**
     * Underlying homomorphisms.
     */
    protected HomPRingPGroup[] homs;

    /**
     * Key data defining the homomorphisms.
     */
    protected PGroupElement[] keys;

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
     * Source of challenges.
     */
    protected Challenger challenger;

    /**
     * Creates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param homs Underlying homomorphisms.
     * @param keys Key data defining the homomorphism.
     * @param challenger Source of challenges.
     * @param prg PRG used for batching homomorphisms that allow this.
     * @param challengeBitLength Number of bits in challenges.
     * @param batchBitLength Number of bits in each component when batching.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public SigmaProofSequential(String sid,
                                Protocol protocol,
                                HomPRingPGroup[] homs,
                                PGroupElement[] keys,
                                Challenger challenger,
                                PRG prg,
                                int challengeBitLength,
                                int batchBitLength,
                                int statDist) {
        super(sid, protocol);
        this.homs = homs;
        this.keys = keys;
        this.challenger = challenger;
        this.prg = prg;
        this.challengeBitLength = challengeBitLength;
        this.batchBitLength = batchBitLength;
        this.statDist = statDist;
    }

    /**
     * Returns an array of the given size initialized with the given
     * homomorphism.
     *
     * @param k Number of homomorphisms.
     * @param hom Homomorphism to use.
     * @return Array containing copies of the given homomorphism.
     */
    protected static HomPRingPGroup[] homs(int k, HomPRingPGroup hom) {
        HomPRingPGroup[] homs = new HomPRingPGroup[k + 1];
        Arrays.fill(homs, hom);
        return homs;
    }

    /**
     * Returns an array of the given size initialized with the given
     * key.
     *
     * @param k Number of homomorphisms.
     * @param key Key defining the homomorphism.
     * @return Array containing copies of the given homomorphism.
     */
    protected static PGroupElement[] keys(int k, PGroupElement key) {
        PGroupElement[] keys = new PGroupElement[k + 1];
        Arrays.fill(keys, key);
        return keys;
    }

    /**
     * Creates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param hom Underlying homomorphism.
     * @param key Key data defining the homomorphism.
     * @param challenger Source of challenges.
     * @param prg PRG used for batching homomorphisms that allow this.
     * @param challengeBitLength Number of bits in challenges.
     * @param batchBitLength Number of bits in each component when batching.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public SigmaProofSequential(String sid,
                                Protocol protocol,
                                HomPRingPGroup hom,
                                PGroupElement key,
                                Challenger challenger,
                                PRG prg,
                                int challengeBitLength,
                                int batchBitLength,
                                int statDist) {
        this(sid, protocol, new HomPRingPGroup[0], null,
             challenger, prg, challengeBitLength, batchBitLength, statDist);

        this.homs = homs(k, hom);
        this.keys = keys(k, key);
    }


    /**
     * Executes mutual proofs sequentially.
     *
     * @param log Logging context.
     * @param commonInputs Common inputs of all parties.
     * @param privateInput Private input of this party.
     * @param exportDir Export directory for universal verifiability.
     */
    public boolean[] execute(Log log,
                             PGroupElement[] commonInputs,
                             PRingElement privateInput,
                             File exportDir) {

        log.info("Execute " + k + " sigma proofs with rotating prover.");
        Log tempLog = log.newChildLog();

        boolean[] verdicts = new boolean[k + 1];

        for (int l = 1; l <= k; l++) {

            SigmaProof sp = new SigmaProof("" + l,
                                           this,
                                           challenger,
                                           prg,
                                           challengeBitLength,
                                           batchBitLength,
                                           statDist);
            if (l == j) {

                sp.prove(tempLog,
                         homs[j],
                         keys[j],
                         commonInputs[j],
                         privateInput,
                         exportDir);

                verdicts[j] = true;

            } else {

                verdicts[l] = sp.verify(tempLog,
                                        l,
                                        homs[l],
                                        keys[l],
                                        commonInputs[l],
                                        exportDir);
            }

        }
        return verdicts;
    }
}
