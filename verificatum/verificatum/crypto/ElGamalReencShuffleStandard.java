
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

import verificatum.arithm.*;

/**
 * Captures a re-encryption shuffle of standard El Gamal ciphertexts.
 *
 * @author Douglas Wikstrom
 */
public class ElGamalReencShuffleStandard implements ElGamalReencShuffle {

    /**
     * Underlying group.
     */
    protected PGroup pGroup;

    /**
     * Number of ciphertexts shuffled together in parallel.
     */
    protected int width;

    /**
     * Creates a container for the operations of a standard
     * reencryption shuffle of El Gamal ciphertexts.
     *
     * @param pGroup Underlying group.
     * @param width Number of ciphertexts shuffled in parallel.
     */
    public ElGamalReencShuffleStandard(PGroup pGroup, int width) {
        this.pGroup = pGroup;
        this.width = width;
    }

    public ElGamalReencShuffleStandard(PGroup pGroup) {
        this(pGroup, 1);
    }

    public int getWidth() {
        return width;
    }

    public BiPRingPGroup getBiKey() {
        return new BiExp(pGroup);
    }

    public BiKeyedArrayMap getEncryptor(int size) {
        return new BiElGamalEncryptStandard(pGroup, width, size);
    }

    public BiKeyedArrayMap getPartDecryptor(int size) {
        return new BiElGamalPartDecryptStandard(pGroup, width, size);
    }

    public HomPGroupPGroup getFinalizer() {
        return new HomElGamalCombDecryptStandard(pGroup, width);
    }
}

