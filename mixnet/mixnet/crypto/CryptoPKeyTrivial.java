
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

import java.util.*;

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;

/**
 * A trivial encryption key that maps a message to itself. This is
 * useful as a fallback to simplify the logics of protocols.
 *
 * @author Douglas Wikstrom
 */
public class CryptoPKeyTrivial implements CryptoPKey {

    /**
     * Constructs an instance corresponding to the input representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Public key represented by the input.
     */
    public static CryptoPKeyTrivial newInstance(ByteTreeReader btr,
                                                RandomSource rs,
                                                int certainty) {
        return new CryptoPKeyTrivial();
    }

    // Documented in CryptoPKey.java

    public byte[] encrypt(byte[] label, byte[] message,
                          RandomSource randomSource, int statDist) {
        return Arrays.copyOfRange(message, 0, message.length);
    }

    // Documented in ByteTreeConvertible.java

    public ByteTreeBasic toByteTree() {
        return new ByteTree();
    }

    // Documented in Marshalizable.java

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose);
    }
}