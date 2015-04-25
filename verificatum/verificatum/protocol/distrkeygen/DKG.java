
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
import java.util.*;
import java.util.logging.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.ui.*;
import verificatum.protocol.*;
import verificatum.ui.info.*;
import verificatum.protocol.secretsharing.*;

/**
 * Implements a basic distributed key generation protocol for a public
 * key for which the secret key is verifiably shared among the
 * parties.
 *
 * @author Douglas Wikstrom
 */
public class DKG extends Protocol {

    /**
     * Original basic public key which defines the homomorphism.
     */
    protected PGroupElement basicPublicKey;

    /**
     * Underlying bilinear map.
     */
    protected BiPRingPGroup bi;

    /**
     * Underlying homomorphism.
     */
    protected HomPRingPGroup hom;

    /**
     * Protocol used for generating joint keys.
     */
    protected PedersenSequential pedersenSequential;

    /**
     * Holds the individual generated public keys of other parties.
     */
    protected PGroupElement[] publicKeys;

    /**
     * Holds the individual recovered private of other parties.
     */
    protected PRingElement[] secretKeys;

    /**
     * Public keys used in subprotocols.
     */
    protected CryptoPKey[] pkeys;

    /**
     * Secret key used in subprotocols.
     */
    protected CryptoSKey skey;

    /**
     * States in which an instance can be.
     */
    protected enum State {

        /**
         * Initial state of this instance after instantiation.
         */
        INITIAL,

        /**
         * State after generation of keys completed.
         */
        GENERATION_COMPLETED
    };

    /**
     * Current state of this instance.
     */
    protected State state;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Creates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param pkeys Plain public keys of all parties.
     * @param skey Plain secret key.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public DKG(String sid,
               Protocol protocol,
               CryptoPKey[] pkeys,
               CryptoSKey skey,
               int statDist) {
        super(sid, protocol);
        this.bi = bi;
        this.pkeys = pkeys;
        this.skey = skey;
        this.statDist = statDist;

        // Make room for all keys.
        publicKeys = new PGroupElement[k + 1];
        secretKeys = new PRingElement[k + 1];

        // Set our state.
        state = State.INITIAL;
    }

    /**
     * Generate the keys of a given type.
     *
     * @param log Logging context.
     * @param bi Bilinear map capturing the key generation algorithm.
     * @param basicPublicKey Basic public key used as input to the key
     * generation algorithm.
     */
    public void generate(Log log,
                         BiPRingPGroup bi,
                         PGroupElement basicPublicKey) {

        if (state != State.INITIAL) {
            throw new ProtocolError("Attempting to reuse instance!");
        }

        this.basicPublicKey = basicPublicKey;

        hom = bi.restrict(basicPublicKey);

        log.info("Generate distributed keys.");
        Log tempLog = log.newChildLog();

        // Generate our secret key.
        tempLog.info("Generate secret key.");
        Log tempLog2 = tempLog.newChildLog();

        File file = getFile("SecretKey");
        if (file.exists()) {

            tempLog2.info("Read secret key from file.");
            try {

                ByteTreeReader btr = (new ByteTreeF(file)).getByteTreeReader();
                secretKeys[j] = hom.getDomain().toElement(btr);
                btr.close();

            } catch (ArithmFormatException afe) {
                throw new ProtocolError("Unable to read secret key!", afe);
            }

        } else {

            // Generate secret key
            secretKeys[j] =
                hom.getDomain().randomElement(randomSource, statDist);

            // Write secret key to file.
            secretKeys[j].toByteTree().unsafeWriteTo(getFile("SecretKey"));

            tempLog2.info("Write secret key to file.");
        }

        // Secret share our secret key and receive shares from others
        tempLog.info("Verifiably secret share secret key.");
        tempLog2 = tempLog.newChildLog();
          pedersenSequential =
            new PedersenSequential("", this, hom, pkeys, skey, statDist, true);

        pedersenSequential.execute(tempLog2, secretKeys[j]);


         // Initialize the other public keys
         for (int i = 1; i <= k; i++) {

            publicKeys[i] = pedersenSequential.getConstantElement(tempLog, i);

            // If the public key is trivial we initialize the
            // corresponding secret key
            if (publicKeys[i].equals(hom.getRange().getg())) {

                secretKeys[i] = hom.getDomain().getONE();

            }
         }

        state = State.GENERATION_COMPLETED;
    }

    /**
     * Recovers the secret key of a given party.
     *
     * @param log Logging context.
     * @param l Index of the owner of the key to recover.
     * @return Secret key of party <code>l</code>.
     */
    public PRingElement recoverSecretKey(Log log, int l) {

        log.info("Recover secret key of " + ui.getDescrString(l) + ".");
        Log tempLog = log.newChildLog();

        if (state != State.GENERATION_COMPLETED) {
            throw new ProtocolError("Keys have not been generated!");
        }

        secretKeys[l] = pedersenSequential.recover(log, l);
        return secretKeys[l];
    }

    /**
     * Returns the secret key of a given party. Provided that it has
     * previously been recovered.
     *
     * @param log Logging context.
     * @param l Index of the party owning the public key.
     * @return Secret key of party <code>l</code>.
     */
    public PRingElement getSecretKey(Log log, int l) {

        if (state != State.GENERATION_COMPLETED) {
            throw new ProtocolError("Keys have not been generated!");
        }

        if (secretKeys[l] != null) {
            return secretKeys[l];
        } else {
            throw new ProtocolError("Requesting secret key that "
                                    + "has not been recovered!");
        }
    }

    /**
     * Returns the basic public key.
     *
     * @return Basic public key.
     */
    public PGroupElement getBasicPublicKey() {
        return basicPublicKey;
    }

    /**
     * Returns the public key of a given party.
     *
     * @param l Index of the party owning the public key.
     * @return Public key of party <code>l</code>.
     */
    public PGroupElement getPublicKey(int l) {

        if (state != State.GENERATION_COMPLETED) {
            throw new ProtocolError("Keys have not been generated!");
        }
        return publicKeys[l];
    }

    /**
     * Returns the group in which the protocol is deployed.
     *
     * @return Group over which the protocol is executed.
     */
    public PGroup getPGroup() {
        return hom.getRange();
    }

    /**
     * Computes the joint public key by taking the product of the
     * y-parts of the public keys of all servers.
     *
     * @return Joint public key.
     */
    public PGroupElement getJointPublicKey() {

        PGroupElement res = hom.getRange().getONE();

        for (int l = 1; l <= k; l++) {
            res = res.mul(publicKeys[l]);
        }
        return res;
    }

    /**
     * Returns the product of the basic public key and the joint
     * public key, i.e., the full public key.
     *
     * @return Full joint public key.
     */
    public PGroupElement getFullPublicKey() {
        PPGroup pPGroup = new PPGroup(basicPublicKey.getPGroup(),
                                      getJointPublicKey().getPGroup());
        return pPGroup.product(getBasicPublicKey(),
                               getJointPublicKey());
    }
}
