
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
import verificatum.eio.*;

/**
 * Homomorphism capturing partial El Gamal decryption in the form of a
 * batchable keyed map.
 *
 * @author Douglas Wikstrom
 */
public class HomElGamalPartDecryptStandard extends HomPRingPGroupRest
    implements Batchable {

    /**
     * Exponents used for batching.
     */
    protected PFieldElementArray batchingExps;

    /**
     * Batched bilinear map.
     */
    protected BiElGamalPartDecryptStandard batchedBi;

    /**
     * Restriction used when batching homomorphism.
     */
    protected PGroupElement batchingRestriction;

    /**
     * Creates the homomorphism from the given restriction.
     *
     * @param bi Underlying bilinear map.
     * @param restriction Restriction of the bilinear map.
     */
    public HomElGamalPartDecryptStandard(BiElGamalPartDecryptStandard bi,
                                         PGroupElement restriction) {
        super(bi, restriction);

        batchedBi =
            new BiElGamalPartDecryptStandard(bi.getBiKey().getPGroupDomain(),
                                             bi.width,
                                             1);
    }


    // Documented in Batchable.java

    public PFieldElementArray initBatching(PRG prg, int batchBitLength) {

        // Extract size of vectors to batch.
        PGroup pgd = bi.getPGroupDomain();
        APGroup apg = (APGroup)((PPGroup)pgd).project(1);
        int size = apg.size();

        LargeIntegerArray tmp =
            LargeIntegerArray.random(size, batchBitLength, prg);
        batchingExps = apg.getPRing().getPField().unsafeToElementArray(tmp);

        return batchingExps;
    }

    public PRingElement batchedPreimage(PRingElement preimage) {
        return preimage;
    }

    public PGroupElement batchedImage(PGroupElement image) {

        // Extract public key and array of group elements.
        PPGroupElement ppge = (PPGroupElement)image;
        PGroupElement publicKey = ppge.project(0);
        APGroupElement apge = (APGroupElement)ppge.project(1);
        PGroupElementArray imageElements = apge.getContent();

        // Batch the group elements.
        PGroupElement imageElement = imageElements.expProd(batchingExps);

        // Pack and return the result.
        PPGroup pgd = (PPGroup)batchedBi.getRange();
        APGroup apg = (APGroup)pgd.project(1);

        return pgd.product(publicKey, apg.toElement(imageElement));
    }

    public HomPRingPGroup batchedMap() {

        // Extract basic public key and array of group elements.
        PPGroupElement ppge = (PPGroupElement)restriction;
        PGroupElement basicPublicKey = ppge.project(0);
        APGroupElement apge = (APGroupElement)ppge.project(1);
        PGroupElementArray groupElements = apge.getContent();

        // Batch the array.
        PGroupElement groupElement = groupElements.expProd(batchingExps);

        // Pack and return the result.
        PPGroup pgd = (PPGroup)batchedBi.getPGroupDomain();
        APGroup apg = (APGroup)pgd.project(1);

        if (batchingRestriction != null) {
            batchingRestriction.free();
        }
        batchingRestriction =
            pgd.product(basicPublicKey, apg.toElement(groupElement));

        return batchedBi.restrict(batchingRestriction);
    }

    public void free() {
        if (batchingRestriction != null) {
            batchingRestriction.free();
        }
    }
}
