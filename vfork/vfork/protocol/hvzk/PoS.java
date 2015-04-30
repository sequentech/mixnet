
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

import vfork.arithm.*;
import vfork.ui.*;

/**
 * Interface for a proof of a shuffle.
 *
 * @author Douglas Wikstrom
 */
public interface PoS {

    /**
     * Execute prover.
     *
     * @param log Logging context.
     * @param g Standard generator.
     * @param h Independent generators.
     * @param u Permutation commitment.
     * @param r Commitment exponents.
     * @param pi Permutation.
     * @param exportDir Export directory for universal verifiability.
     */
    public void prove(Log log,
                      PGroupElement g,
                      PGroupElementArray h,
                      PGroupElementArray u,
                      PRingElementArray r,
                      Permutation pi,
                      File exportDir);

    /**
     * Execute verifier.
     *
     * @param log Logging context.
     * @param l Index of prover.
     * @param g Standard generator.
     * @param h Independent generators.
     * @param u Permutation commitment.
     * @return Verdict about the proof.
     * @param exportDir Export directory for universal verifiability.
     */
    public boolean verify(Log log,
                          int l,
                          PGroupElement g,
                          PGroupElementArray h,
                          PGroupElementArray u,
                          File exportDir);
}