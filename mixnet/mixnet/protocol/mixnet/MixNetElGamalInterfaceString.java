
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
public abstract class MixNetElGamalInterfaceString
    extends MixNetElGamalInterfaceDemo {

    public final static int CIPHERTEXT_BUFFER_SIZE = 100 * 1000;

    /**
     * Returns a string representation of a ciphertext.
     *
     * @param ciphertext Ciphertext to be converted.
     * @return Representation of ciphertext.
     */
    protected abstract String ciphertextToString(PGroupElement ciphertext);

    public void writeCiphertexts(PGroupElementArray ciphertexts, File file) {

        PrintStream ps = null;
        try {

            FileOutputStream fos = new FileOutputStream(file);
            ps = new PrintStream(new BufferedOutputStream(fos));

            PGroupElementIterator pgei = ciphertexts.getIterator();

            PGroupElement ciphertext = null;
            while ((ciphertext = pgei.next()) != null) {
                ps.println(ciphertextToString(ciphertext));
            }

        } catch (IOException ioe) {
            throw new ProtocolError("Unable to write ciphertexts!", ioe);
        } finally {
            if (ps != null) {
                ExtIO.strictClose(ps);
            }
        }
    }

    /**
     * Returns the group element from the given representation.
     *
     * @param ciphPGroup Group to which the ciphertext belongs.
     * @param ciphertextString Representation of ciphertext.
     * @return Ciphertext recovered from the representation.
     */
    protected abstract PGroupElement
        stringToCiphertext(PGroup ciphPGroup, String ciphertextString)
        throws ProtocolFormatException;

    public PGroupElementArray readCiphertexts(PGroup pGroup, File file)
    throws ProtocolFormatException {

        ArrayList<PGroupElementArray> ciphertextArrays =
            new ArrayList<PGroupElementArray>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException fnfe) {
            throw new ProtocolFormatException("Can not find file (" +
                                              file.toString() + ")", fnfe);
        }

        try {

            boolean moreLines = true;

            while (moreLines) {

                ArrayList<PGroupElement> ciphertextList =
                    new ArrayList<PGroupElement>();

                while (ciphertextList.size() < CIPHERTEXT_BUFFER_SIZE) {

                    String line = br.readLine();
                    if (line != null) {

                        try {

                            ciphertextList.add(stringToCiphertext(pGroup,
                                                                  line));

                        } catch (Exception e) {
                            // Ignore badly formatted ciphertexts.
                        }

                    } else {

                        moreLines = false;
                        break;

                    }
                }

                if (ciphertextList.size() > 0) {

                    PGroupElement[] ciphertexts =
                        ciphertextList.toArray(new PGroupElement[0]);
                    PGroupElementArray ciphertextArray =
                        pGroup.toElementArray(ciphertexts);
                    ciphertextArrays.add(ciphertextArray);

                }
            }
            PGroupElementArray[] resArrays =
                ciphertextArrays.toArray(new PGroupElementArray[0]);

            PGroupElementArray res = pGroup.toElementArray(resArrays);

            for (int i = 0; i < resArrays.length; i++) {
                resArrays[i].free();
            }
            return res;

        } catch (IOException ioe) {
            throw new ProtocolFormatException("Unable to read from file!", ioe);
        } finally {
            ExtIO.strictClose(br);
        }
    }

    /**
     * Decodes a plaintext element to a string.
     *
     * @param plaintext Plaintext element to be decoded.
     * @return String embedded in the given group element.
     */
    protected String decodePlaintext(PGroupElement plaintext) {
        try {
            String s = new String(plaintext.decode(), "UTF8");
            return s.replaceAll("\n", "").replaceAll("\r", "");
        } catch (UnsupportedEncodingException uee) {
            throw new ProtocolError("Unable to decode plaintext!", uee);
        }
    }

    public void decodePlaintexts(PGroupElementArray plaintexts, File file) {

        PrintStream ps = null;
        try {

            File unsortedFile = TempFile.getFile();
            FileOutputStream fos = new FileOutputStream(unsortedFile);
            ps = new PrintStream(fos);

            PGroupElementIterator pgei = plaintexts.getIterator();

            PGroupElement plaintext = null;
            while ((plaintext = pgei.next()) != null) {

                ps.println(decodePlaintext(plaintext));
            }

            try {
                ExtIO.sort(unsortedFile, file);
            } catch (InterruptedException ie) {
                throw new ProtocolError("Unable to sort plaintexts!", ie);
            }
            unsortedFile.delete();

        } catch (IOException ioe) {
            throw new ProtocolError("Unable to read from file!", ioe);
        } finally {
            ExtIO.strictClose(ps);
        }
    }




    public static String zeroString(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append("0");
        }
        return sb.toString();
    }

    public void demoCiphertexts(PGroupElement fullPublicKey,
                                int noCiphs,
                                File outputFile,
                                RandomSource randomSource) {

        PGroupElement basicPublicKey =
            ((PPGroupElement)fullPublicKey).project(0);
        PGroupElement publicKey =
            ((PPGroupElement)fullPublicKey).project(1);

        PGroup publicKeyPGroup = publicKey.getPGroup();

        PGroupElement[] encodedAlphabet = null;

        boolean alphabetical = false;
        if (publicKeyPGroup.getEncodeLength() < 2) {

            alphabetical = true;

            encodedAlphabet = new PGroupElement[26];
            byte[] letter = new byte[1];

            for (int i = 0; i < 26; i++) {
                letter[0] = (byte)(i + 65);
                encodedAlphabet[i] =
                    publicKeyPGroup.encode(letter, 0, letter.length);
            }
        }

        String zerosString = zeroString(publicKeyPGroup.getEncodeLength());

        // Generate dummy plaintexts.
        PGroupElement[] m_a = new PGroupElement[noCiphs];

        for (int i = 0; i < noCiphs; i++) {

            byte[] iBytes;

            if (alphabetical) {

                m_a[i] = encodedAlphabet[i % 26];

            } else {

                String iString = Integer.toString(i);
                iString = zerosString.substring(iString.length()) + iString;
                iBytes = iString.getBytes();
                m_a[i] = publicKeyPGroup.encode(iBytes, 0, iBytes.length);
            }
        }

        // Encode plaintexts as group elements.
        PGroupElementArray m = publicKeyPGroup.toElementArray(m_a);

        // Encrypt the result.
        PRing randomizerPRing = publicKeyPGroup.getPRing();

        PRingElementArray r =
            randomizerPRing.randomElementArray(noCiphs, randomSource, 20);

        PGroupElementArray u = basicPublicKey.exp(r);

        PGroupElementArray v = publicKey.exp(r).mul(m);

        PGroupElementArray ciphs =
            ((PPGroup)fullPublicKey.getPGroup()).product(u, v);

        writeCiphertexts(ciphs, outputFile);
    }
}
