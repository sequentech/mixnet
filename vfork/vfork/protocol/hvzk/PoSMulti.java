
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

package vfork.protocol.hvzk;

import java.io.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.protocol.*;
import vfork.protocol.coinflip.*;
import vfork.protocol.demo.*;
import vfork.protocol.distrkeygen.*;
import vfork.ui.*;
import vfork.ui.info.*;
import vfork.ui.opt.*;

/**
 * Interface for a round of proofs of shuffles.
 *
 * @author Douglas Wikstrom
 */
public interface PoSMulti {

    /**
     * Execute proofs.
     *
     * @param log Logging context.
     * @param g Standard generator.
     * @param generators Independent generators.
     * @param permutationCommitments Permutation commitment.
     * @param exportDir Export directory for universal verifiability.
     * @return Array of verdicts.
     */
    public boolean[] execute(Log log,
                             PGroupElement g,
                             PGroupElementArray generators,
                             PGroupElementArray[] permutationCommitments,
                             File exportDir);

    /**
     * Execute proofs.
     *
     * @param log Logging context.
     * @param g Standard generator.
     * @param generators Independent generators.
     * @param permutationCommitments Permutation commitment.
     * @param commitmentExponents Commitment exponents.
     * @param permutation Permutation.
     * @param exportDir Export directory for universal verifiability.
     * @return Array of verdicts.
     */
    public boolean[] execute(Log log,
                             PGroupElement g,
                             PGroupElementArray generators,
                             PGroupElementArray[] permutationCommitments,
                             PRingElementArray commitmentExponents,
                             Permutation permutation,
                             File exportDir);
}

