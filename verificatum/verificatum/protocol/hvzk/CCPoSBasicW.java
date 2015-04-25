
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
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.protocol.*;
import verificatum.ui.*;
import verificatum.util.*;

/**
 * Implements the basic functionality of Wikstrom's
 * commitment-consistent proof of a shuffle.
 *
 * <p>
 *
 * For clarity, each method is labeled BOTH, PROVER, or VERIFIER
 * depending on which parties normally call the method.
 *
 * @author Douglas Wikstrom
 */
public class CCPoSBasicW {

    /**
     * Size of the set that is permuted.
     */
    protected int size;

    /**
     * Bit length of the challenge.
     */
    protected int challengeBitLength;

    /**
     * Bit length of each element in the batching vector.
     */
    protected int batchBitLength;

    /**
     * Pseudo-random generator used to derive the random vector.
     */
    protected PRG prg;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Underlying group.
     */
    protected PGroup pGroup;

    /**
     * Ring associated with the commitment group.
     */
    protected PRing pRing;

    /**
     * Field associated with the ring.
     */
    protected PField pField;


    // ################### Instance and witness ###################

    /**
     * Standard generator of the group.
     */
    protected PGroupElement g;

    /**
     * Array of "independent" generators.
     */
    protected PGroupElementArray h;

    /**
     * Commitment of a permutation.
     */
    protected PGroupElementArray u;

    /**
     * Random exponents used to form the permutation commitment.
     */
    protected PRingElementArray r;

    /**
     * Permutation committed to.
     */
    protected Permutation pi;

    /**
     * Encryption homomorphism.
     */
    protected HomPRingPGroup hom;

    /**
     * Input ciphertexts.
     */
    protected PGroupElementArray w;

    /**
     * Output ciphertexts.
     */
    protected PGroupElementArray wp;

    /**
     * Random exponents used to form the output ciphertexts.
     */
    protected PRingElementArray s;


    // ################# Message 1 (verifier) #####################

    /**
     * Vector of random exponents.
     */
    protected PFieldElementArray e;


    // ################# Message 2 (prover) #######################

    /**
     * Proof commitment.
     */
    protected PGroupElement Ap;

    /**
     * Proof commitment.
     */
    protected PGroupElement Bp;


    // ########### Secret values for bridging commitment #######

    /**
     * Inversely permuted random vector. (This is denoted e' in the
     * comments below.)
     */
    protected PFieldElementArray ipe;


    // ######### Randomizers and blinders of the prover ########

    /**
     * Randomizer for inner product of r and e'.
     */
    protected PRingElement alpha;

    /**
     * Randomizer for inverse permuted batching vector.
     */
    protected PFieldElementArray epsilon;

    /**
     * Randomizer for inner product of s and e.
     */
    protected PRingElement beta;

    /**
     * Randomizer for inner product of s and e.
     */
    protected PPRingElement BETA;

    /**
     * Mapped randomizer for inner product of s and e.
     */
    protected PPGroupElement HOM_BETA;


    // ################## Message 3 (Verifier) ##################

    /**
     * Challenge from the verifier.
     */
    protected PFieldElement v;


    // ################## Message 4 (Prover) ##################

    /**
     * Reply for inner product of r and e'.
     */
    protected PRingElement k_A;

    /**
     * Reply inner product of s and e.
     */
    protected PRingElement k_B;

    /**
     * Reply inner product of s and e with public key part.
     */
    protected PRingElement K_B;

    /**
     * <code>K_B</code> mapped by the homomorphism.
     */
    protected PPGroupElement HOM_K_B;

    /**
     * Reply for the inverse permuted random vector.
     */
    protected PFieldElementArray k_E;

    /**
     * BOTH: Constructor to instantiate the protocol.
     *
     * @param challengeBitLength Bit length of the challenge.
     * @param batchBitLength Bit length of each component in random
     * vector.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public CCPoSBasicW(int challengeBitLength,
                       int batchBitLength,
                       int statDist,
                       PRG prg) {
        this.challengeBitLength = challengeBitLength;
        this.batchBitLength = batchBitLength;
        this.statDist = statDist;
        this.prg = prg;
    }

    /**
     * VERIFIER: Initializes the instance.
     *
     * @param g Standard generator used in permutation commitments.
     * @param h "Independent" generators used in permutation commitments.
     * @param u Permutation commitment.
     * @param hom Encryption homomorphism.
     * @param w List of ciphertexts.
     * @param wp List of ciphertexts.
     */
    public void setInstance(PGroupElement g,
                            PGroupElementArray h,
                            PGroupElementArray u,
                            HomPRingPGroup hom,
                            PGroupElementArray w,
                            PGroupElementArray wp) {
        this.g = g;
        this.h = h;
        this.u = u;

        this.hom = hom;
        this.w = w;
        this.wp = wp;

        this.r = null;
        this.pi = null;
        this.s = null;

        this.size = h.size();
        this.pGroup = g.getPGroup();
        this.pRing = pGroup.getPRing();
        this.pField = pRing.getPField();
    }

