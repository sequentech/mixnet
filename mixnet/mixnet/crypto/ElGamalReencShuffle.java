
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

import mixnet.arithm.*;

/**
 * Captures a re-encryption shuffle of generalized El Gamal
 * ciphertexts.
 *
 * @author Douglas Wikstrom
 */
public interface ElGamalReencShuffle {

    /**
     * Returns the bilinear map corresponding to the key generation
     * algorithm.
     *
     * @return Bilinear map corresponding to the key generation
     * algorithm.
     */
    public BiPRingPGroup getBiKey();

    /**
     * Returns the number of ciphertexts shuffled together in parallel.
     */
    public int getWidth();

    /**
     * Returns a map corresponding to encryption of arrays of
     * plaintexts of the given size.
     *
     * @param size Number of elements to encrypt.
     * @return Encryption map.
     */
    public BiKeyedArrayMap getEncryptor(int size);

    /**
     * Returns a map corresponding to decryption of arrays of
     * ciphertexts of the given size. This computes the "decryption
     * factors".
     *
     * @param size Number of elements to decrypt.
     * @return Partial decryption map.
     */
    public BiKeyedArrayMap getPartDecryptor(int size);

    /**
     * Returns a map corresponding to the finalization step of
     * decryption. This takes the product of the decryption factors
     * and combines it with the ciphertext to derive the plaintexts.
     *
     * @param size Number of elements to decrypt.
     * @return Finalization map.
     */
    public HomPGroupPGroup getFinalizer();
}

