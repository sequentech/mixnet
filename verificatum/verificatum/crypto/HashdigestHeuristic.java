
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

import java.security.*;

import verificatum.eio.*;

/**
 * Interface for a digest of a collision-free hash function.
 *
 * @author Douglas Wikstrom
 */
public class HashdigestHeuristic implements Hashdigest {

    /**
     * Message digest wrapped by this instance.
     */
    protected MessageDigest md;

    /**
     * Constructs a wrapper for the given message digest.
     *
     * @param md Message digest wrapped by this instance.
     */
    public HashdigestHeuristic(MessageDigest md) {
        this.md = md;
    }

    // Documented in Hashdigest.java

    public void update(byte[] ... data) {
        for (int i = 0; i < data.length; i++) {
            md.update(data[i]);
        }
    }

    public void update(byte[] data, int offset, int length) {
        md.update(data, offset, length);
    }

    public byte[] digest() {
        return md.digest();
    }
}