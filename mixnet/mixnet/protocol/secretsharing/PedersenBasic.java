
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

package mixnet.protocol.secretsharing;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.protocol.*;

/**
 * Implements the basic functionality of Pedersen's Verifiable Secret
 * Sharing (VSS) scheme generalized to an arbitrary homomorphism.
 *
 * <p>
 *
 * A prover calls {@link
 * #generateSharing(PFieldElement,PFieldElement)} and then repeatedly
 * {@link #computeShare(int)} to get the shares to be distributed to
 * the receivers.
 *
 * <p>
 *
 * A receiver calls {@link
 * #setPolynomialInExponent(PolynomialInExponent)}, {@link
 * #setShare(PRingElement)}, and {@link #verifyShare()}.
 *
 * <p>
 *
 * To recover a secret from a set of shares, the method {@link
 * #recover(int[],PRingElement[],int)} can be used.
 *
 * @author Douglas Wikstrom
 */
public class PedersenBasic implements PGroupAssociated {

    /**
     * Number of parties.
     */
    protected int k;

    /**
     * Threshold number of parties needed to recover the secret.
     */
    protected int t;

    /**
     * Index of this party.
     */
    protected int j;

    /**
     * Index of the dealer.
     */
    protected int l;

    /**
     * Underlying homomorphism.
     */
    protected HomPRingPGroup hom;

    /**
     * Underlying Shamir secret sharing.
     */
    protected ShamirBasic shamirBasic;

    /**
     * Polynomial in the exponents corresponding to the Shamir secret
     * sharing polynomial.
     */
    protected PolynomialInExponent polynomialInExponent;

    /**
     * Source of random bits used.
     */
    protected RandomSource randomSource;

    /**
     * Our share in this instance.
     */
    protected PRingElement share;

    /**
     * Different states an instance of this class can be in.
     */
    protected enum State {

        /**
         * Initial state after instantiation.
         */
        INITIAL,

        /**
         * State after a sharing polynomial has been generated.
         */
        SHARING_COMPUTED,

        /**
         * A receivers state after setting its share, after which
         * verification of the share is possible.
         */
        VERIFICATION_POSSIBLE
    };

    /**
     * Current state of this instance.
     */
    protected State state;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Creates an instance with index <code>j</code> expecting to be
     * run with <code>k-1</code> other parties where the
     * <code>l</code>th party is the dealer.
     *
     * @param k Number of parties executing the protocol.
     * @param t Threshold number of parties needed to recover the
     * secret.
     * @param j Index of this instance/party.
     * @param l Index of the dealer.
     * @param hom Underlying homomorphism.
     * @param randomSource Source of random bits.
     * @param statDist Decides statistical distance from the uniform
     * distribution.
     */
    public PedersenBasic(int k, int t, int j, int l, HomPRingPGroup hom,
                         RandomSource randomSource, int statDist) {
        this.state = State.INITIAL;
        this.k = k;
        this.t = t;
        this.j = j;
        this.l = l;

        this.hom = hom;

        this.shamirBasic = null;
        this.polynomialInExponent = null;
        this.share = null;
        this.statDist = statDist;

        this.randomSource = randomSource;
    }

    /**
     * Returns the underlying homomorphism.
     *
     * @return Underlying homomorphism of this instance.
     */
    public HomPRingPGroup getHom() {
        return hom;
    }

    /**
     * Creates an instance from a share and a polynomial in the
     * exponent such that the state of the instance is as if the
     * dealer correctly handed out the given share. This is useful
     * when "adding" or "factoring" instances.
     *
     * @param k Number of parties executing the protocol.
     * @param t Threshold number of parties needed to recover the
     * secret.
     * @param j Index of this instance/party.
     * @param l Index of the dealer.
     * @param hom Underlying homomorphism.
     * @param share Share of this instance.
     * @param polynomialInExponent Polynomial in the exponent used in
     * this instance.
     * @param statDist Decides statistical distance from the uniform
     * distribution.
     */
    protected PedersenBasic(int k, int t, int j, int l,
                            HomPRingPGroup hom,
                            PRingElement share,
                            PolynomialInExponent polynomialInExponent,
                            int statDist) {
        this(k, t, j, l, hom, null, statDist);
        if (!(hom.getDomain().equals(share.getPRing())
              && hom.getRange().equals(polynomialInExponent.getPGroup()))) {
            throw new ProtocolError("Incompatible groups and fields!");
        }
         this.share = share;
         this.polynomialInExponent = polynomialInExponent;

        this.state = State.VERIFICATION_POSSIBLE;
    }