    /**
     * PROVER: Initializes the instance.
     *
     * @param g Standard generator used in permutation commitments.
     * @param h "Independent" generators used in permutation commitments.
     * @param u Permutation commitment.
     * @param hom Encryption homomorphism.
     * @param w List of ciphertexts.
     * @param wp List of ciphertexts.
     * @param r Random exponents used to form
     * the permutation commitment.
     * @param pi Permutation committed to.
     * @param s Random exponents used to process ciphertexts.
     */
    public void setInstance(PGroupElement g,
                            PGroupElementArray h,
                            PGroupElementArray u,
                            HomPRingPGroup hom,
                            PGroupElementArray w,
                            PGroupElementArray wp,
                            PRingElementArray r,
                            Permutation pi,
                            PRingElementArray s) {
        setInstance(g, h, u, hom, w, wp);
        this.r = r;
        this.pi = pi;
        this.s = s;
    }

    /**
     * BOTH: Extracts the random vector from a seed. This is useful
     * when the honest verifier is replaced by a coin tossing protocol
     * or when this protocol is used as a subprotocol.
     *
     * @param prgSeed Seed to the pseudorandom generator used to
     * extract the random vector.
     */
    public void setBatchVector(byte[] prgSeed) {
        prg.setSeed(prgSeed);
        LargeIntegerArray lia =
            LargeIntegerArray.random(size, batchBitLength, prg);
        this.e = pField.unsafeToElementArray(lia);
    }

    /**
     * PROVER: Generates the commitment of the prover.
     *
     * @param prgSeed Seed used to extract the random vector.
     * @param randomSource Source of random bits.
     * @return Representation of the commitments.
     */
    public ByteTreeBasic commit(byte[] prgSeed, RandomSource randomSource) {

        setBatchVector(prgSeed);

        // ################# Permuted Batching Vector #############

        ipe = e.permute(pi.inv());

        // ################# Proof Commitments ####################

        // During verification, the verifier computes:
        //
        // A = \prod u_i^{e_i}                                  (1)
        //
        // and requires that it equals:
        //
        // g^{<r,e'>} * \prod h_i^{e_i'}                        (2)
        //
        // We must show that we can open (1) as (2). For that purpose
        // we generate randomizers.

        alpha = pRing.randomElement(randomSource, statDist);


        // The bit length of each component of e' is bounded. Thus,
        // we can sample its randomizers as follows.

        int epsilonBitLength =
            batchBitLength + challengeBitLength + statDist;

        LargeIntegerArray epsilonIntegers =
            LargeIntegerArray.random(size, epsilonBitLength, randomSource);
        epsilon = pField.toElementArray(epsilonIntegers);
        epsilonIntegers.free();

        // Next we compute the corresponding blinder.

        Ap = g.exp(alpha).mul(h.expProd(epsilon));


        // We must show that we can open B = \prod w_i^{e_i} as
        //
        // B = \phi(-b)\prod (w_i')^{e_i'}
        //
        // where b=<s,e>, and \phi is the homomorphism defined by the
        // mapping of the second component of hom.
        BETA = (PPRingElement)hom.getDomain().randomElement(randomSource,
                                                            statDist);
        beta = ((APRingElement)BETA.project(1)).getContent().get(0);

        HOM_BETA = (PPGroupElement)hom.map(BETA.neg());
        PGroupElement hom_beta =
            ((APGroupElement)HOM_BETA.project(1)).getContent().get(0);

        Bp = hom_beta.mul(wp.expProd(epsilon));

        // ################### Byte tree ##########################

        return new ByteTreeContainer(Ap.toByteTree(), Bp.toByteTree());
    }

