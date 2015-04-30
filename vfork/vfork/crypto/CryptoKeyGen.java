
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

package vfork.crypto;

import vfork.eio.*;

/**
 * Interface representing a key generation algorithm of a cryptosystem
 * for a given set of parameters, including the security parameter.
 *
 * @author Douglas Wikstrom
 */
public interface CryptoKeyGen extends Marshalizable {

    /**
     * Generates a key pair of a cryptosystem.
     *
     * @param randomSource Source of randomness used by generator.
     * @param statDist Statistical distance.
     * @return Key pair of a cryptosystem.
     */
    public abstract CryptoKeyPair gen(RandomSource randomSource, int statDist);
}
