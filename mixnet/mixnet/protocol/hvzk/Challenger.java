
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

package mixnet.protocol.hvzk;

import java.io.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;

/**
 * Interface capturing the challenger of a public-coin protocol.
 *
 * @author Douglas Wikstrom
 */
public interface Challenger {

    /**
     * Returns a challenge.
     *
     * @param log Logging context.
     * @param data Input to the random oracle, if this instance
     * generates its challenges using one. This should contain the
     * instance and the messages up to the challenge step.
     * @param challengeBitLength Number of bits to generate.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Challenge bytes.
     */
    public byte[] challenge(Log log,
                            ByteTreeBasic data,
                            int challengeBitLength,
                            int statDist);
}
