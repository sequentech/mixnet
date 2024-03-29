
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
 * Source of random bytes. This may be a source of physical truly
 * random bits, or the output of a pseudo-random generator.
 *
 * @author Douglas Wikstrom
 */
public abstract class RandomSource implements Marshalizable {

    /**
     * Returns <code>length</code> bytes as a <code>byte[]</code>.
     *
     * @param length Number of generated bytes.
     * @return Random bits.
     */
    public byte[] getBytes(int length) {
        byte[] result = new byte[length];
        getBytes(result);
        return result;
    }

    /**
     * Fills the input array with random bytes.
     *
     * @param array Destination of generated bytes.
     */
    public abstract void getBytes(byte[] array);

    /**
     * Fills the input array with random bytes.
     *
     * @param array Destination of generated bytes.
     * @param start Index of position of first generated byte.
     * @param length Number of bytes generated.
     */
    public void getBytes(byte[] array, int start, int length) {
        byte[] tmp = getBytes(length);
        System.arraycopy(tmp, 0, array, start, length);
    }
}
