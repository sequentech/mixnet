
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
 * Bilinear map capturing partial El Gamal decryption, i.e., this map
 * computes what is commonly known as the "decryption factors".
 *
 * @author Douglas Wikstrom
 */
public class BiElGamalPartDecryptStandard extends BiKeyedArrayMap {

    /**
     * Width of the plaintext group as a product over the underlying
     * group.
     */
    protected int width;

    protected static PGroup genPGroupDomain(PGroup pGroup, int width) {
        if (width == 1) {
            return new PPGroup(pGroup, 2);
        } else {
            return new PPGroup(new PPGroup(pGroup, width),
                               new PPGroup(pGroup, width));
        }
    }

    protected static PGroup genPGroupRange(PGroup pGroup, int width) {
        if (width == 1) {
            return pGroup;
        } else {
            return new PPGroup(pGroup, width);
        }
    }

    /**
     * Creates an instance with the given underlying group.
     *
     * @param pGroup Underlying group.
     * @param size Number of elements in input.
     */
    public BiElGamalPartDecryptStandard(PGroup pGroup, int width, int size) {
        super(new BiExp(pGroup),
              null,
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
        PGroupElementArray u = ((PPGroupElementArray)groupElements).project(0);
        return u.exp(secretKey);
    }

    public HomPRingPGroup restrict(PGroupElement groupElement) {
        return new HomElGamalPartDecryptStandard(this, groupElement);
    }
}
