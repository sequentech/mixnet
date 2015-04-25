
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
import verificatum.protocol.coinflip.*;
import verificatum.protocol.demo.*;
import verificatum.protocol.distrkeygen.*;
import verificatum.ui.*;
import verificatum.ui.info.*;
import verificatum.ui.opt.*;

/**
 * Factory for instances implementing {@link CCPoS}.
 *
 * @author Douglas Wikstrom
 */
public class CCPoSFactory {

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
     * Creates a factory that constructs instances of {@link CCPoS}.
     *
     * @param challenger Source of challenges.
     * @param prg PRG used for batching homomorphisms that allow this.
     * @param challengeBitLength Number of bits in challenges.
     * @param batchBitLength Number of bits in each component when batching.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public CCPoSFactory(Challenger challenger,
                        PRG prg,
                        int challengeBitLength,
                        int batchBitLength,
                        int statDist) {
        this.challenger = challenger;
        this.prg = prg;
        this.challengeBitLength = challengeBitLength;
        this.batchBitLength = batchBitLength;
        this.statDist = statDist;
    }

    /**
     * Returns a new instance with the given session identifier and
     * parent protocol.
     *
     * @param sid Session identifier.
     * @param protocol Parent protocol.
     * @return Instance of a proof of a shuffle.
     */
    public CCPoS newPoS(String sid, Protocol protocol) {
        return new CCPoSW(sid,
                          protocol,
                          challenger,
                          prg,
                          challengeBitLength,
                          batchBitLength,
                          statDist);
    }
}

