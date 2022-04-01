
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

package mixnet.protocol.mixnet;

import java.io.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.protocol.*;
import mixnet.ui.*;
import mixnet.util.*;
import mixnet.protocol.distrkeygen.*;


/**
 * Implements a plain El Gamal submission scheme, i.e.,
 * newline-separated hexadecimal coded ciphertexts are simply read
 * from a file.
 *
 * @author Douglas Wikstrom
 */
public class MixNetElGamalInterfaceNative extends MixNetElGamalInterfaceString {

    public void writePublicKey(PGroupElement fullPublicKey, File file) {

        PGroup pGroup = ((PPGroupElement)fullPublicKey).project(0).getPGroup();
        ByteTreeBasic gbt = Marshalizer.marshal(pGroup);
        ByteTreeBasic kbt = fullPublicKey.toByteTree();

        byte[] keyBytes = (new ByteTreeContainer(gbt, kbt)).toByteArray();

        try {
            ExtIO.writeString(file, Hex.toHexString(keyBytes));

        } catch (IOException ioe) {
            throw new ProtocolError("Unable to write public key!", ioe);
        }
    }

    public PGroupElement readPublicKey(File file,
                                       RandomSource randomSource,
                                       int certainty)
    throws ProtocolFormatException {

        try {

            String publicKeyString = ExtIO.readString(file);
            byte[] keyBytes = Hex.toByteArray(publicKeyString);
            ByteTreeReader btr =
                (new ByteTree(keyBytes, null)).getByteTreeReader();

            PGroup pGroup = Marshalizer.unmarshalAux_PGroup(btr.getNextChild(),
                                                            randomSource,
                                                            certainty);
            return (new PPGroup(pGroup, 2)).toElement(btr.getNextChild());

        } catch (IOException ioe) {
            throw new ProtocolFormatException("Malformed key!", ioe);
        } catch (EIOException eioe) {
            throw new ProtocolFormatException("Malformed key!", eioe);
        } catch (ArithmFormatException afe) {
            throw new ProtocolFormatException("Malformed key!", afe);
        }
    }

    public String ciphertextToString(PGroupElement ciphertext) {
        byte[] bytes = ciphertext.toByteTree().toByteArray();
        return Hex.toHexString(bytes);
    }

    protected PGroupElement stringToCiphertext(PGroup ciphPGroup,
                                               String ciphertextString)
    throws ProtocolFormatException {

        try {
            byte[] bytes = Hex.toByteArray(ciphertextString);
            ByteTree bt = new ByteTree(bytes, null);
            ByteTreeReader btr = bt.getByteTreeReader();

            return ciphPGroup.toElement(btr);
        } catch (EIOException eioe) {
            throw new ProtocolFormatException("Unable to parse ciphertext!",
                                              eioe);
        } catch (ArithmFormatException afe) {
            throw new ProtocolFormatException("Unable to parse ciphertext!",
                                              afe);
        }
    }
}
