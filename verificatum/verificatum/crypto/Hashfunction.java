
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
 * Interface for a collision-free hash function with any length input.
 *
 * @author Douglas Wikstrom
 */
public interface Hashfunction extends HashfunctionBasic {

    /**
     * Evaluates the function on the given input.
     *
     * @param datas Input data.
     * @return Output of hash function.
     */
    public abstract byte[] hash(byte[] ... datas);

    /**
     * Returns an updateable digest.
     *
     * @return Updateable digest.
     */
    public abstract Hashdigest getDigest();
}
