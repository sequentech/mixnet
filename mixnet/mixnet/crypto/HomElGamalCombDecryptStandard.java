
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
 * Homomorphism that corresponds to the final combination step of
 * joint decryption of an array of decryption factors and an array of
 * El Gamal ciphertexts.
 *
 * @author Douglas Wikstrom
 */
public class HomElGamalCombDecryptStandard implements HomPGroupPGroup {

    /**
     * Underlying group.
     */
    protected PGroup pGroup;

    /**
     * Number of ciphertexts shuffled in parallel.
     */
    protected int width;

    /**
     * Creates a homomorphism that corresponds to the final step of
     * joint decryption.
     *
     * @param pGroup Underlying group.
     * @param width Number of ciphertexts shuffled in parallel.
     */
    public HomElGamalCombDecryptStandard(PGroup pGroup, int width) {
        this.pGroup = pGroup;
        this.width = width;
    }


    // Documented in HomPGroupPGroup.java.

    public PGroup getDomain() {
        if (width == 1) {
            return new PPGroup(pGroup, 2);
        } else {
            return new PPGroup(new PPGroup(pGroup, width),
                               new PPGroup(pGroup, width));
        }
    }

    public PGroup getRange() {
        if (width == 1) {
            return pGroup;
        } else {
            return new PPGroup(pGroup, width);
        }
    }

    public PGroupElement map(PGroupElement element) {

        // Extract decryption factors.
        PGroupElement factorsElement = ((PPGroupElement)element).project(0);

        // Extract the second component array of the ciphertext.
        PGroupElementArray ciphertexts =
            ((APGroupElement)((PPGroupElement)element).project(1)).getContent();
        PGroupElementArray v = ((PPGroupElementArray)ciphertexts).project(1);
        APGroup aPGroup = new APGroup(v.getPGroup(), v.size());

        // Return the list of plaintexts.
        return aPGroup.toElement(v).div(factorsElement);
    }
}
