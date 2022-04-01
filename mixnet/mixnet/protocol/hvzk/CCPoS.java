
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

import mixnet.arithm.*;
import mixnet.ui.*;

/**
 * Interface for a commitment-consistent proof of a shuffle.
 *
 * @author Douglas Wikstrom
 */
public interface CCPoS {

    /**
     * Execute prover.
     *
     * @param log Logging context.
     * @param g Standard generator.
     * @param h Independent generators.
     * @param u Permutation commitment.
     * @param hom Encryption homomorphism.
     * @param pkey Public key used to construct the homomorphism.
     * @param w List of ciphertexts.
     * @param wp List of ciphertexts.
     * @param r Commitment exponents.
     * @param pi Permutation.
     * @param s Random exponents used to process ciphertexts.
     * @param exportDir Export directory for universal verifiability.
     */
    public void prove(Log log,
                      PGroupElement g,
                      PGroupElementArray h,
                      PGroupElementArray u,
                      HomPRingPGroup hom,
                      PGroupElement pkey,
                      PGroupElementArray w,
                      PGroupElementArray wp,
                      PRingElementArray r,
                      Permutation pi,
                      PRingElementArray s,
                      File exportDir);

    /**
     * Execute verifier.
     *
     * @param log Logging context.
     * @param l Index of prover.
     * @param g Standard generator.
     * @param h Independent generators.
     * @param u Permutation commitment.
     * @param hom Encryption homomorphism.
     * @param pkey Public key used to construct the homomorphism.
     * @param w List of ciphertexts.
     * @param wp List of ciphertexts.
     * @return Verdict about the proof.
     * @param exportDir Export directory for universal verifiability.
     */
    public boolean verify(Log log,
                          int l,
                          PGroupElement g,
                          PGroupElementArray h,
                          PGroupElementArray u,
                          HomPRingPGroup hom,
                          PGroupElement pkey,
                          PGroupElementArray w,
                          PGroupElementArray wp,
                          File exportDir);
}