    /**
     * VERIFIER: Sets the commitment.
     *
     * @param btr Commitment from the prover.
     * @return Representation of the commitments.
     */
    public ByteTreeBasic setCommitment(ByteTreeReader btr) {

        PGroup ciphPGroup =
            ((APGroup)((PPGroup)hom.getRange()).project(1)).getContentPGroup();

        boolean malformed = false;
        try {

            Ap = pGroup.toElement(btr.getNextChild());
            Bp = ciphPGroup.toElement(btr.getNextChild());

        } catch (EIOException eioe) {
            malformed = true;
        } catch (ArithmFormatException afe) {
            malformed = true;
        }

        // If anything is malformed we set it to suitable
        // predetermined trivial value.
        if (malformed) {

            Ap = pGroup.getONE();
            Bp = ciphPGroup.getONE();
        }

        return new ByteTreeContainer(Ap.toByteTree(), Bp.toByteTree());
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
     * VERIFIER: Sets the challenge. This is useful if the challenge
     * is generated jointly.
     *
     * @param integerChallenge Challenge of verifier.
     * @throws ProtocolException If the challenge is not of the
     * expected form.
     */
    public void setChallenge(LargeInteger integerChallenge) {
        if (!(0 <= integerChallenge.compareTo(LargeInteger.ZERO)
              && integerChallenge.bitLength() <= challengeBitLength)) {
            throw new ProtocolError("Malformed challenge!");
        }
        this.v = pField.toElement(integerChallenge);
    }

    /**
     * Computes the reply of the prover to the given challenge, i.e.,
     * the second message of the prover.
     *
     * @param integerChallenge Challenge of verifier.
     * @return Reply of prover.
     */
    public ByteTreeBasic reply(LargeInteger integerChallenge) {

        setChallenge(integerChallenge);

        // Initialize the special exponents.
        PRingElement a = r.innerProduct(ipe);
        PRingElement b = s.innerProduct(e);

        // Compute the replies as:
        //
        // k_A = va + \alpha
        // k_B = vb + \beta
        // k_{E,i} = ve_i' + \epsilon_i
        //
        k_A = a.mulAdd(v, alpha);
        k_B = b.mulAdd(v, beta);
        k_E = (PFieldElementArray)ipe.mulAdd(v, epsilon);

        ByteTreeContainer reply = new ByteTreeContainer(k_A.toByteTree(),
                                                        k_B.toByteTree(),
                                                        k_E.toByteTree());
        return reply;
    }

    /**
     * VERIFIER: Verifies the reply of the prover and outputs true or
     * false depending on if the reply was accepted or not.
     *
     * @param btr Reply of the prover.
     * @return <code>true</code> if the reply is accepted and
     * <code>false</code> otherwise.
     */
    public boolean verify(ByteTreeReader btr) {

        PRing ciphPRing =
            ((APRing)((PPRing)hom.getDomain()).project(1)).getContentPRing();

        // Read and parse replies.
        try {

            k_A = pRing.toElement(btr.getNextChild());
            k_B = ciphPRing.toElement(btr.getNextChild());
            k_E = pField.toElementArray(size, btr.getNextChild());

        } catch (EIOException eio) {
            return false;
        } catch (ArithmFormatException afe) {
            return false;
        }

        // Compute A and B.
        PGroupElement A = u.expProd(e);
        PGroupElement B = w.expProd(e);

        // Assume prover makes us accept.
        boolean verdict = true;

        // Verify that prover knows a=<r,e'> and e' such that:
        //
        // A = \prod u_i^{e_i} = g^a * \prod h_i^{e_i'}
        //
        if (!A.expMul(v, Ap).equals(g.exp(k_A).mul(h.expProd(k_E)))) {
            verdict = false;
        }

        if (verdict) {

            // Verify that the prover knows b = <s,e> such that
            //
            // B = \prod w_i^{e_i} = \phi(-b)\prod (w_i')^{e_i'}
            //
            PPRing pPRing = (PPRing)hom.getDomain();
            PRing keyPRing = pPRing.project(0);
            APRing aPRing = (APRing)pPRing.project(1);

            PRingElementArray k_B_as_array =
                aPRing.getContentPRing().toElementArray(1, k_B);
            K_B = pPRing.product(keyPRing.getONE(),
                                 aPRing.toElement(k_B_as_array));

            HOM_K_B = (PPGroupElement)hom.map(K_B.neg());
            PGroupElement hom_k_B =
                ((APGroupElement)HOM_K_B.project(1)).getContent().get(0);

            if (!B.expMul(v, Bp).equals(hom_k_B.mul(wp.expProd(k_E)))) {
                verdict = false;
            }
        }

        return verdict;
    }

    /**
     * VERIFIER: Returns the reply that must already have been
     * processed.
     *
     * @return Reply processed by the verifier.
     */
    public ByteTreeBasic getReply() {
        return new ByteTreeContainer(k_A.toByteTree(),
                                     k_B.toByteTree(),
                                     k_E.toByteTree());
    }

    public void free() {

        if (e != null) {
            e.free();
        }
        if (ipe != null) {
            ipe.free();
        }
        if (epsilon != null) {
            epsilon.free();
        }
        if (k_E != null) {
            k_E.free();
        }
        if (K_B != null) {
            K_B.free();
        }
        if (HOM_K_B != null) {
            HOM_K_B.free();
        }
        if (BETA != null) {
            BETA.free();
        }
        if (HOM_BETA != null) {
            HOM_BETA.free();
        }
    }
}
