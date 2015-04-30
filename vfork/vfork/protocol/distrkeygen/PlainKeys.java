
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

package vfork.protocol.distrkeygen;

import java.io.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.protocol.*;
import vfork.ui.info.*;
import vfork.ui.*;

/**
 * The trivial protocol where each party generates a key pair of a
 * cryptosystem and then shares it with the other parties.
 *
 * @author Douglas Wikstrom
 */
public class PlainKeys extends Protocol {

    /**
     * Underlying key generator.
     */
    protected CryptoKeyGen keyGen;

    /**
     * Secret key of this instance.
     */
    protected CryptoSKey skey;

    /**
     * Public keys of all parties.
     */
    protected CryptoPKey[] pkeys;

    /**
     * States in which an instance of this protocol can be.
     */
    enum State {
        INITIAL,
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
     * @param keyGen Key generator.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public PlainKeys(String sid,
                     Protocol protocol,
                     CryptoKeyGen keyGen,
                     int statDist) {
        super(sid, protocol);
        this.keyGen = keyGen;
        this.statDist = statDist;
        state = State.INITIAL;
    }

    /**
     * Executes the protocol.
     *
     * @param log Logging context.
     */
    public void generate(Log log) {

        // Make room for public keys
        pkeys = new CryptoPKey[k + 1];

        log.info("Generate and read plain keys.");
        Log tempLog = log.newChildLog();

        // Read keys from file if they exist.
        File file = getFile("Keys");
        if (file.exists()) {
            tempLog.info("Read keys from file.");
            try {
                ByteTreeReader btr = (new ByteTreeF(file)).getByteTreeReader();

                skey = Marshalizer.unmarshalAux_CryptoSKey(btr.getNextChild(),
                                                           randomSource,
                                                           statDist);
                for (int l = 1; l <= k; l++) {
                    pkeys[l] =
                        Marshalizer.unmarshalAux_CryptoPKey(btr.getNextChild(),
                                                            randomSource,
                                                            statDist);
                }
            } catch (EIOException eioe) {
                throw new ProtocolError("Unable to open or read state file!",
                                        eioe);
            }

            state = State.GENERATION_COMPLETED;
            return;
        }

        if (state != State.INITIAL) {
            throw new ProtocolError("Trying to reuse instance!");
        }

        // Generate a new key
        tempLog.info("Generate key-pair.");
        CryptoKeyPair keyPair = keyGen.gen(randomSource, statDist);

        skey = keyPair.getSKey();
        pkeys[j] = keyPair.getPKey();

        // Read the keys of all parties
        tempLog.info("Exchange public keys with all parties.");
        Log tempLog2 = tempLog.newChildLog();

        for (int l = 1; l <= k; l++) {

            if (l == j) {

                // Write our public key on the bulletin board
                tempLog2.info("Publish public key.");
                bullBoard.publish("PublicKey", Marshalizer.marshal(pkeys[j]),
                                  tempLog2);

            } else {

                // Read public key of other party.
                tempLog2.info("Read public key of " +
                              ui.getDescrString(l) + ".");
                ByteTreeReader reader =
                    bullBoard.waitFor(l, "PublicKey", tempLog2);

                try {

                    pkeys[l] = Marshalizer.unmarshalAux_CryptoPKey(reader,
                                                                   randomSource,
                                                                   statDist);
                    tempLog2.info("Parsed public key successfully.");

                } catch (EIOException eioe) {

                    tempLog2.register(eioe);
                    pkeys[l] = null;

                }
                if (pkeys[l] == null) {
                    String s = "Unable to parse public key. "
                        + "Setting it to trivial key.";
                    tempLog2.info(s);
                }
                reader.close();
            }
        }

        // If a key is badly formatted we set it to the trivial key.
        for (int l = 1; l <= k; l++) {
            if (pkeys[l] == null) {
                pkeys[l] = new CryptoPKeyTrivial();
            }
        }

        // Write all keys to file.
        tempLog.info("Writing keys to file.");

        ByteTreeBasic[] byteTrees = new ByteTreeBasic[k + 1];
        byteTrees[0] = Marshalizer.marshal(skey);
        for (int l = 1; l <= k; l++) {
            byteTrees[l] = Marshalizer.marshal(pkeys[l]);
        }
        ByteTreeBasic byteTree = new ByteTreeContainer(byteTrees);
        byteTree.unsafeWriteTo(getFile("Keys"));

        state = State.GENERATION_COMPLETED;
    }

    /**
     * Returns the secret key of this instance.
     *
     * @return Secret key of this instance.
     */
    public CryptoSKey getSKey() {
        return skey;
    }

    /**
     * Returns an array containing the generated public keys.
     *
     * @return Array of all public keys.
     */
    public CryptoPKey[] getPKeys() {
        return Arrays.copyOf(pkeys, pkeys.length);
    }
}
