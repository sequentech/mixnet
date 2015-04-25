
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

package verificatum.crypto;

import java.io.*;

import verificatum.eio.*;

/**
 * Abstract pseudo-random generator (PRG). This differs from a {@link
 * RandomSource} that happens to be implemented by a pseudo-random
 * generator in that the seed can be set/reset and the PRG is
 * guaranteed to give the same output each time on a given seed.
 *
 * @author Douglas Wikstrom
 */
public abstract class PRG extends RandomSource {

    /**
     * Resets the seed to the given value. The output of the PRG
     * <b>must</b> be determined by the seed, i.e., every time the
     * seed is reset the output of the PRG must be exactly the same.
     *
     * @param seed New seed.
     */
    public abstract void setSeed(byte[] seed);

    /**
     * Returns the minimum number of random seed bytes needed for this
     * PRG to remain secure (under the appropriate complexity
     * assumptions).
     *
     * @return Needed number of seed bytes.
     */
    public abstract int minNoSeedBytes();

    // Replaces documentation in io.ByteTreeConvertible.java, since
    // PRGs convert themselves in an unusual way.

    /**
     * Returns a <code>ByteTree</code> representation of this
     * instance. Typically, the instance derives a new seed from its
     * output and stores it. Thus, one should not use this method to
     * store a particular state of a PRG. To do that, a seed must be
     * stored explicitly.
     *
     * @return Representation of this instance.
     */
    public abstract ByteTreeBasic toByteTree();

    /**
     * Reads a seed from file, resets this PRG, and replaces the seed
     * on file by a newly generated seed derived from the PRG
     * itself. This encapsulates a relatively safe way to repeatedly
     * use seed a PRG from the same file.
     *
     * <p>
     *
     * WARNING! The seed file must remain secret. Make sure it is not
     * readable by the adversary.
     *
     * @param seedFile File containing the seed.
     * @param tmpSeedFile Temporary file used to implement atomic
     * write to the seed file.
     *
     * @throws IOException If there is an IO failure when reading or
     * writing the seed.
     */
    public void setSeedReplaceStored(File seedFile, File tmpSeedFile)
        throws IOException {

        // Read and set seed.
        String seedString = ExtIO.readString(seedFile);
        byte[] seed = Hex.toByteArray(seedString);
        setSeed(seed);

        // Write new seed.
        byte[] newSeed = getBytes(minNoSeedBytes());
        String newSeedString = Hex.toHexString(newSeed);
        ExtIO.atomicWriteString(tmpSeedFile, seedFile, newSeedString);
    }
}
