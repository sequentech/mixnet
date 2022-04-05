
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

/**
 * Container class for a random oracle used to produce challenges in
 * public coin protocols.

 * @author Douglas Wikstrom
 */
public class ChallengerRO implements Challenger {

    /**
     * Hashfunction used to construct random oracles.
     */
    protected Hashfunction roHashfunction;

    /**
     * Prefix used with each invocation of the random oracle.
     */
    protected byte[] globalPrefix;

    /**
     * Creates an instance which generates challenges using a "random
     * oracle" constructed from the given hashfunction.
     *
     * <p>
     *
     * WARNING! The hashfunction must be "cryptographically strong",
     * e.g., SHA-256 has this property, a collision-resistant
     * hashfunction is not enough.
     *
     * @param roHashfunction Hashfunction used to construct random oracles.
     * @param globalPrefix Prefix used with each invocation of the
     * random oracle.
     */
    public ChallengerRO(Hashfunction roHashfunction, byte[] globalPrefix) {
        this.roHashfunction = roHashfunction;
        this.globalPrefix = globalPrefix;
    }

    /**
     * Returns a challenge.
     *
     * @param data Input to the random oracle. This should contain the
     * instance and the messages up to the challenge step.
     * @param challengeBitLength Number of bits to generate.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Challenge bytes.
     */
    public byte[] challenge(ByteTreeBasic data,
                            int challengeBitLength,
                            int statDist) {
        return challenge(null, data, challengeBitLength, statDist);
    }

    // Documented in Challenger.java

    public byte[] challenge(Log log,
                            ByteTreeBasic data,
                            int challengeBitLength,
                            int statDist) {
        if (log != null) {
            log.info("Derive " + challengeBitLength +
                     " bits using random oracle.");
        }

        // Define a random oracle with the given output length.
        RandomOracle ro = new RandomOracle(roHashfunction, challengeBitLength);

        // Compute the digest of the byte tree.
        Hashdigest d = ro.getDigest();

        d.update(globalPrefix);
        data.update(d);

        byte[] digest = d.digest();

        return digest;
    }
}
