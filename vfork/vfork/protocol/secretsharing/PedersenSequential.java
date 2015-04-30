
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

package vfork.protocol.secretsharing;

import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.ui.*;
import vfork.protocol.*;

/**
 * Implements a container class of several {@link Pedersen}
 * instances. This is used in symmetric settings, where each party
 * plays the role of the dealer once in sequential order.
 *
 * @author Douglas Wikstrom
 */
public class PedersenSequential extends Protocol {

    /**
     * Underlying homomorphisms.
     */
    protected HomPRingPGroup[] homs;

    /**
     * Instances of the Pedersen VSS protocol run as subprotocols by
     * this instance.
     */
    protected Pedersen[] pedersen;

    /**
     * List of eliminated parties.
     */
    protected boolean[] eliminated;

    /**
     * Public keys used for communication.
     */
    protected CryptoPKey[] pkeys;

    /**
     * Secret key of this instance.
     */
    protected CryptoSKey skey;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Indicates if this instance can be factored or not.
     */
    protected boolean factorizable;

    /**
     * Generates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param hom Underlying homomorphism.
     * @param pkeys Plain public keys.
     * @param skey Plain secret key.
     * @param storeToFile Should be true if this instance should store
     * itself to file, and false otherwise.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public PedersenSequential(String sid,
                              Protocol protocol,
                              HomPRingPGroup hom,
                              CryptoPKey[] pkeys,
                              CryptoSKey skey,
                              int statDist,
                              boolean storeToFile) {
        this(sid, protocol, generateHoms(hom, pkeys.length), pkeys, skey,
             statDist, storeToFile);
    }

    /**
     * Creates an array of the given size populated with the given
     * homomorphism.
     *
     * @param hom Underlying homomorphism.
     * @param size Number of copies of the homomorphism.
     * @return Populated array.
     */
    private static HomPRingPGroup[] generateHoms(HomPRingPGroup hom, int size) {
        HomPRingPGroup[] tmpHoms = new HomPRingPGroup[size];
        Arrays.fill(tmpHoms, hom);
        return tmpHoms;
    }

    /**
     * Generates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param homs Underlying homomorphisms (this must be indexed from one).
     * @param pkeys Plain public keys.
     * @param skey Plain secret key.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @param storeToFile Should be true if this instance should store
     * itself to file, and false otherwise.
     */
    public PedersenSequential(String sid,
                              Protocol protocol,
                              HomPRingPGroup[] homs,
                              CryptoPKey[] pkeys,
                              CryptoSKey skey,
                              int statDist,
                              boolean storeToFile) {
        super(sid, protocol);
        this.homs = homs;
        this.skey = skey;
        this.pkeys = pkeys;
        this.statDist = statDist;

        // Make room for k instances of Pedersen VSS indexed from one.
        pedersen = new Pedersen[k + 1];

        // Make room for k booleans representing which parties either
        // complained or were pointed out as corrupted, i.e., player
        // elimination.
        eliminated = new boolean[k + 1];
        Arrays.fill(eliminated, false);

        // Each party executes Pedersen as dealer once using its own
        // homomorphism.
        for(int l = 1; l <= k; l++) {

            // Create an instance of Pedersen VSS with a unique SID.
            pedersen[l] = new Pedersen(Integer.toString(l),
                                       this,
                                       l,
                                       homs[l],
                                       pkeys,
                                       skey,
                                       statDist,
                                       storeToFile);
        }
    }

    /**
     * Generates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param hom Underlying homomorphism (this must be indexed from one).
     * @param pkeys Plain public keys.
     * @param skey Plain secret key.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @param storeToFile Should be true if this instance should store
     * itself to file, and false otherwise.
     * @param pedersen Underlying instances of {@link Pedersen}.
     * @param eliminated List of eliminated parties.
     */
    public PedersenSequential(String sid,
                              Protocol protocol,
                              HomPRingPGroup hom,
                              CryptoPKey[] pkeys,
                              CryptoSKey skey,
                              int statDist,
                              boolean storeToFile,
                              Pedersen[] pedersen,
                              boolean[] eliminated) {
        this(sid, protocol, hom, pkeys, skey, statDist, storeToFile);
        this.pedersen = Arrays.copyOfRange(pedersen, 0, pedersen.length);
        this.eliminated = Arrays.copyOfRange(eliminated, 0, pedersen.length);
    }

