
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
import vfork.protocol.coinflip.*;

/**
 * Mutual execution of proofs of shuffles.
 *
 * @author Douglas Wikstrom
 */
public class PoSTWSequential extends Protocol implements PoSMulti {

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
     * @param challenger Source of challenges.
     * @param prg PRG used for batching homomorphisms that allow this.
     * @param challengeBitLength Number of bits in challenges.
     * @param batchBitLength Number of bits in each component when batching.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public PoSTWSequential(String sid,
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

    // Documented in PoSMulti.java

    public boolean[] execute(Log log,
                             PGroupElement g,
                             PGroupElementArray generators,
                             PGroupElementArray[] permutationCommitments,
                             File exportDir) {
        return execute(log, g, generators, permutationCommitments, null, null,
                       exportDir);
    }

    public boolean[] execute(Log log,
                             PGroupElement g,
                             PGroupElementArray generators,
                             PGroupElementArray[] permutationCommitments,
                             PRingElementArray commitmentExponents,
                             Permutation permutation,
                             File exportDir) {

        log.info("Execute " + threshold +
                 " proofs of shuffles with rotating prover.");
        Log tempLog = log.newChildLog();

        boolean[] verdicts = new boolean[threshold + 1];

        for (int l = 1; l <= threshold; l++) {

            PoSTW pos = new PoSTW("" + l,
                                  this,
                                  challenger,
                                  prg,
                                  challengeBitLength,
                                  batchBitLength,
                                  statDist);
            if (l == j) {

                pos.prove(tempLog,
                          g,
                          generators,
                          permutationCommitments[l],
                          commitmentExponents,
                          permutation,
                          exportDir);

                // We are of course honest.
                verdicts[j] = true;

            } else {

                verdicts[l] = pos.verify(tempLog,
                                         l,
                                         g,
                                         generators,
                                         permutationCommitments[l],
                                         exportDir);
            }

        }
        return verdicts;
    }
}
