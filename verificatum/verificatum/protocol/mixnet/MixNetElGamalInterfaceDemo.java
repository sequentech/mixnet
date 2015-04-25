
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

package verificatum.protocol.mixnet;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.protocol.*;
import verificatum.ui.*;

/**
 * Interface of an El Gamal mix-net with the ability to generate demo
 * ciphertexts.
 *
 * @author Douglas Wikstrom
 */
public abstract class MixNetElGamalInterfaceDemo
    extends MixNetElGamalInterface {

    /**
     * Generates the given number of ciphertexts.
     *
     * @param fullPublicKey Full public key.
     * @param noCiphs Number of ciphertexts to generate.
     * @param outputFile Destination of generated ciphertexts.
     * @param randomSource Source of randomness.
     */
    public abstract void demoCiphertexts(PGroupElement fullPublicKey,
                                         int noCiphs,
                                         File outputFile,
                                         RandomSource randomSource);
}
