
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

// FORK (entire file)

package vfork.protocol.mixnet;

import java.io.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.protocol.*;
import vfork.ui.*;
import vfork.util.*;
import vfork.protocol.distrkeygen.*;


/**
 * Implements a plain El Gamal submission scheme, i.e.,
 * newline-separated hexadecimal coded ciphertexts are simply read
 * from a file. The outputs can be easily decoded.
 *
 * @author Douglas Wikstrom
 */
public class MixNetElGamalInterfaceJSONDecode
    extends MixNetElGamalInterfaceString {

    public void writePublicKey(PGroupElement fullPublicKey, File file) {

        // We can trust the public key to be a pair, but not that each
        // component is a modular group.

        PPGroup pPGroup = (PPGroup)fullPublicKey.getPGroup();
        PGroup pGroupg = pPGroup.project(0);
        PGroup pGroupy = pPGroup.project(1);
        if (!(pGroupg instanceof ModPGroup)
            || !(pGroupy instanceof ModPGroup)) {

            String e = "The JSON format used can only handle modular groups " +
                "(vfork.arithm.ModPGroup).";
            throw new ProtocolError(e);
        }

        LargeInteger p = ((ModPGroup)pGroupg).getModulus();
        LargeInteger q = ((ModPGroup)pGroupy).getElementOrder();

        LargeInteger g =
            ((ModPGroupElement)((PPGroupElement)fullPublicKey).project(0)).
            toLargeInteger();
        LargeInteger y =
            ((ModPGroupElement)((PPGroupElement)fullPublicKey).project(1)).
            toLargeInteger();

        String form = "{\"g\":\"%s\",\"p\":\"%s\",\"q\":\"%s\",\"y\":\"%s\"}";

        try {
            ExtIO.writeString(file, String.format(form,
                                                  g.toString(10),
                                                  p.toString(10),
                                                  q.toString(10),
                                                  y.toString(10)));
        } catch (IOException ioe) {
            throw new ProtocolError("Unable to write public key!", ioe);
        }
    }

    public PGroupElement readPublicKey(File file,
                                       RandomSource randomSource,
                                       int certainty)
    throws ProtocolFormatException {

        String publicKeyString = null;
        try {
            publicKeyString = ExtIO.readString(file);
        } catch (IOException ioe) {
            throw new ProtocolFormatException("Unable to read public key!",
                                              ioe);
        }

        TreeMap<String, String> map = null;
        try {
            map = SimpleJSON.readMap(publicKeyString);
        } catch (SimpleJSONException sje) {
            throw new ProtocolFormatException("Could not parse!", sje);
        }

        if (map.size() != 4) {
            throw new ProtocolFormatException("Wrong number of values in map!");
        }

        LargeInteger p = null;
        LargeInteger q = null;
        LargeInteger g = null;
        LargeInteger y = null;

        if (map.containsKey("p")) {
            p = new LargeInteger(map.get("p"));
        } else {
            throw new ProtocolFormatException("Missing p in public key!");
        }
        if (map.containsKey("q")) {
            q = new LargeInteger(map.get("q"));
        } else {
            throw new ProtocolFormatException("Missing q in public key!");
        }
        if (map.containsKey("g")) {
            g = new LargeInteger(map.get("g"));
        } else {
            throw new ProtocolFormatException("Missing g in public key!");
        }
        if (map.containsKey("y")) {
            y = new LargeInteger(map.get("y"));
        } else {
            throw new ProtocolFormatException("Missing y in public key!");
        }

        try {
            int encoding = ModPGroup.SUBGROUP_ENCODING;
            if (q.mul(LargeInteger.TWO).add(LargeInteger.ONE).equals(p)) {
                encoding = ModPGroup.SAFEPRIME_ENCODING;
            }
            ModPGroup modPGroup = new ModPGroup(p, q, g,
                                                encoding,
                                                randomSource,
                                                certainty);
            PPGroup pPGroup = new PPGroup(modPGroup, 2);

            if (!modPGroup.contains(y)) {
                String s = "y does not represent a group element";
                throw new ProtocolFormatException(s);
            }
            return pPGroup.product(modPGroup.getg(), modPGroup.toElement(y));

        } catch (ArithmFormatException afe) {
            String e = "Bad integer values!";
            throw new ProtocolFormatException(e, afe);
        }
    }

    /**
     * Converts a pair to a codified string value.
     */
    public String pairToString(PGroupElement alpha, PGroupElement beta)
    {
        String format = "{\"alpha\":\"%s\",\"beta\":\"%s\"}";

        LargeInteger alphaLi = ((ModPGroupElement)alpha).toLargeInteger();
        LargeInteger betaLi = ((ModPGroupElement)beta).toLargeInteger();

        return String.format(format, alphaLi.toString(10), betaLi.toString(10));
    }

    public String ciphertextToString(PGroupElement ciphertext) {

        // We can trust the public key to be a pair, but not that each
        // component is a modular group.

        PPGroupElement pCiphertext = (PPGroupElement)ciphertext;

        if (pCiphertext.project(0) instanceof PPGroupElement) {

            PGroupElement[] alphas =
                ((PPGroupElement)pCiphertext.project(0)).getFactors();
            PGroupElement[] betas =
                ((PPGroupElement)pCiphertext.project(1)).getFactors();

            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int i = 0; i < alphas.length;i++)
            {
                String pairStr = pairToString(alphas[i], betas[i]);
                builder.append(pairStr);
                builder.append(",");
            }

            // remove the comma
            builder.deleteCharAt(builder.length() -1);

            builder.append("]");
            return builder.toString();

        }
        else
        {
            return pairToString(
                pCiphertext.project(0), pCiphertext.project(1));
        }
    }

    protected PGroupElement stringToCiphertext(PGroup ciphPGroup,
                                               String elementString)
    throws ProtocolFormatException {

        LargeInteger alphali = null;
        LargeInteger betali = null;

        PPGroup pCiphPGroup = (PPGroup)ciphPGroup;
        PGroup elPGroup = pCiphPGroup.project(0);

        if (elPGroup instanceof PPGroup) {

            PPGroup pelPGroup = (PPGroup)elPGroup;
            ModPGroup modPGroup = (ModPGroup)pelPGroup.project(0);

            ArrayList<TreeMap<String, String>> maps = null;
            try {
                maps = SimpleJSON.readMaps(elementString);
            }
            catch (SimpleJSONException sjexcept)
            {
                throw new ProtocolFormatException("Could not parse elements",
                    sjexcept);
            }

            PGroupElement[] alphas = new PGroupElement[maps.size()];
            PGroupElement[] betas = new PGroupElement[maps.size()];

            for (int i = 0; i < alphas.length; i++)
            {
                TreeMap<String, String> map = maps.get(i);
                if (map.containsKey("alpha"))
                {
                    alphali = new LargeInteger(map.get("alpha"),10);
                }
                else
                {
                    throw new ProtocolFormatException("Missing alpha value");
                }


                if (map.containsKey("beta"))
                {
                    betali = new LargeInteger(map.get("beta"),10);
                }
                else
                {
                    throw new ProtocolFormatException("Missing beta value");
                }

                alphas[i] = modPGroup.toElement(alphali);
                betas[i] = modPGroup.toElement(betali);
            }

            PGroupElement pgEl = pCiphPGroup.product(
                pelPGroup.product(alphas), pelPGroup.product(betas));
            return pgEl;

        } else {

            ModPGroup modPGroup = (ModPGroup)elPGroup;
            TreeMap<String, String> map = null;

            try {
                map = SimpleJSON.readMap(elementString);
            }
            catch (SimpleJSONException sjexcept)
            {
                throw new ProtocolFormatException("Could not parse elements",
                    sjexcept);
            }

            if (map.containsKey("alpha"))
            {
                alphali = new LargeInteger(map.get("alpha"),10);
            }
            else
            {
                throw new ProtocolFormatException("Missing alpha value");
            }

            if (map.containsKey("beta"))
            {
                betali = new LargeInteger(map.get("beta"), 10);
            } else
            {
                throw new ProtocolFormatException("Missing beta value");
            }

            return pCiphPGroup.product(
                modPGroup.toElement(alphali), modPGroup.toElement(betali));
        }
    }

    public String decodePlaintext(PGroupElement plaintext) {
        try {
            String s = new String(plaintext.decode(), "UTF8");
            return s.replaceAll("\n", "").replaceAll("\r", "");
        } catch (UnsupportedEncodingException uee) {
            throw new ProtocolError("Unable to decode plaintext!", uee);
        }
    }
}
