
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
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.protocol.*;
import verificatum.ui.*;
import verificatum.util.*;
import verificatum.protocol.distrkeygen.*;


/**
 * Implements a plain El Gamal submission scheme, i.e.,
 * newline-separated hexadecimal coded ciphertexts are simply read
 * from a file.
 *
 * @author Douglas Wikstrom
 */
public class MixNetElGamalInterfaceHelios extends MixNetElGamalInterfaceString {

    public void writePublicKey(PGroupElement fullPublicKey, File file) {

        // We can trust the public key to be a pair, but not that each
        // component is a modular group.

        PPGroup pPGroup = (PPGroup)fullPublicKey.getPGroup();
        PGroup pGroupg = pPGroup.project(0);
        PGroup pGroupy = pPGroup.project(1);
        if (!(pGroupg instanceof ModPGroup)
            || !(pGroupy instanceof ModPGroup)) {

            String e = "Helios can only handle modular groups " +
                "(verificatum.arithm.ModPGroup).";
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
            map = SimpleJSON.fromJSON(publicKeyString);
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

    public String ciphertextToString(PGroupElement ciphertext) {

        // We can trust the public key to be a pair, but not that each
        // component is a modular group.

        PPGroup pPGroup = (PPGroup)ciphertext.getPGroup();
        PGroup pGroupg = pPGroup.project(0);
        PGroup pGroupy = pPGroup.project(1);
        if (!(pGroupg instanceof ModPGroup)
            || !(pGroupy instanceof ModPGroup)) {

            String e = "Helios can only handle modular groups " +
                "(verificatum.arithm.ModPGroup) and single ciphertexts.";
            throw new ProtocolError(e);
        }

        PPGroupElement pCiphertext = (PPGroupElement)ciphertext;

        ModPGroupElement alphaElement =
            (ModPGroupElement)pCiphertext.project(0);
        ModPGroupElement betaElement =
            (ModPGroupElement)pCiphertext.project(1);

        String format = "{\"alpha\":\"%s\",\"beta\":\"%s\"}";

        return String.format(format,
                             alphaElement.toLargeInteger(),
                             betaElement.toLargeInteger());
    }

    protected PGroupElement stringToCiphertext(PGroup ciphPGroup,
                                               String elementString)
    throws ProtocolFormatException {

        TreeMap<String, String> map = null;
        try {
            map = SimpleJSON.fromJSON(elementString);
        } catch (SimpleJSONException sje) {
            throw new ProtocolFormatException("Could not parse!", sje);
        }

        LargeInteger alpha = null;
        LargeInteger beta = null;

        if (map.containsKey("alpha")) {
            alpha = new LargeInteger(map.get("alpha"), 16);
        } else {
            throw new ProtocolFormatException("Missing alpha in ciphertext!");
        }
        if (map.containsKey("beta")) {
            beta = new LargeInteger(map.get("beta"), 16);
        } else {
            throw new ProtocolFormatException("Missing beta in ciphertext!");
        }

        // We can trust the public key to be a pair, but not that each
        // component is a modular group.

        PPGroup pPGroup = (PPGroup)ciphPGroup;
        PGroup pGroupg = pPGroup.project(0);
        PGroup pGroupy = pPGroup.project(1);
        if (!(pGroupg instanceof ModPGroup)
            || !(pGroupy instanceof ModPGroup)) {

            String e = "Helios can only handle modular groups " +
                "(verificatum.arithm.ModPGroup) and single ciphertexts!";
            throw new ProtocolError(e);
        }
        ModPGroup modPGroupg = (ModPGroup)pGroupg;
        ModPGroup modPGroupy = (ModPGroup)pGroupy;

        if (modPGroupg.contains(alpha) && modPGroupy.contains(beta)) {

            PGroupElement alphaElement = modPGroupg.toElement(alpha);
            PGroupElement betaElement = modPGroupy.toElement(beta);

            return ((PPGroup)ciphPGroup).product(alphaElement, betaElement);

        } else {
            throw new ProtocolFormatException("Malformed ciphertext!");
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
