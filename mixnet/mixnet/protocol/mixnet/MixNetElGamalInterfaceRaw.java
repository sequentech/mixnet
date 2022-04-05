
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

/**
 * Raw interface of an El Gamal mix-net.
 *
 * @author Douglas Wikstrom
 */
public class MixNetElGamalInterfaceRaw extends MixNetElGamalInterfaceDemo {

    public void writePublicKey(PGroupElement fullPublicKey, File file) {
        PGroup pGroup = ((PPGroupElement)fullPublicKey).project(0).getPGroup();
        ByteTreeBasic gbt = Marshalizer.marshal(pGroup);
        ByteTreeBasic kbt = fullPublicKey.toByteTree();

        (new ByteTreeContainer(gbt, kbt)).unsafeWriteTo(file);
    }

    public PGroupElement readPublicKey(File file,
                                       RandomSource randomSource,
                                       int certainty)
    throws ProtocolFormatException {

        ByteTreeReader btr = null;
        try {

            ByteTreeBasic bt = new ByteTreeF(file);
            btr = bt.getByteTreeReader();
            PGroup pGroup = Marshalizer.unmarshalAux_PGroup(btr.getNextChild(),
                                                            randomSource,
                                                            certainty);
            return (new PPGroup(pGroup, 2)).toElement(btr.getNextChild());

        } catch (EIOException eioe) {
            throw new ProtocolFormatException("Malformed key!", eioe);
        } catch (ArithmFormatException afe) {
            throw new ProtocolFormatException("Malformed key!", afe);
        } finally {
            if (btr != null) {
                btr.close();
            }
        }
    }

    public void writeCiphertexts(PGroupElementArray ciphertexts, File file) {
        ciphertexts.toByteTree().unsafeWriteTo(file);
    }

    public PGroupElementArray readCiphertexts(PGroup pGroup, File file)
    throws ProtocolFormatException {

        try {

            ByteTreeBasic bt = new ByteTreeF(file);

            PGroupElementArray res = null;
            ByteTreeReader btr = null;
            try {

                btr = bt.getByteTreeReader();
                res = pGroup.toElementArray(0, btr);

            } finally {
                btr.close();
            }
            return res;

        } catch (ArithmFormatException afe) {
            throw new ProtocolFormatException("Malformed ciphertexts!", afe);
        }
    }

    public void decodePlaintexts(PGroupElementArray plaintexts, File file) {

        plaintexts.toByteTree().unsafeWriteTo(file);
    }

    public void demoCiphertexts(PGroupElement fullPublicKey,
                                int noCiphs,
                                File outputFile,
                                RandomSource randomSource) {

        PGroupElement basicPublicKey =
            ((PPGroupElement)fullPublicKey).project(0);
        PGroupElement publicKey =
            ((PPGroupElement)fullPublicKey).project(1);

        PRing pRing = publicKey.getPGroup().getPRing();
        PRingElementArray exponents = pRing.randomElementArray(noCiphs,
                                                               randomSource,
                                                               20);
        PGroupElementArray m = publicKey.exp(exponents);

        PRingElementArray r =
            pRing.randomElementArray(noCiphs, randomSource, 20);

        PGroupElementArray u = basicPublicKey.exp(r);
        PGroupElementArray v = publicKey.exp(r).mul(m);

        PGroupElementArray ciphs =
            ((PPGroup)fullPublicKey.getPGroup()).product(u, v);

        writeCiphertexts(ciphs, outputFile);
    }

}
