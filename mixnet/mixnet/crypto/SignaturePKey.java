
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

import mixnet.eio.*;

/**
 * Interface representing a public signature key.
 *
 * @author Douglas Wikstrom
 */
public interface SignaturePKey extends Marshalizable {

    /**
     * Verify the given signature and message using this public key.
     *
     * @param signature Candidate signature.
     * @param message Data that supposedly is signed.
     * @return Verdict for the signature and message pair.
     */
    public boolean verify(byte[] signature, byte[] ... message);

    /**
     * Verify the given signature and digest using this public
     * key. The result is undefined unless {@link #getDigest()} was
     * used to compute the digest.
     *
     * @param signature Candidate signature.
     * @param d Digest that supposedly is signed.
     * @return Verdict for the signature and message pair.
     */
    public boolean verifyDigest(byte[] signature, byte[] d);

    /**
     * Returns an updateable digest that can later be given as input
     * to {@link #verify(byte[],byte[])}.
     *
     * @return Updateable digest.
     */
    public Hashdigest getDigest();
}