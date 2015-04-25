
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

package verificatum.arithm;

import verificatum.crypto.*;

/**
 * Homomorphism capturing exponentiation of a fixed base with multiple
 * exponents. The multiple exponents are represented by an {@link
 * APRingElement} instance.
 *
 * @author Douglas Wikstrom
 */
public class HomFixedBaseExp extends HomPRingPGroupRest
    implements Batchable {

    /**
     * Exponents used for batching.
     */
    PFieldElementArray batchingExps;

    /**
     * Creates the homomorphism from the given restriction.
     *
     * @param bi Underlying bilinear map.
     * @param restriction Restriction of the bilinear map.
     */
    public HomFixedBaseExp(BiFixedBaseExp bi, PGroupElement restriction) {
        super(bi, restriction);
    }


    // Documented in Batchable.java

    public PFieldElementArray initBatching(PRG prg, int batchBitLength) {
        int size = ((APRing)bi.getPRingDomain()).size();
        LargeIntegerArray tmp =
            LargeIntegerArray.random(size, batchBitLength, prg);
        batchingExps =
            bi.getPRingDomain().getPField().unsafeToElementArray(tmp);
        return batchingExps;
    }

    public PRingElement batchedPreimage(PRingElement preimage) {
        return ((APRingElement)preimage).getContent().
            innerProduct(batchingExps);
    }

    public PGroupElement batchedImage(PGroupElement image) {
        return ((APGroupElement)image).getContent().
            expProd(batchingExps);
    }

    public HomPRingPGroup batchedMap() {
        return new BiExp(restriction.getPGroup()).restrict(restriction);
    }

    public void free() {}
}
