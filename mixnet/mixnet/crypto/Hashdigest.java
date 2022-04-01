
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

package mixnet.crypto;

import mixnet.eio.*;

/**
 * Interface for a digest of a collision-free hash function.
 *
 * @author Douglas Wikstrom
 */
public interface Hashdigest {

    /**
     * Update the digest with more data.
     *
     * @param data Data to be hashed.
     */
    public abstract void update(byte[] ... data);

    /**
     * Update the digest with more data.
     *
     * @param data Data to be hashed.
     * @param offset Offset of first byte to be processed.
     * @param length Number of bytes to process.
     */
    public abstract void update(byte[] data, int offset, int length);

    /**
     * Finalizes and returns the digest.
     *
     * @return Digest.
     */
    public abstract byte[] digest();
}