    /**
     * Returns the instances corresponding to this one over the
     * factors of the underlying group and ring. This assumes that
     * factoring is possible.
     *
     * @return Instances corresponding to this one over the factors of
     * the underlying ring.
     */
    public PedersenBasic[] getFactors() {

        PolynomialInExponent[] polys = polynomialInExponent.getFactors();

        HomPRingPGroup[] homs = ((PHomPRingPGroup)hom).getFactors();
        PRingElement[] shares = ((PPRingElement)share).getFactors();

        PedersenBasic[] pedersens = new PedersenBasic[homs.length];
        for (int i = 0; i < pedersens.length; i++) {
            pedersens[i] = new PedersenBasic(k, t, j, l,
                                             polys[i].hom,
                                             shares[i],
                                             polys[i],
                                             statDist);
        }
        return pedersens;
    }

    /**
     * Creates a trivial instance such that the state of the instance
     * is as if the dealer correctly shared the unit element in the
     * domain of the homomorphism using constant polynomials (instead
     * of random). Thus, the share of each party is the unit ring
     * element and the instance allows verification. This is useful at
     * a higher abstraction layer to eliminate the influence of
     * corrupted dealers.
     *
     * @param k Number of parties executing the protocol.
     * @param t Threshold number of parties needed to recover the
     * secret.
     * @param j Index of this instance/party.
     * @param l Index of the dealer.
     * @param hom Underlying homomorphism.
     * @param statDist Decides statistical distance from the uniform
     * distribution.
     */
    protected PedersenBasic(int k, int t, int j, int l,
                            HomPRingPGroup hom,
                            int statDist) {
        this(k, t, j, l, hom, hom.getDomain().getONE(),
             new PolynomialInExponent(hom), statDist);
        if (j == l) {
            shamirBasic =
                new ShamirBasic(new Polynomial(hom.getDomain().getONE()));
        }
    }

    /**
     * Takes a representation of a state output by {@link
     * #stateToByteTree()} and moves the instance from the initial
     * state to one of the states {@link
     * State#SHARING_COMPUTED} and {@link
     * State#VERIFICATION_POSSIBLE}. The method {@link
     * #recover} can then be called.
     *
     * @param btr Representation of state.
     * @throws ProtocolFormatException If the input does not represent
     * a valid instance.
     */
    public void stateFromByteTree(ByteTreeReader btr)
        throws ProtocolFormatException {
        if (state == State.INITIAL) {
            String s = "Input does not represent an instance!";
            try {
                polynomialInExponent =
                    new PolynomialInExponent(hom, t - 1, btr.getNextChild());
                share = hom.getDomain().toElement(btr.getNextChild());

                if (j == l) {

                    shamirBasic =
                        new ShamirBasic(hom.getDomain(),
                                        t - 1,
                                        btr.getNextChild());
                    state = State.SHARING_COMPUTED;

                } else {

                    shamirBasic = null;
                    state = State.VERIFICATION_POSSIBLE;
                }
            } catch (ArithmFormatException afe) {
                throw new ProtocolFormatException(s, afe);
            } catch (EIOException eioe) {
                throw new ProtocolFormatException(s, eioe);
            }
        } else {
            throw new ProtocolError("Attempting to redefine contents "
                                    + "of instance!");
        }
    }

    /**
     * Takes a representation of a state output by {@link
     * #stateToByteTree()} and moves the instance from the initial
     * state to the represented state. The method {@link #recover} can
     * then be called. WARNING: This method assumes that the input is
     * correctly formatted.
     *
     * @param btr Representation of state.
     */
    public void unsafeStateFromByteTree(ByteTreeReader btr) {
        try {
            stateFromByteTree(btr);
        } catch (ProtocolFormatException pfe) {
            throw new ProtocolError("Fatal error!", pfe);
        }
    }

    /**
     * Returns a representation of the state of this instance that can
     * be input to {@link #stateFromByteTree(ByteTreeReader)}.
     *
     * @return Representation of state.
     */
    public ByteTreeContainer stateToByteTree() {
        if (state == State.VERIFICATION_POSSIBLE
            || state == State.SHARING_COMPUTED) {

            ByteTreeBasic btPIE = polynomialInExponent.toByteTree();

            if (j == l) {
                return new ByteTreeContainer(btPIE,
                                             share.toByteTree(),
                                             shamirBasic.toByteTree());
            } else {
                return new ByteTreeContainer(btPIE, share.toByteTree());
            }
        } else {
            throw new ProtocolError("Attempting to write incomplete "
                                    + "protocol execution to file!");
        }
    }

    /**
     * Adds the input to this instance.
     *
     * @param pb Instance added to this instance.
     * @return Sum of this instance and the input.
     */
    public PedersenBasic add(PedersenBasic pb) {
        if ((state == State.SHARING_COMPUTED
             || state == State.VERIFICATION_POSSIBLE)
            &&
            (pb.state == State.SHARING_COMPUTED
             || pb.state == State.VERIFICATION_POSSIBLE)) {

            return new PedersenBasic(k, t, j, l, hom,
                         share.add(pb.share),
                         polynomialInExponent.mul(pb.polynomialInExponent),
                         statDist);
        } else {
            throw new ProtocolError("Attempting to add incomplete instances!");
        }
    }

