
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

package verificatum.protocol.coinflip;

import java.io.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.ui.*;
import verificatum.protocol.*;
import verificatum.protocol.secretsharing.*;

/**
 * Implements a coin-flipping protocol using Pedersen verifiable
 * secret sharing, i.e., each party verifiably shares a random value
 * over a ring. Then all random secrets are recovered and the random
 * coin is defined as the sum of these secrets. This is essentially a
 * wrapper class for {@link PedersenSequential} using the
 * exponentiated product map {@link BiExpProd}.
 *
 * <p>
 *
 * For efficiency, the (correct) secret sharings are added up in the
 * natural way into a single joint Pedersen sharing before
 * recovery. The result of the method {@link #prepareCoin(Log)} is
 * that each party shares a random value and the resulting shares are
 * added up locally at each party. Later, the user can call {@link
 * #getCoin(Log)} to actually get the random coin that was prepared.
 *
 * @author Douglas Wikstrom
 */
public class CoinFlipPRing extends Protocol {

    /**
     * Instance representing the prepared coin after several
     * instances have been collapsed.
     */
    protected Pedersen pedersen;

    /**
     * Independent generator.
     */
    protected PGroupElement h;

    /**
     * Public keys used for communication.
     */
    protected CryptoPKey[] pkeys;

    /**
     * Secret key of this instance.
     */
    protected CryptoSKey skey;

    /**
     * Different states an instance of this class can be in.
     */
    protected enum State {
        /**
         * State directly after creation.
         */
        INITIAL,

        /**
         * State after coin is prepared.
         */
        COIN_PREPARED,

        /**
         * After coin has been collected.
         */
        COIN_COLLECTED
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
     * Number of this coin.
     */
    protected int coinNumber;

    /**
     * Parent protocol.
     */
    protected Protocol parentProtocol;

    /**
     * Generates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param coinNumber Number of this coin.
     * @param h Independent generator.
     * @param pkeys Plain public keys.
     * @param skey Plain secret key.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public CoinFlipPRing(String sid,
                         Protocol protocol,
                         int coinNumber,
                         PGroupElement h,
                         CryptoPKey[] pkeys,
                         CryptoSKey skey,
                         int statDist) {
        super(sid, protocol);

        // We need the parent to collapse instances.
        this.parentProtocol = protocol;

        this.coinNumber = coinNumber;
        this.h = h;
        this.pkeys = pkeys;
        this.skey = skey;
        this.statDist = statDist;
        state = State.INITIAL;
    }

    /**
     * Generates an instance that allows recovering a coin. This is
     * used to implement factoring of an instance.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param pedersen Underlying Pedersen instance.
     * @param coinNumber Number of this coin.
     */
    protected CoinFlipPRing(String sid, Protocol protocol, Pedersen pedersen,
                            int coinNumber) {
        super(sid, protocol);
        this.pedersen = pedersen;
        this.coinNumber = coinNumber;
        state = State.COIN_PREPARED;
    }

    /**
     * Returns the factors of this instance. This assumes that
     * factoring is possible.
     *
     * @param log Logging context.
     * @return Factors of this instance.
     */
    public CoinFlipPRing[] getFactors(Log log) {
        if (state == State.INITIAL) {
            throw new ProtocolError("Unable to factor instance in " +
                                    "initial state!");
        }
        if (!(h instanceof PPGroupElement)) {
            throw new ProtocolError("Can not factor over prime order group!");
        }

        Pedersen[] pedersens = pedersen.getFactors(log);

        CoinFlipPRing[] coinflips = new CoinFlipPRing[pedersens.length];
        for (int i = 0; i < pedersens.length; i++) {

            String tsid = String.format("%03d", (coinNumber + i));
            coinflips[i] = new CoinFlipPRing(tsid,
                                             parentProtocol,
                                             pedersens[i],
                                             coinNumber + i);
        }
        return coinflips;
    }

    /**
     * Prepares a random coin to be revealed later.
     *
     * @param log Logging context.
     */
    public void prepareCoin(Log log) {

        log.info("Prepare joint coin.");
        Log tempLog = log.newChildLog();

        if (state != State.INITIAL) {
            throw new ProtocolError("Attempting to re-prepare coin!");
        }

        // Set up homomorphism.
        PGroupElement g = h.getPGroup().getg();

        HomPRingPGroup hom = null;
        if (h instanceof PPGroupElement) {

            // We need to make sure that we can later factor this
            // instance if it was executed over a product group.
            PGroupElement[] hs = ((PPGroupElement)h).getFactors();
            PGroupElement[] gs = ((PPGroupElement)g).getFactors();

            HomPRingPGroup[] homs = new HomPRingPGroup[hs.length];
            for (int i = 0; i < homs.length; i++) {

                BiPRingPGroup biExpProd = new BiExpProd(hs[i].getPGroup(), 2);
                PPGroup domain = (PPGroup)biExpProd.getPGroupDomain();
                PGroupElement restriction = domain.product(gs[i], hs[i]);
                homs[i] = biExpProd.restrict(restriction);

            }
            hom = new PHomPRingPGroup(homs);

        } else {

            BiPRingPGroup biExpProd = new BiExpProd(h.getPGroup(), 2);
            PGroupElement restriction =
                ((PPGroup)biExpProd.getPGroupDomain()).product(g, h);
            hom = biExpProd.restrict(restriction);

        }

        if (readBoolean("State")) {

            pedersen = new Pedersen("Collapsed_" + sid,
                                    this,
                                    0,
                                    hom,
                                    pkeys,
                                    skey,
                                    statDist,
                                    true);
            pedersen.receiveShare(tempLog);

        } else {

            PedersenSequential pedersenSequential =
                new PedersenSequential(sid,
                                       this,
                                       hom,
                                       pkeys,
                                       skey,
                                       statDist,
                                       false);
            PRingElement secret =
                hom.getDomain().randomElement(randomSource, statDist);
            pedersenSequential.execute(tempLog, secret);

            pedersen =
                pedersenSequential.collapse("Collapsed_" + sid, this, true,
                                            tempLog);
            writeBoolean("State");
        }

        state = State.COIN_PREPARED;
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the coin of this instance has already been used or not.
     *
     * @return Usage status of the coin of this instance.
     */
    public boolean used() {
        File file = getFile("Used");
        return file.exists();
    }

    /**
     * Outputs a prepared coin.
     *
     * @param log Logging context.
     * @return Random coin.
     */
    public PRingElement getCoin(Log log) {

        // We make sure that this coin can never be reused no matter
        // what the calling protocol does.
        if (used()) {
            throw new ProtocolError("Attempting to reuse coin!");
        }

        log.info("Collect previously prepared joint coin (" + sid + ").");
        Log tempLog = log.newChildLog();

        if (state != State.COIN_PREPARED) {
            throw new ProtocolError("No coin has been prepared!");
        }

        PPRingElement share = (PPRingElement)pedersen.recover(tempLog);

        state = State.COIN_COLLECTED;

        File file = getFile("Used");
        try {
            file.createNewFile();
        } catch (IOException ioe) {
            throw new ProtocolError("Unable to create used file!");
        }

        return share.project(0);
    }
}
