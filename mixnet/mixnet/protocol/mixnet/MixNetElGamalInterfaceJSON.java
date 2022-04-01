
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

// FORK (entire file) (plus change to elToString below)

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
 * Plaintext El Gamal submission scheme.
 *
 * ciphertexts are encoded as hexadecimal separated in lines and simply read
 * from a file. The outputs are easily decoded.
 */
public class MixNetElGamalInterfaceJSON
    extends MixNetElGamalInterfaceJSONDecode {

    /**
     * Converts a pgroup element into a string.
     *
     * @param plaintext Plaintext element.
     */
    protected String elToString(PGroupElement plaintext)
    {
        // FORK
        // return "\"" +
        //    ((ModPGroupElement)plaintext).toLargeInteger().toString(10) +
        //    "\"";
        ModPGroupElement el = (ModPGroupElement)plaintext;
        LargeInteger m = el.toLargeInteger();
        ModPGroup pGroup = (ModPGroup)el.getPGroup();

        // if m > publicKey.q, negate
        if (m.compareTo(pGroup.getPRing().getCharacteristic()) > 0)
        {
            m = m.neg().mod(pGroup.getModulus());
        }
        return "\"" + m.toString(10) + "\"";
    }

    /**
     * Decodes the input plaintexts to file.
     *
     * @param plaintexts Plaintext elements.
     * @param file Destination of decoded messages.
     */
    public String decodePlaintext(PGroupElement plaintext)
    {
        if (!(plaintext instanceof PPGroupElement))
        {
            return elToString(plaintext);
        }
        else
        {
            PGroupElement[] plaintexts = ((PPGroupElement)plaintext).getFactors();

            StringBuilder builder = new StringBuilder();
            builder.append("[");

            for (int i = 0; i < plaintexts.length; i++)
            {
                builder.append(elToString(plaintexts[i]));
                builder.append(",");
            }
            // remove the last comma
            builder.deleteCharAt(builder.length() - 1);

            builder.append("]");
            return builder.toString();
        }
    }

    /**
     * Generates the given number of ciphertexts.
     *
     * @param fullPublicKey Full public key.
     * @param noCiphs Number of ciphertexts to generate.
     * @param outputFile Destination of generated ciphertexts.
     * @param randomSource Source of randomness.
     */
    public void demoCiphertexts(PGroupElement fullPublicKey, int noCiphs,
                                File outputFile, RandomSource randomSource)
    {

        PGroupElement basicPublicKey = ((PPGroupElement)fullPublicKey).project(0);
        PGroupElement publicKey = ((PPGroupElement)fullPublicKey).project(1);
        PGroupElement[] m_a = new PGroupElement[noCiphs];

        ModPGroup modPGroup = null;
        if (!(publicKey instanceof PPGroupElement))
        {
            modPGroup = (ModPGroup)publicKey.getPGroup();
        } else {
            modPGroup = (ModPGroup)((PPGroup)publicKey.getPGroup()).project(0);
        }

        // Here we create the dummy plaintexts
        if (modPGroup.getEncoding() != ModPGroup.RO_ENCODING)
        {
            LargeInteger lint = new LargeInteger(0);

            for (int i = 0; i < noCiphs; i++) {
                while (!modPGroup.contains(lint))
                {
                    lint = lint.add(LargeInteger.ONE);
                }

                m_a[i] = modPGroup.toElement(lint);
                lint = lint.add(LargeInteger.ONE);
            }
        } else {
            PGroupElement pgel = modPGroup.getg();
            for(int i =0; i < noCiphs;i++) { m_a[i] = pgel; pgel = pgel.mul(pgel); }
        }

        // Here we encode the plaintexts as group elements
        PGroupElementArray modm = modPGroup.toElementArray(m_a);
        PGroupElementArray m = null;

        if (!(publicKey instanceof PPGroupElement))
        {
            m = modm;
        } else {
            m = ((PPGroup)publicKey.getPGroup()).product(modm);
        }

        // Here we encrypt the result
        PRing randomizerPRing = m.getPGroup().getPRing();
        PRingElementArray r = randomizerPRing.randomElementArray(noCiphs, randomSource, 20);
        PGroupElementArray u = basicPublicKey.exp(r);
        PGroupElementArray v = publicKey.exp(r).mul(m);
        PGroupElementArray ciphs = ((PPGroup)fullPublicKey.getPGroup()).product(u, v);

        // and finally write the result
        writeCiphertexts(ciphs, outputFile);
    }
}