
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

package vfork.protocol.distrkeygen;

import java.io.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.protocol.*;
import vfork.protocol.coinflip.*;
import vfork.protocol.hvzk.*;
import vfork.ui.*;

/**
 * Generates a list of independent generators, i.e., a list of
 * generators for which finding any non-trivial representation implies
 * that the discrete logarithm assumption is violated.
 *
 * @author Douglas Wikstrom
 */
public class IndependentGeneratorsI extends Protocol
    implements IndependentGenerators {

    /**
     * Source of random coins.
     */
    protected CoinFlipPRingSource coins;

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
     * @param coins Source of jointly generated random coins.
     * @param prg PRG used for batching homomorphisms that allow this.
     * @param challengeBitLength Number of bits in challenges.
     * @param batchBitLength Number of bits in each component when batching.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public IndependentGeneratorsI(String sid,
                                  Protocol protocol,
                                  CoinFlipPRingSource coins,
                                  PRG prg,
                                  int challengeBitLength,
                                  int batchBitLength,
                                  int statDist) {
        super(sid, protocol);
        this.coins = coins;
        this.prg = prg;
        this.challengeBitLength = challengeBitLength;
        this.batchBitLength = batchBitLength;
        this.statDist = statDist;
    }

    // Documented in IndependentGenerators.java

    public PGroupElementArray generate(Log log,
                                       PGroup pGroup,
                                       int numberOfGenerators) {

        log.info("Generate independent generators.");

        BiPRingPGroup biExp = new BiFixedBaseExp(pGroup, numberOfGenerators);
        HomPRingPGroup hom = biExp.restrict(pGroup.getg());

        PGroupElement generatorsContainer;

        Log tempLog = log.newChildLog();
        File file = getFile("IndependentGenerators");
        if (file.exists()) {

            tempLog.info("Independent generators exists on file.");

            try {

                ByteTreeReader btr = (new ByteTreeF(file)).getByteTreeReader();
                generatorsContainer = hom.getRange().toElement(btr);
                btr.close();

            } catch (ArithmFormatException afe) {
                throw new ProtocolError("Unable to read generators from file!",
                                        afe);
            }

        } else {

            // Make room for generator parts.
            PGroupElement[] generatorParts = new PGroupElement[k + 1];

            // Generate our generator parts.
            PRingElement exponents =
                hom.getDomain().randomElement(randomSource, statDist);

            generatorParts[j] = hom.map(exponents);

            // Publish our parts and read the other's.
            for (int l = 1; l <= k; l++) {

                if (l == j) {

                    tempLog.info("Publish generator parts.");
                    bullBoard.publish("GeneratorsPart",
                                      generatorParts[j].toByteTree(),
                                      tempLog);

                } else {

                    // Try to read and parse
                    tempLog.info("Read generator parts of " +
                                 ui.getDescrString(l) + ".");
                    boolean wellFormed = true;
                    ByteTreeReader reader = null;
                    try {

                        reader =
                            bullBoard.waitFor(l, "GeneratorsPart", tempLog);
                        generatorParts[l] = hom.getRange().toElement(reader);

                    } catch (ArithmFormatException afe) {
                        wellFormed = false;
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }

                    // If anything went wrong we set to unit elements.
                    if (!wellFormed) {

                        Log tempLog2 = tempLog.newChildLog();
                        tempLog2.info("Could not parse generator parts of " +
                                      ui.getDescrString(l) + ".");
                        tempLog2.info("Setting to unit elements.");
                        generatorParts[l] = hom.getRange().getONE();

                    }
                }
            }

            Challenger challenger = new ChallengerI(coins);

            // Execute mutual proof of knowledge of the exponents.
            SigmaProofSequential sigma =
                new SigmaProofSequential("SigmaProof",
                                         this,
                                         hom,
                                         pGroup.getg(),
                                         challenger,
                                         prg,
                                         challengeBitLength,
                                         batchBitLength,
                                         statDist);
            boolean[] verdicts =
                sigma.execute(tempLog, generatorParts, exponents, null);

            generatorsContainer = (APGroupElement)hom.getRange().getONE();

            // Compute product of the generator parts with accepting
            // sigma proofs.
            for (int l = 1; l <= k; l++) {
                if (verdicts[l]) {
                    generatorsContainer =
                        generatorsContainer.mul(generatorParts[l]);
                }
            }

            // Store the generators for later use.
            tempLog.info("Write independent generators to file.");
            file = getFile("IndependentGenerator");
            generatorsContainer.toByteTree().unsafeWriteTo(file);

        }

        return ((APGroupElement)generatorsContainer).getContent();
    }
}
