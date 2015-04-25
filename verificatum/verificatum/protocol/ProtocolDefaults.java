
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

package verificatum.protocol;

import java.io.*;
import java.net.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.ui.gen.*;
import verificatum.util.*;


/**
 * Provides default values of common protocol parameters.
 *
 * @author Douglas Wikstrom
 */
public class ProtocolDefaults {

    /**
     * Certainty with which probabilistically checked parameters are
     * verified, i.e., the probability of an error is bounded by
     * 2<sup>-{@link #CERTAINTY}</sup>.
     */
    public final static int CERTAINTY = 50;

    /**
     * Main security parameter.
     */
    public final static int SEC_PARAM = 2048;

    /**
     * Security parameter deciding the bit length of challenges in
     * interactive protocols.
     */
    public final static int SEC_PARAM_CHALLENGE = 100;

    /**
     * Security parameter deciding the bit length of challenges in
     * non-interactive random-oracle proofs.
     */
    public final static int SEC_PARAM_CHALLENGE_RO = 200;

    /**
     * Security parameter deciding the bit length of each component of
     * random vectors used for batching.
     */
    public final static int SEC_PARAM_BATCH = 100;

    /**
     * Security parameter deciding the bit length of each component of
     * random vectors used for batching in non-interactive
     * random-oracle proofs.
     */
    public final static int SEC_PARAM_BATCH_RO = 200;

    /**
     * Security parameter deciding the statistical error in a protocol
     * or hypothetical simulator, on top of any computational error.
     */
    public final static int STAT_DIST = 100;

    /**
     * Default type of http server.
     */
    final public static String HTTP_TYPE= "internal";

    /**
     * Default http port.
     */
    final public static int HTTP_PORT = 8040;

    /**
     * Default hint port.
     */
    final public static int HINT_PORT = 4040;

    /**
     * Returns a path to the user directory.
     *
     * @return Path to user directory.
     */
    public static String DIR() {
        File cwd = new File(System.getProperty("user.dir"));
        return (new File(cwd, "dir")).toString();
    }

    /**
     * Directory published by HTTP server.
     *
     * @return String representation of directory.
     */
    public static String HTTP_DIR() {
        File cwd = new File(System.getProperty("user.dir"));
        return (new File(cwd, "http")).toString();
    }

    /**
     * Returns a string representation of a randomly generated
     * signature key pair.
     *
     * @param rs Source of random bits.
     * @return String representation of a key pair.
     */
    public static String SignatureSKey(RandomSource rs) {
        SignatureKeyGen keygen = new SignatureKeyGenHeuristic(SEC_PARAM);
        SignatureKeyPair keyPair = keygen.gen(rs);
        return Marshalizer.marshalToHexHuman(keyPair, true);
    }

    /**
     * A lazy-evaluation version of {@link #SignatureSKey}.
     *
     * @param rs Source of random bits.
     * @return Object that can generate a string representation of a key
     * pair.
     */
    public static Object LazySignatureSKey(final RandomSource rs) {
        return new Lazy() {
            String skey = null;

            public String gen() {
                if (skey != null) {
                    return skey;
                } else {
                    skey = SignatureSKey(rs);
                    return skey;
                }
            }
        };
    }

    /**
     * Returns a string representation of the default {@link PGroup},
     * which is a {@link ModPGroup} instance.
     *
     * @return String representation of the default {@link PGroup}.
     */
    public static String PGroup() {
        try {
            PGroup pGroup = new ModPGroup(SEC_PARAM);
            return Marshalizer.marshalToHexHuman(pGroup, true);

        } catch (ArithmFormatException afe) {
            throw new ProtocolError("Bad constant!", afe);
        }
    }

    /**
     * Returns a string representation of the default hashfunction,
     * which is SHA-256.
     *
     * @return String representation of the default hashfunction.
     */
    public static String Hashfunction() {
        return "SHA-256";
    }

    /**
     * Returns a string representation of a key generator for the
     * Cramer-Shoup cryptosystem defined over the default group
     * defined by {@link #PGroup} and using SHA-256.
     *
     * @return String representation of the default key generator of a
     * cryptosystem.
     */
    public static String CryptoKeyGen() {
        try {
            PGroup pGroup = new ModPGroup(SEC_PARAM);
            Hashfunction hashfunction = new HashfunctionHeuristic("SHA-256");

            CryptoKeyGen keyGen =
                new CryptoKeyGenCramerShoup(pGroup, hashfunction);

            return Marshalizer.marshalToHexHuman(keyGen, true);

        } catch (ArithmFormatException afe) {
            throw new ProtocolError("Bad constant!", afe);
        }
    }

    /**
     * Returns a string representation of the default random device,
     * which is the Un*x standard pseudo-random device
     * <code>/dev/urandom</code>.
     *
     * @return String representation of the default random device.
     */
    public static String RandomDevice() {
        RandomDevice dev = new RandomDevice();
        return Marshalizer.marshalToHexHuman(dev, true);
    }

    /**
     * Returns a string representation of the default pseudo-random
     * generator (PRG).
     *
     * @return String representation of the default PRG.
     */
    public static String PRG() {
        return "SHA-256";
    }

    /**
     * Returns the default hostname, i.e., the hostname of the machine
     * executing this code. Depending on the configuration of the
     * server and the network, this may, or may not give a useful
     * result.
     *
     * @return Hostname of the machine executing this code.
     */
    public static String HOST() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            return ia.getHostName();
        } catch (UnknownHostException uhe) {
            throw new ProtocolError("Can not find out our own hostname!", uhe);
        }
    }

    /**
     * Returns a string representation of the default port address of
     * the hint server.
     *
     * @return String representation of the default port address of
     * the hint server.
     */
    public static String HINT() {
        return HOST() + ":" + HINT_PORT;
    }

    /**
     * Returns a string representation of the default local port
     * address of the hint server, i.e., the address to which the hint
     * server is bound.
     *
     * @return String representation of the default local port address
     * of the hint server.
     */
    public static String HINTL() {
        return HINT();
    }

    /**
     * Returns a string representation of the default local port
     * address of the http server, i.e., the address to which the http
     * server is bound.
     *
     * @return String representation of the default local port address
     * of the http server.
     */
    public static String HTTP() {
        return "http://" + HOST() + ":" + HTTP_PORT;
    }

    /**
     * Returns a string representation of the default local port
     * address of the http server, i.e., the address to which the http
     * server is bound.
     *
     * @return String representation of the default local port address
     * of the http server.
     */
    public static String HTTPL() {
        return HTTP();
    }
}
