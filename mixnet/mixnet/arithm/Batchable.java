
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

package mixnet.arithm;

import mixnet.crypto.*;

/**
 * Interface for a batchable homomorphism.
 *
 * @author Douglas Wikstrom
 */
public interface Batchable extends HomPRingPGroup {

    /**
     * Initializes the batching.
     *
     * @param prg Pseudo-random generator used to derive batching
     * vector.
     * @param batchBitLength Bit-size of components when batching.
     * @return Batching vector derived from the prg.
     */
    public abstract PFieldElementArray initBatching(PRG prg,
                                                    int batchBitLength);

    /**
     * Returns the batched preimage.
     *
     * @param preimage Preimage to be batched.
     * @return Batched preimage.
     */
    public abstract PRingElement batchedPreimage(PRingElement preimage);

    /**
     * Returns the batched image.
     *
     * @param image Image to be batched.
     * @return Batched image.
     */
    public abstract PGroupElement batchedImage(PGroupElement image);

    /**
     * Returns the batched homomorphism.
     *
     * @return Batched map.
     */
    public abstract HomPRingPGroup batchedMap();
}
