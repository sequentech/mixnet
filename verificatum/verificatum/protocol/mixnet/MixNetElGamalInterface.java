
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
 * Interface of an El Gamal mix-net. This defines the format of: the
 * public key that is used by senders, the input ciphertexts, and the
 * output plaintexts.
 *
 * @author Douglas Wikstrom
 */
public abstract class MixNetElGamalInterface {

    /**
     * Returns the named interface.
     *
     * @param interfaceName Name of interface.
     * @return Requested interface.
     */
    @SuppressWarnings("unchecked")
    public static MixNetElGamalInterface getInterface(String interfaceName) {

        if (interfaceName.equals("raw")) {

            return new MixNetElGamalInterfaceRaw();

        } else if (interfaceName.equals("native")) {

            return new MixNetElGamalInterfaceNative();

        } else if (interfaceName.equals("helios")) {

            return new MixNetElGamalInterfaceHelios();

        } else if (interfaceName.equals("tvs")) {

            return new MixNetElGamalInterfaceTVS();

        } else {

            // If we don't recognize the string we assume that the
            // user has implemented his own interface class.
            try {

                Class klass = Class.forName(interfaceName);
                Constructor con = klass.getConstructor(new Class[0]);
                Object obj = con.newInstance();

                if (obj instanceof MixNetElGamalInterface) {
                    return (MixNetElGamalInterface)obj;
                } else {
                    throw new ProtocolError("Unknown interface!");
                }

            } catch (InvocationTargetException ite) {
                throw new ProtocolError("Unknown interface!", ite);
            } catch (IllegalAccessException iae) {
                throw new ProtocolError("Unknown interface!", iae);
            } catch (ClassNotFoundException cnfe) {
                throw new ProtocolError("Unknown interface!", cnfe);
            } catch (NoSuchMethodException nsme) {
                throw new ProtocolError("Unknown interface!", nsme);
            } catch (InstantiationException ie) {
                throw new ProtocolError("Unknown interface!", ie);
            }
        }
    }

    /**
     * Writes a full public key to file.
     *
     * @param fullPublicKey Full public key.
     * @param file Destination of representation of public key.
     */
    public abstract void writePublicKey(PGroupElement fullPublicKey,
                                        File file);

    /**
     * Reads a full public key from file.
     *
     * @param file Source of representation of public key.
     * @param randomSource Source of randomness.
     * @param certainty Determines the error probability when
     * verifying the representation of the underlying group.
     * @return Full public key.
     */
    public abstract PGroupElement readPublicKey(File file,
                                                RandomSource randomSource,
                                                int certainty)
        throws ProtocolFormatException;

    /**
     * Writes ciphertexts to file.
     *
     * @param ciphertexts Ciphertexts to be written.
     * @param file Destination of representation of ciphertexts.
     */
    public abstract void writeCiphertexts(PGroupElementArray ciphertexts,
                                          File file);

    /**
     * Reads ciphertexts from file.
     *
     * @param pGroup Group to which the ciphertexts belong.
     * @param file Source of representation of ciphertexts.
     * @return Ciphertexts.
     */
    public abstract PGroupElementArray readCiphertexts(PGroup pGroup,
                                                       File file)
        throws ProtocolFormatException;

    /**
     * Decodes the input plaintexts to file.
     *
     * @param plaintexts Plaintext elements.
     * @param file Destination of decoded messages.
     */
    public abstract void decodePlaintexts(PGroupElementArray plaintexts,
                                          File file);
}
