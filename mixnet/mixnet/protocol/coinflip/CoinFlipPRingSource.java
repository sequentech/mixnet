
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

package mixnet.protocol.coinflip;

import java.io.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;
import mixnet.protocol.*;
import mixnet.protocol.secretsharing.*;

/**
 * This is a container class of {@link CoinFlipPRing} that simplifies
 * the handling of jointly generated random "coins". The user may
 * request a random coin by simply calling {@link #getCoin(Log)},
 * provided that all parties agree to do this at the same time of
 * course. The method {@link #prepareCoins(Log,int)} can be used to
 * perform most of the necessary communication and computations in
 * advance for a given number of coins. The prepared coins can then be
 * output quickly with limited communication.
 *
 * <p>
 *
 * This class assumes that all parties agree both on how many coins
 * are precomputed and on the order the coins are used, i.e., the
 * coins are queued and output in the order they are generated. The
 * programmer is responsible for keeping the (honest) parties in
 * synchronization.
 *
 * @author Douglas Wikstrom
 */
public class CoinFlipPRingSource extends Protocol {

    /**
     * An "independent" generator.
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
     * A list of the prepared "coins".
     */
    protected LinkedList<CoinFlipPRing> preparedCoins;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Generates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param h An "independent" generator.
     * @param pkeys Plain public keys.
     * @param skey Plain secret key.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public CoinFlipPRingSource(String sid,
                               Protocol protocol,
                               PGroupElement h,
                               CryptoPKey[] pkeys,
                               CryptoSKey skey,
                               int statDist) {
        super(sid, protocol);
        this.h = h;
        this.skey = skey;
        this.pkeys = pkeys;
        this.statDist = statDist;

        preparedCoins = new LinkedList<CoinFlipPRing>();
    }

    /**
     * Reads prepared coins from file. It is the responsibility of the
     * programmer to only call this method once.
     *
     * @param log Logging context.
     */
    public void readPrepared(Log log) {

        log.info("Read prepared coins.");
        Log tempLog = log.newChildLog();

        int counter = readCounter("Counter", log);

        for (int i = 0; i < counter; i++) {

            CoinFlipPRing cfpf = new CoinFlipPRing("" + i,
                                                   this,
                                                   i,
                                                   h,
                                                   pkeys,
                                                   skey,
                                                   statDist);
            if (!cfpf.used()) {
                cfpf.prepareCoin(tempLog);
                preparedCoins.add(cfpf);
            }
        }
    }

    /**
     * Prepares the given number of additional coins for later
     * use. Although it is not necessary to call this method at all
     * before calling {@link #getCoin(Log)} it allows a protocol to
     * pre-process the generation of random coins.
     *
     * @param log Logging context.
     * @param noCoins Number of coins to prepare.
     */
    public void prepareCoins(Log log, int noCoins) {

        log.info("Generate " + noCoins + " coins.");
        Log tempLog = log.newChildLog();

        int counter = readCounter("Counter", log);

        PPGroup pPGroup = new PPGroup(h.getPGroup(), noCoins);

        String tsid = String.format("Parent%03d", counter);
        CoinFlipPRing cfpf =
            new CoinFlipPRing(tsid,
                              this,
                              counter,
                              pPGroup.product(h),
                              pkeys,
                              skey,
                              statDist);
        cfpf.prepareCoin(tempLog);

        CoinFlipPRing[] cfpfs = cfpf.getFactors(tempLog);
        for (int i = 0; i < cfpfs.length; i++) {
            preparedCoins.add(cfpfs[i]);
        }
        writeCounter("Counter", counter + noCoins);
    }

    /**
     * Returns a random coin. It is a good idea to prepare coins in
     * advance using {@link #prepareCoins(Log,int)} to improve the online
     * complexity of protocols.
     *
     * @param log Logging context.
     * @return Jointly generated random coin.
     */
    public PRingElement getCoin(Log log) {

        // If no coins are ready we prepare one
        if (preparedCoins.size() == 0) {
            prepareCoins(log, 1);
        }

        CoinFlipPRing cfpf = preparedCoins.remove();

        log.info("Collect coin.");
        Log tempLog = log.newChildLog();

        return cfpf.getCoin(tempLog);
    }

    /**
     * Generates a random coin with a given number of bits. It is a
     * good idea to prepare coins in advance using {@link
     * #prepareCoins(Log,int)}.
     *
     * @param log Logging context.
     * @param bitsRequested Number of bits in coin.
     * @param statDist Decides statistical distance from the uniform
     * distribution.
     * @return Jointly generated random coin.
     */
    public LargeInteger getCoin(Log log, int bitsRequested, int statDist) {
        return LargeInteger.toPositive(getCoinBytes(log, bitsRequested,
                                                    statDist));
    }

    /**
     * Generates a random coin with a given number of bits. It is a
     * good idea to prepare coins in advance using {@link
     * #prepareCoins(Log,int)} to improve the online complexity of
     * protocols, since generating random coins is expensive. If the
     * number of requested bits <i>n</i> is not a multiple of 8, then
     * the <i>8-(n mod 8)</i> most significant bits of the first
     * output byte are set to zero.
     *
     * @param log Logging context.
     * @param bitsRequested Number of bits in coin.
     * @param statDist Decides statistical distance from the uniform
     * distribution.
     * @return Jointly generated random coin.
     */
    public byte[] getCoinBytes(Log log, int bitsRequested, int statDist) {

        log.info("Generate " + bitsRequested + " random bits.");
        Log tempLog = log.newChildLog();

        int bytesRequested = (bitsRequested + 7) / 8;

        byte[] result = new byte[bytesRequested];

        // Ensure statistical distance.
        int offset = (statDist + 7) / 8;
        int coinLen = h.getPGroup().getPRing().getByteLength();
        int index = 0;

        // Here we may end up throwing away quite a few random bits,
        // but this is unavoidable, since we can not delay the
        // recovery of part of a ring element (from which the random
        // bits are derived).
        while (index < bytesRequested) {
            byte[] coins = unpack(getCoin(tempLog), offset);

            int len = Math.min(result.length - index, coins.length - offset);

            System.arraycopy(coins, offset, result, index, len);
            index += len;
        }
        int z = 8 - (bitsRequested % 8);
        if (z > 0) {
            result[0] &= (0xFF >>> z);
        }
        return result;
    }

    /**
     * Returns bytes extracted from the input element.
     *
     * @param el Element to be turned into bytes.
     * @param offset Number of leading bytes to eliminate.
     * @return Extracted bytes.
     */
    protected static byte[] unpack(PRingElement el, int offset) {
        if (el instanceof PFieldElement) {

            byte[] bytes = ((PFieldElement)el).toByteArray();
            return Arrays.copyOfRange(bytes, offset, bytes.length);

        } else {

            PRingElement[] els = ((PPRingElement)el).getFactors();
            ArrayList<byte[]> list = new ArrayList<byte[]>();
            for (int i = 0; i < els.length; i++) {
                list.add(unpack(els[i], offset));
            }
            int total = 0;
            for (byte[] bytes : list) {
                total += bytes.length;
            }
            byte[] result = new byte[total];
            int index = 0;
            for (byte[] bytes : list) {
                System.arraycopy(bytes, 0, result, index, bytes.length);
                index += bytes.length;
            }
            return result;

        }
    }
}
