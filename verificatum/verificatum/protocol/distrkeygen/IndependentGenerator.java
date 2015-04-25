
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

package verificatum.protocol.distrkeygen;

import java.io.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.protocol.*;
import verificatum.protocol.secretsharing.*;
import verificatum.ui.*;

/**
 * Generates an independent generator of a prime order group. This is
 * essentially a wrapper class for {@link
 * verificatum.protocol.secretsharing.PedersenSequential} executed as
 * a Feldman protocol with a randomly chosen secret.
 *
 * <p>
 *
 * This is not a coin-flipping protocol, since the generated group
 * element is not necessarily (pseudo) random. However, if the
 * adversary can compute the logarithm of the generated element
 * without corrupting a threshold of the parties and without breaking
 * the security of the cryptosystem, then an algorithm for computing
 * the logarithm of a randomly chosen element can be constructed. This
 * property suffices for most, if not all, applications.
 *
 * @author Douglas Wikstrom
 */
public class IndependentGenerator extends Protocol {

    /**
     * Group for which the independent generator is generated.
     */
    protected PGroup pGroup;

    /**
     * Our secret key.
     */
    protected CryptoSKey skey;

    /**
     * All public keys.
     */
    protected CryptoPKey[] pkeys;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Creates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param pGroup Group in which the generator is generated.
     * @param pkeys Plain public keys of all parties.
     * @param skey Plain secret key.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public IndependentGenerator(String sid,
                                Protocol protocol,
                                PGroup pGroup,
                                CryptoPKey[] pkeys,
                                CryptoSKey skey,
                                int statDist) {
        super(sid, protocol);
        this.pGroup = pGroup;
        this.pkeys = pkeys;
        this.skey = skey;
        this.statDist = statDist;
    }

    /**
     * Generate independent generator.
     *
     * @param log Logging context.
     * @return Generated group element.
     */
    public PGroupElement generate(Log log) {
        PGroupElement h;

        log.info("Generate independent generator.");

        Log tempLog = log.newChildLog();
        File file = getFile("IndependentGenerator");
        if (file.exists()) {

            tempLog.info("Read independent generator from file.");

            ByteTreeReader btr = (new ByteTreeF(file)).getByteTreeReader();
            h = pGroup.unsafeToElement(btr);
            btr.close();

        } else {

            BiPRingPGroup biExp = new BiExp(pGroup);
            HomPRingPGroup hom = biExp.restrict(pGroup.getg());

            PedersenSequential independentGenerator =
                new PedersenSequential("IndependentGenerator",
                                       this,
                                       hom,
                                       pkeys,
                                       skey,
                                       statDist);

            PRingElement secret =
                hom.getDomain().randomElement(randomSource, statDist);
            independentGenerator.execute(tempLog, secret);

            h = independentGenerator.getConstantElementProduct(tempLog);

            file = getFile("IndependentGenerator");
            h.toByteTree().unsafeWriteTo(file);

            tempLog.info("Write independent generator to file.");

        }
        return h;
    }
}