    /**
     * Generates an instance of the protocol that does not store
     * itself to file.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param hom Underlying homomorphism.
     * @param pkeys Plain public keys.
     * @param skey Plain secret key.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public PedersenSequential(String sid,
                              Protocol protocol,
                              HomPRingPGroup hom,
                              CryptoPKey[] pkeys,
                              CryptoSKey skey,
                              int statDist) {
        this(sid, protocol, hom, pkeys, skey, statDist, false);
    }

    /**
     * Executes <code>k</code> copies of <code>Pedersen</code> where
     * each party sequentially plays the role of the dealer.
     *
     * @param log Logging context.
     * @param secret Secret we share.
     */
    public void execute(Log log, PRingElement secret) {

        log.info("Execute Pedersen " + k + " times with rotating dealer.");
        Log tempLog = log.newChildLog();

        // Each party executes Pedersen as dealer once.
        for(int l = 1; l <= k; l++) {

             // We play the role of the dealer.
             if (l == j) {

                 pedersen[l].dealSecret(tempLog, secret);
                 eliminated[l] = false;

             // We play the role of a verifier.
             } else {

                 eliminated[l] = !pedersen[l].receiveShare(tempLog);

            }
        }

        // All eliminated parties are assigned trivial sharings.
        for (int i = 1; i <= k; i++) {
            if (eliminated[i]) {
                pedersen[i].setTrivial(tempLog);
            }
        }
    }

    /**
     * Executes <code>k</code> copies of <code>Pedersen</code> where
     * each party sequentially plays the role of the dealer with
     * random shared secrets.
     *
     * @param log Logging context.
     */
    public void execute(Log log) {
        execute(log, homs[j].getDomain().randomElement(randomSource, statDist));
    }

    /**
     * Returns <code>true</code> if the party with the input index has
     * been eliminated.
     *
     * @param l Index of the party.
     * @return <code>true</code> if party <code>l</code> is eliminated
     * and <code>false</code> otherwise.
     */
    public boolean isEliminated(int l) {
        return eliminated[l];
    }

    /**
     * Collapses all the subprotocols of this instance into a single
     * Pedersen VSS instance considered to be shared correctly by a
     * non-existent party 0. This gives a simpler and more efficient
     * recovery protocol, when the recovered secret values are to be
     * added anyway after recovery.
     *
     * @param sid Session identifier of created <code>Pedersen</code>
     * instance.
     * @param virtualParent Protocol which invokes this one.
     * @param storeToFile Should be true if this instance should store
     * itself to file, and false otherwise.
     * @param log Logging context.
     *
     * @return Single instance corresponding to the collapse of the
     * individual instances.
     */
    public Pedersen collapse(String sid, Protocol virtualParent,
                             boolean storeToFile, Log log) {
        PedersenBasic result = pedersen[1].pedersenBasic;

        for (int i = 2; i <= k; i++) {
            result = result.add(pedersen[i].pedersenBasic);
        }

        // Resulting sharing is considered as correctly dealt by a
        // non-existent Party 0.
        result.l = 0;
        Pedersen pedersen =
            new Pedersen(sid, virtualParent, 0, pkeys, skey, result,
                         statDist, storeToFile, log);
        return pedersen;
    }

    /**
     * Recovers and returns the secret shared by player <code>l</code>.
     *
     * @param log Logging context.
     * @param l Index of party of which the secret is recovered.
     * @return Shared secret of party <code>l</code>.
     */
    public PRingElement recover(Log log, int l) {
        return pedersen[l].recover(log);
    }

    /**
     * Returns the checking element corresponding to the constant
     * coefficient of the polynomial in the exponent of the given
     * party. This turns out to be one of the interesting elements in
     * many applications, where the secret is never recovered.
     *
     * @param log Logging context.
     * @param l Index of the party.
     * @return Constant element of party <code>l</code>.
     */
    public PGroupElement getConstantElement(Log log, int l) {
        return pedersen[l].getConstCoeffElement(log);
    }

    /**
     * Returns the product of the checking elements of the constant
     * coefficients of the polynomial in the exponent of all
     * parties. This turns out to be the most interesting element in
     * many applications, where the secret is never recovered, e.g.,
     * distributed key generation.
     *
     * @param log Logging context.
     * @return Product of constant checking elements of all parties.
     */
    public PGroupElement getConstantElementProduct(Log log) {

        // We compute the group element as the product of the constant
        // checking element of all Pedersen VSS instances.
        PGroupElement result = pedersen[1].getConstCoeffElement(log);
        for (int l = 2; l <= k; l++) {
            result = result.mul(pedersen[l].getConstCoeffElement(log));
        }
        return result;
    }
}
