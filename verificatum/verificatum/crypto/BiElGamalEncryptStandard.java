
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

import verificatum.arithm.*;
import verificatum.eio.*;

/**
 * Bilinear map capturing El Gamal encryption.
 *
 * @author Douglas Wikstrom
 */
public class BiElGamalEncryptStandard extends BiKeyedArrayMap {

    /**
     * Width of the plaintext group as a product over the underlying
     * group.
     */
    protected int width;

    protected static PRing genPRingDomain(PGroup pGroup, int width) {
        if (width == 1) {
            return pGroup.getPRing();
        } else {
            return new PPRing(pGroup.getPRing(), width);
        }
    }

    protected static PGroup genPGroupDomain(PGroup pGroup, int width) {
        if (width == 1) {
            return pGroup;
        } else {
            return new PPGroup(pGroup, width);
        }
    }

    protected static PGroup genPGroupRange(PGroup pGroup, int width) {
        if (width == 1) {
            return new PPGroup(pGroup, 2);
        } else {
            return new PPGroup(new PPGroup(pGroup, width),
                               new PPGroup(pGroup, width));
        }
    }

    /**
     * Creates an instance with the given underlying group.
     *
     * @param pGroup Underlying group.
     * @param size Number of elements in input.
     * @param width Number of ciphertexts shuffled in parallel.
     */
    public BiElGamalEncryptStandard(PGroup pGroup, int width, int size) {

        // Here the key map is the simple identity map, since
        // encryption does not require a secret key.
        super(new BiIdentity(new PPGroup(pGroup, 2)),
              genPRingDomain(pGroup, width),
              genPGroupDomain(pGroup, width),
              genPGroupRange(pGroup, width),
              size);
        this.width = width;
    }


    // Documented in BiKeyedArrayMap.java

    protected PGroupElementArray arrayMap(PRingElement secretKey,
                                          PGroupElement basicPublicKey,
                                          PRingElementArray randomizers,
                                          PGroupElementArray groupElements) {

        // Extract the two parts of the key.
        PGroupElement g = ((PPGroupElement)basicPublicKey).project(0);
        PGroupElement y = ((PPGroupElement)basicPublicKey).project(1);

        PPGroup arrayRange = (PPGroup)getArrayRange();

        // Expand the keys if needed.
        if (width > 1) {
            g = (new PPGroup(g.getPGroup(), width)).product(g);
            y = (new PPGroup(y.getPGroup(), width)).product(y);
        }

        // Construct ciphertexts.
        PGroupElementArray u = g.exp(randomizers);
        PGroupElementArray tmp = y.exp(randomizers);
        PGroupElementArray v = tmp.mul(groupElements);
        tmp.free();

        return arrayRange.product(u, v);
    }
}
