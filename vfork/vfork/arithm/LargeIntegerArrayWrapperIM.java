
/*
 * Copyright 2011 Eduardo Robles Elvira <edulix AT wadobo DOT com>
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

package vfork.arithm;

import java.io.*;
import java.math.*;
import java.util.*;

import vfork.crypto.*;
import vfork.eio.*;
import vfork.util.*;

/**
 * Wrapper for LargeIntegerArrayIM
 *
 * @author Eduardo Robles Elvira
 */
public class LargeIntegerArrayWrapperIM extends LargeIntegerArrayWrapper {

    public LargeIntegerArray create(LargeInteger[] integers) {
        return new LargeIntegerArrayIM(integers);
    }

    public LargeIntegerArray create(LargeIntegerArray ... arrays) {
        return new LargeIntegerArrayIM(arrays);
    }

    public LargeIntegerArray create(int size,
                                    ByteTreeReader btr,
                                    LargeInteger lb,
                                    LargeInteger ub)
        throws IOException, EIOException, ArithmFormatException {
        return new LargeIntegerArrayIM(size, btr, lb, ub);
    }

    public LargeIntegerArray create(int size,
                                    LargeInteger modulus,
                                    int statDist,
                                    RandomSource randomSource) {
        return new LargeIntegerArrayIM(size, modulus, statDist, randomSource);
    }

    public LargeIntegerArray create(int size,
                                    int bitLength,
                                    RandomSource randomSource) {
        return new LargeIntegerArrayIM(size, bitLength, randomSource);
    }

    public LargeIntegerArray create(int size, LargeInteger value) {
        return new LargeIntegerArrayIM(size, value);
    }
}
