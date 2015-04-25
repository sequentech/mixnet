
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
 * Abstract base class for a collision-free hash function with fixed
 * input and output sizes. If the input length is not a multiple of 8,
 * then the appropriate number of bits in the first input byte are
 * ignored.
 *
 * @author Douglas Wikstrom
 */
public interface HashfunctionFixedLength extends HashfunctionBasic {

    /**
     * Evaluates the function on the given input. If the output length
     * is not a multiple of 8, the suitable number of bits in the
     * first output byte are set to zero.
     *
     * @param data Input data.
     * @return Output of hash function.
     */
    public abstract byte[] hash(byte[] data);

    /**
     * Returns the input bit length.
     *
     * @return Input length in bits.
     */
    public abstract int getInputLength();
}