    /**
     * Compute a sharing of the secret given as input.
     *
     * @param secret Secret given as input.
     */
    public void generateSharing(PRingElement secret) {
        if (state == State.INITIAL) {

            if (!secret.getPRing().equals(hom.getDomain())) {
                throw new ProtocolError("Mismatching element!");
            }

            // Generate a Shamir secret sharing polynomial
            shamirBasic =
                new ShamirBasic(t - 1, secret, randomSource, statDist);

            // Compute corresponding polynomial "in the exponent"
            polynomialInExponent = new PolynomialInExponent(hom, shamirBasic);

            state = State.SHARING_COMPUTED;

            // Compute our own share
            share = computeShare(j);

        } else {
            throw new ProtocolError("Attempting to reuse instance!");
        }
    }

    /**
     * Computes and outputs a share of the secret.
     *
     * @param shareIndex Index of the share.
     * @return Share of party <code>shareIndex</code>.
     */
    public PRingElement computeShare(int shareIndex) {
        if (state == State.SHARING_COMPUTED) {
            return shamirBasic.evaluate(shareIndex);
        } else {
            throw new ProtocolError("No secret has been shared!");
        }
    }

    /**
     * Initializes the polynomial in exponent. This can only be done
     * once.
     *
     * @param polynomialInExponent Polynomial in exponent to use.
     * @throws ProtocolFormatException If the degree of the protocol
     * in the exponent is to large.
     * @see #getPolynomialInExponent
     */
    public void setPolynomialInExponent(
                           PolynomialInExponent polynomialInExponent)
    throws ProtocolFormatException {
        if (state == State.INITIAL && this.polynomialInExponent == null) {
            this.polynomialInExponent = polynomialInExponent;
            if (polynomialInExponent.getDegree() > t - 1) {
                throw new ProtocolFormatException("Too large degree!");
            }
            if (share != null) {
                state = State.VERIFICATION_POSSIBLE;
            }
        } else {
            String s = "Attempting to reuse instance or "
                + "change polynomial in exponent!";
            throw new ProtocolError(s);
        }
    }

    /**
     * Polynomials encoded in the exponent.
     *
     * @return Polynomial in exponent used.
     * @see #setPolynomialInExponent
     */
    public PolynomialInExponent getPolynomialInExponent() {
        if (polynomialInExponent != null) {
            return polynomialInExponent;
        } else {
            throw new ProtocolError("No secret has been shared!");
        }
    }

    /**
     * Initializes the share of this instance.
     *
     * @param share Share of this instance.
     * @see #getShare
     */
    public void setShare(PRingElement share) {
        if (state == State.INITIAL && this.share == null) {
            this.share = share;
            if (polynomialInExponent != null) {
                state = State.VERIFICATION_POSSIBLE;
            }
        } else {
            throw new ProtocolError("Attempting reuse instance or "
                                    + "to change share!");
        }
    }

    /**
     * Access method for the share of this instance.
     *
     * @return Share of this instance.
     * @see #setShare
     */
    public PRingElement getShare() {
        if (share == null) {
            throw new ProtocolError("Share is not initialized");
        }
        return share;
    }

    /**
     * Outputs true or false depending on if the <code>l</code>th
     * share given as input is correct or not.
     *
     * @param l Index of the share.
     * @param share Share of the secret.
     * @return <code>true</code> if the share is correct for party
     * <code>l</code> and <code>false</code> otherwise.
     */
    public boolean verifyShare(int l, PRingElement share) {
        if (state == State.VERIFICATION_POSSIBLE
            || state == State.SHARING_COMPUTED) {
            return polynomialInExponent.evaluate(l).equals(hom.map(share));
        } else {
            throw new ProtocolError("No checking data available!");
        }
    }

    /**
     * Verifies the share of this instance that has been set using
     * {@link #setShare(PRingElement)}.
     *
     * @return true if the share of this party is correct and false
     * otherwise.
     */
    public boolean verifyShare() {
        return verifyShare(j, getShare());
    }

    /**
     * Recovers a shared secret from a set of correct shares.
     *
     * @param indices Indices of the shares.
     * @param values Shares.
     * @param noShares Number of valid shares.
     * @return Secret shared by the dealer.
     */
    public static PRingElement recover(int[] indices,
                                       PRingElement[] values,
                                       int noShares) {
        return ShamirBasic.recover(indices, values, noShares);
    }

    // Documented in arithm.PGroupAssociated.java.

    public PGroup getPGroup() {
        return hom.getRange();
    }
}


