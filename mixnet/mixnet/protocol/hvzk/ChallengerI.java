
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
 * Container class for a coin-flipping functionality used to generate
 * challenges for public coin protocols.
 *
 * @author Douglas Wikstrom
 */
public class ChallengerI implements Challenger {

    /**
     * Source of jointly generated random coins.
     */
    protected CoinFlipPRingSource coins;

    /**
     * Creates an instance which generates challenges using the given
     * source of jointly generated random coins.
     *
     * @param coins Source of jointly generated random coins.
     */
    public ChallengerI(CoinFlipPRingSource coins) {
        this.coins = coins;
    }


    // Documented in Challenger.java

    public byte[] challenge(Log log,
                            ByteTreeBasic data,
                            int challengeBitLength,
                            int statDist) {

        log.info("Generate bits jointly.");
        Log tempLog = log.newChildLog();

        return coins.getCoinBytes(tempLog, challengeBitLength, statDist);
    }
}
