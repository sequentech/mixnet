
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

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.protocol.*;

/**
 * Generic sigma protocol for proving knowledge of a preimage of a
 * homomorphism as implemented in {@link HomPRingPGroup}.
 *
 * @author Douglas Wikstrom
 */
public class SigmaProofBasic {

    /**
     * Homomorphism.
     */
    protected HomPRingPGroup hom;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Bit-size of the challenge.
     */
    protected int challengeBitLength;

    /**
     * Common input to both prover and verifier.
     */
    protected PGroupElement commonInput;

    /**
     * Private input to prover.
     */
    protected PRingElement privateInput;

    /**
     * Randomizer used by prover.
     */
    protected PRingElement randomizer;

    /**
     * Commitment from prover.
     */
    protected PGroupElement commitment;

    /**
     * Challenge of verifier.
     */
    protected PFieldElement challenge;

    /**
     * Reply from prover.
     */
    protected PRingElement reply;

    /**
     * Indicates that this instance has been batched.
     */
    protected boolean batched;

    /**
     * Initializes the sigma proof.
     *
     * @param hom Homomorphism.
     * @param challengeBitLength Number of bits in challenges.
     * @param statDist Decides the statistical distance.
     */
    public SigmaProofBasic(HomPRingPGroup hom,
                           int challengeBitLength,
                           int statDist) {
        this.hom = hom;
        this.challengeBitLength = challengeBitLength;
        this.statDist = statDist;
    }

    /**
     * Initializes the instance for use by the prover.
     *
     * @param commonInput Common input, i.e., the image.
     * @param privateInput Private input, i.e., the preimage.
     */
    public void setInstance(PGroupElement commonInput,
                            PRingElement privateInput) {
        this.commonInput = commonInput;
        this.privateInput = privateInput;
    }

    /**
     * Initializes the instance for use by the verifier.
     *
     * @param commonInput Common input, i.e., the image.
     */
    public void setInstance(PGroupElement commonInput) {
        setInstance(commonInput, null);
    }

    /**
     * Replaces the instance by a batched version that is more
     * efficient to process. This assumes that the homomorphism is
     * batchable.
     *
     * @param prg Pseudo-random generator used for batching.
     * @param batchBitLength Bit-size of components when batching.
     */
    public void batch(PRG prg, int batchBitLength) {
        Batchable bhom = (Batchable)hom;

        PFieldElementArray batchingExps =
            bhom.initBatching(prg, batchBitLength);

        if (privateInput != null) {
            privateInput = bhom.batchedPreimage(privateInput);
        }
        commonInput = bhom.batchedImage(commonInput);
        hom = bhom.batchedMap();
        batchingExps.free();

        batched = true;
    }

    /**
     * Generates the commitment of the prover, i.e., the first message
     * of the prover.
     *
     * @param rs Source of randomness.
     * @return Commitment of prover.
     */
    public ByteTreeBasic commit(RandomSource rs) {
        if (privateInput == null) {
            throw new ProtocolError("Verifier attempting to be prover!");
        }
        randomizer = hom.getDomain().randomElement(rs, statDist);
        commitment = hom.map(randomizer);

        return commitment.toByteTree();
    }

    /**
     * Generates the challenge of the verifier, i.e., the only message
     * of the verifier.
     *
     * @param commitmentReader Commitment of prover.
     * @param rs Source of randomness.
     * @return Challenge of verifier.
     */
    public LargeInteger challenge(ByteTreeReader commitmentReader,
                                  RandomSource rs) {
        if (privateInput != null) {
            throw new ProtocolError("Prover attempting to be verifier!");
        }
        try {
            this.commitment = hom.getRange().toElement(commitmentReader);
        } catch (ArithmFormatException afe) {
            this.commitment = hom.getRange().getONE();
        }
        LargeInteger integerChallenge =
            new LargeInteger(challengeBitLength, rs);
        this.challenge =
            hom.getRange().getPRing().getPField().toElement(integerChallenge);

        return integerChallenge;
    }

    /**
     * Sets the commitment of the prover.
     *
     * @param commitmentReader Commitment of prover.
     * @return Commitment as group element.
     */
    public ByteTreeBasic setCommitment(ByteTreeReader commitmentReader) {
        try {
            commitment = hom.getRange().toElement(commitmentReader);
        } catch (ArithmFormatException afe) {
            commitment = hom.getRange().getONE();
        }

        return commitment.toByteTree();
    }

    /**
     * Returns the bit length of challenges.
     *
     * @return Bit length of challenge.
     */
    public int getChallengeBitLength() {
        return challengeBitLength;
    }

    /**
     * Sets the challenge of the verifier, i.e., the only message of
     * the verifier.
     *
     * @param integerChallenge Challenge of verifier.
     */
    public void setChallenge(LargeInteger integerChallenge) {
        if (integerChallenge.compareTo(LargeInteger.ZERO) < 0) {
            throw new ProtocolError("Negative challenge!");
        }
        if (integerChallenge.bitLength() > challengeBitLength) {
            throw new ProtocolError("Too many bits in challenge!");
        }
        this.challenge =
            hom.getRange().getPRing().getPField().toElement(integerChallenge);
    }

    /**
     * Computes the reply of the prover to the given challenge, i.e.,
     * the second message of the prover.
     *
     * @param integerChallenge Challenge of verifier.
     * @return Reply of prover.
     */
    public ByteTreeBasic reply(LargeInteger integerChallenge) {
        if (privateInput == null) {
            throw new ProtocolError("Verifier attempting to be prover!");
        }
        setChallenge(integerChallenge);

        PRingElement reply = privateInput.mulAdd(challenge, randomizer);
        return reply.toByteTree();
    }

    /**
     * Computes the verdict of the verifier to the commitment,
     * challenge, and the given reply.
     *
     * @param replyReader Reply of prover.
     * @return Verdict of verifier.
     */
    public boolean verify(ByteTreeReader replyReader) {
        if (privateInput != null) {
            throw new ProtocolError("Prover attempting to be verifier!");
        }
        try {
            this.reply = hom.getDomain().toElement(replyReader);
        } catch (ArithmFormatException afe) {
            this.reply = hom.getDomain().getONE();
        }
        PGroupElement left = commonInput.expMul(challenge, commitment);
        PGroupElement right = hom.map(reply);
        boolean verdict = left.equals(right);

        left.free();
        right.free();

        return verdict;
    }

    /**
     * Returnst the reply of the prover.
     *
     * @return Reply of the prover.
     */
    public ByteTreeBasic getReply() {
        return reply.toByteTree();
    }

    public void free() {
        if (batched) {
            if (privateInput != null) {
                privateInput.free();
            }
            commonInput.free();
            hom.free();
        }
        if (randomizer != null) {
            randomizer.free();
        }
        if (commitment != null) {
            commitment.free();
        }
        if (reply != null) {
            reply.free();
        }
    }
}
