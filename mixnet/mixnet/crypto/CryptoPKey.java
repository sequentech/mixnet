
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
 * Interface representing a public key of a cryptosystem with labels.
 *
 * @author Douglas Wikstrom
 */
public interface CryptoPKey extends Marshalizable {

    /**
     * Encrypts the given message using randomness from the given
     * source.
     *
     * @param label Label used when encrypting.
     * @param message Message to be encrypted.
     * @param randomSource Source of randomness.
     * @param statDist Allowed statistical error from ideal distribution.
     * @return Resulting ciphertext.
     */
    public abstract byte[] encrypt(byte[] label, byte[] message,
                                   RandomSource randomSource, int statDist);
}