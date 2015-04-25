
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
import java.text.*;

import verificatum.*;
import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.protocol.*;
import verificatum.util.*;
import verificatum.ui.info.*;
import verificatum.ui.opt.*;

/**
 * Generates a file containing newline separated hexadecimal encoded
 * El Gamal ciphertexts of numbered dummy messages. This is used for
 * debugging and demonstrations.
 *
 * @author Douglas Wikstrom
 */
public class MixNetElGamalDemo {

    /**
     * Command line interface.
     *
     * @param args Command line arguments
     *
     * @throws Exception If something goes wrong.
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Missing command name!");
        }
        String commandName = args[0];

        args = Arrays.copyOfRange(args, 1, args.length);

        String defaultErrorString =
            "Invalid usage form, please use \"" + commandName +
            " -h\" for usage information!";

        Opt opt = new Opt(commandName, defaultErrorString);

        opt.addParameter("protocolInfo", "Protocol info file.");
        opt.addParameter("publicKey", "Public key.");
        opt.addParameter("noCiphs", "Number of ciphertexts generated.");
        opt.addParameter("ciphertexts",
                         "Destination of generated ciphertexts.");

        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-version", "", "Print the package version.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "##protocolInfo,publicKey,noCiphs,ciphertexts#");

        opt.addUsageForm();
        opt.appendToUsageForm(2, "-version###");

        String s =
"Generates demo ciphertexts for the interface specified in the protocol info " +
"file. The protocol info file can be dropped if there is a protocol info " +
"named protInfo.xml in the working directory.";

        opt.appendDescription(s);

        if (args.length == 3) {
            String[] newargs = new String[args.length + 1];
            System.arraycopy(args, 0, newargs, 1, args.length);
            newargs[0] = "protInfo.xml";
            args = newargs;
        }

        try {

            try {
                opt.parse(args);
            } catch (OptException oe) {
                throw new ProtocolException(oe.getMessage(), oe);
            }

            if (opt.getBooleanValue("-h")) {

                System.out.println(opt.usage());
                System.exit(0);

            } else if (opt.getBooleanValue("-version")) {

                System.out.println(Version.packageVersion);
                System.exit(0);

            } else {

                // Extract the interface from the protocol info file.

                InfoGenerator generator = new MixNetElGamalGen();
                ProtocolInfo protocolInfo = generator.newProtocolInfo();

                try {
                    protocolInfo.parse(opt.getStringValue("protocolInfo"));
                } catch (InfoException ie) {
                    throw new ProtocolError("Failed to parse info files!", ie);
                }
                String interfaceName = protocolInfo.getStringValue("inter");

                MixNetElGamalInterface mixnetInterface = null;
                try {
                    mixnetInterface =
                        MixNetElGamalInterface.getInterface(interfaceName);
                } catch (ProtocolError pe) {
                    System.out.println("Unable to instantiate interface! (" +
                                       interfaceName + ")");
                    System.exit(1);
                }

                if (!(mixnetInterface instanceof MixNetElGamalInterfaceDemo)) {

                    throw new ProtocolException("The interface does not " +
                                                "support demo ciphertext " +
                                                "generation!");
                }

                MixNetElGamalInterfaceDemo mixnetInterfaceDemo =
                    (MixNetElGamalInterfaceDemo)mixnetInterface;

                // Read public key.
                RandomSource randomSource = new PRGHeuristic();
                int certainty = 100;

                File publicKeyFile = new File(opt.getStringValue("publicKey"));
                PGroupElement fullPublicKey = null;
                try {
                    fullPublicKey =
                        mixnetInterface.readPublicKey(publicKeyFile,
                                                      randomSource,
                                                      certainty);
                } catch (ProtocolFormatException pfe) {
                    throw new ProtocolException(pfe.getMessage(), pfe);
                }

                // Expand public key with respect to the width.
                int width = protocolInfo.getIntValue("width");

                PGroupElement basicPublicKey =
                    ((PPGroupElement)fullPublicKey).project(0);
                PGroupElement publicKey =
                    ((PPGroupElement)fullPublicKey).project(1);

                PGroup basicPublicKeyPGroup = basicPublicKey.getPGroup();
                PGroup publicKeyPGroup = publicKey.getPGroup();

                if (width > 1) {

                    basicPublicKeyPGroup =
                        new PPGroup(basicPublicKeyPGroup, width);
                    publicKeyPGroup = new PPGroup(publicKeyPGroup, width);
                    PPGroup fullPublicKeyPGroup =
                        new PPGroup(basicPublicKeyPGroup, publicKeyPGroup);

                    basicPublicKey =
                        ((PPGroup)basicPublicKeyPGroup).product(basicPublicKey);
                    publicKey =
                        ((PPGroup)publicKeyPGroup).product(publicKey);
                    fullPublicKey =
                        fullPublicKeyPGroup.product(basicPublicKey, publicKey);
                }

                // Determine number of ciphertexts.
                int noCiphs = opt.getIntValue("noCiphs");

                // Destination of the ciphertexts.
                File ciphertexts = new File(opt.getStringValue("ciphertexts"));

                mixnetInterfaceDemo.demoCiphertexts(fullPublicKey,
                                                    noCiphs,
                                                    ciphertexts,
                                                    randomSource);
            }
        } catch (ProtocolException pe) {

            String e = "\n" + "ERROR: " + pe.getMessage() + "\n";
            System.err.println(e);
        }
    }
}
