
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

import verificatum.eio.*;

/**
 * Interface representing a secret key of a cryptosystem with labels.
 *
 * @author Douglas Wikstrom
 */
public interface CryptoSKey extends Marshalizable {

    /**
     * Decrypts the given ciphertext.
     *
     * @param label Label associated with the ciphertext.
     * @param ciphertext Ciphertext to be decrypted.
     * @return Resulting plaintext.
     */
    public abstract byte[] decrypt(byte[] label, byte[] ciphertext);
}