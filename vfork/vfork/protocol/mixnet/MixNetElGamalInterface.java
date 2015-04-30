
/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 * Copyright 2015 Eduardo Robles
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

package vfork.protocol.mixnet;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.protocol.*;
import vfork.ui.*;
// FORK
import vfork.ui.info.*;
// FORK
import vfork.ui.opt.*;
// FORK
import vfork.*;

/**
 * Interface of an El Gamal mix-net. This defines the format of: the
 * public key that is used by senders, the input ciphertexts, and the
 * output plaintexts.
 *
 * @author Douglas Wikstrom
 */
public abstract class MixNetElGamalInterface {

    // FORK
    /**
     * Certainty: determines if there's an error at the moment of checking
     * consistency of data. Bad data is accepted with some quantity of
     * probability, not superior to this formula:  2^(-certainty).
     */
    public static final int CERTAINTY = 100;

    /**
     * Returns the named interface.
     *
     * @param interfaceName Name of interface.
     * @return Requested interface.
     */
    @SuppressWarnings("unchecked")
    public static MixNetElGamalInterface getInterface(String interfaceName) {

        // FORK added json and json decode, removed helios
        if (interfaceName.equals("raw")) {

            return new MixNetElGamalInterfaceRaw();

        } else if (interfaceName.equals("native")) {

            return new MixNetElGamalInterfaceNative();

        }
        else if (interfaceName.equals("json"))
        {

            return new  MixNetElGamalInterfaceJSON();
        }
        else if (interfaceName.equals("jsondecode"))
        {

            return new  MixNetElGamalInterfaceJSONDecode();

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

    // FORK
    /**
     * This function returns a random source, initialized and defined
     * by parameters.
     *
     * @param rsFile This is a file, that should contain a string to be used as
     * iinput to {@link Marshalizer#unmarshalHex_RandomSource(String)}.
     * @param seedFile When the random source is a {@link PRG}, it must
     * contain a seed file large enough.
     * @param tmpSeedFile Seed file used temporarily for implementing the
     * atomic write of a new seed.
     *
     * @return a Random source.

     * @throws ProtocolError When creating a random source from the data
     * isn't possible for some reason.
     */
    public static RandomSource standardRandomSource(File rsFile,
                                                    File seedFile,
                                                    File tmpSeedFile) {
        try {
            String rsStr = ExtIO.readString(rsFile);
            RandomSource rndSource = Marshalizer.unmarshalHex_RandomSource(rsStr);

            // If the random source is a PRG (Prime Number Generator),
            // then there should be a long enough associated seed file.
            if (rndSource instanceof PRG)
            {
                try
                {
                    ((PRG)rndSource).setSeedReplaceStored(seedFile, tmpSeedFile);
                } catch (IOException ioe)
                {
                    throw new ProtocolError("Unable to read/write for some " +
                        "reason the PRG seed file. Check that \"" + seedFile +
                        "\" is readable and writeable, and is large enough.",
                        ioe);
                }
            }
            return rndSource;
        } catch (EIOException eioe)
        {
            throw new ProtocolError("Couldn't create the random source. " +
                "Check that " + rsFile + " is valid!", eioe);
        } catch (IOException ioe)
        {
            throw new ProtocolError("Unable to read the random source for " +
                "some reason.", ioe);
        }
    }

    // FORK
    /**
     * This function generates an option that represents the multiple forms
     * in which the protocol can be used/invoked.
     *
     * @param commandName This is the name of the command to be executed by
     * the user to invoke the protocol. For example, the name of the sh
     * script wrapper.
     * @return An instanced Opt that represents the multiple forms
     * in which the protocol can be used/invoked.
     */
    protected static Opt opt(String commandName)
    {
        Opt opt = new Opt(
            commandName,
            "Invalid usage, please check how " + "this protocol can be used " +
            "with \"" + commandName + " -h\"");

        opt.addParameter("protocolInfo", "Protocol-info file.");

        opt.addParameter("in", "Src object to be converted.");
        opt.addParameter("out", "Dest for the converted object.");

        opt.addOption("-version", "", "Prints the vfork version and lists " +
                      "versions of the protocol that it can handle.");
        opt.addOption("-h", "", "Print how the protocol can be used.");

        opt.addOption("-ini", "name", "Mixnet iface used for representing " +
            "the input. Defaults to the \"raw\" interface.");
        opt.addOption("-outi", "name", "Mixnet iface used for representing " +
            "the output. Defaults to the \"raw\" interface.");

        opt.addOption("-width", "value", "Num of ciphertexts to be used as a" +
            "considered as a single block.");

        opt.addOption("-plain", "", "Use to decode plaintexts.");
        opt.addOption("-ciphs", "", "Use to convert some ciphertexts.");
        opt.addOption("-pkey", "", "USe to convert a public key.");

        // explain some usages
        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1,"-pkey#-ini,-outi#protocolInfo,in,out#");
        opt.addUsageForm();

        opt.appendToUsageForm(2, "-ciphs#-ini,-outi,-width#protocolInfo,in,out#");
        opt.addUsageForm();

        opt.appendToUsageForm(3, "-plain#-outi,-width#protocolInfo,in,out#");

        opt.addUsageForm();
        opt.appendToUsageForm(4, "-version###");

        // final description
        opt.appendDescription("Used for Converting ciphertexts, public keys and " +
            "plaintexts from different representations. The input & output " +
            "representations are specified using the \"-ini\" and \"-outi\" " +
            "options.\n\n" +
            "The values that can be used for input or output interfaces are: " +
            "\"raw\", \"native\", \"json\", \"tvs\". Also any name of a " +
            "class inheriting vfork.protocol.mixnet.MixNetElGamalInterface.");

        return opt;
    }

    /**
     * FORK
     */
    public static void main(String[] args)
    {
        int givenCertainty = CERTAINTY;

        // check enough args are given
        if (args.length < 3)
        {
            System.err.println("Not enough arguments: usage is " + args[0] +
                " <rndSource> <seedFile>");
        }

        // init from cmd args
        String cmdName = args[0];


        File rsFile = new File(args[1]),
             tmprsFile = new File(args[1] + "_TMP");
        File seedFile = new File(args[2]),
             tmpSeedFile = new File(args[2] + "_TMP");

        RandomSource randomSource = standardRandomSource(rsFile, seedFile, tmpSeedFile);
        // make a copy
        args = Arrays.copyOfRange(args, 3,args.length);

        Opt opt = opt(cmdName);
        try {
            opt.parse(args);

        } catch (OptException oexception)
        {
            // When parsing fails, assume the user is executing the commands
            // "in the working directory". This way, we can make another
            // attempt with default filenames.

            boolean patching = false;
            if (args.length > 3)
            {
                String protInfoFName = "protInfo.xml";

                String[] args2 = new String[args.length +1];
                System.arraycopy(args, 0, args2, 0, args.length -2);
                args2[args.length - 2] = protInfoFName;

                System.arraycopy(args, args.length - 2, args2, args2.length - 2,
                    2);

                opt = opt(cmdName);

                try { opt.parse(args2); patching = true; }
                catch (OptException oe2) {
                    // nothing, we'll detect this with !patching
                }
            }
            if (!patching) {
                System.err.println("\n" + "ERROR: " + oexception.getMessage() + "\n");
                System.exit(0);
            }
        }

        if (opt.getBooleanValue("-version"))
        {
            System.out.println(Version.packageVersion + " (" +
                MixNetElGamal.compatibleProtocolVersions() + ")");
            System.exit(0);
        }
        else if(opt.getBooleanValue("-h"))
        {
            System.out.println(opt.usage()); System.exit(0);
        }

        // read protinfo and privinfo files
        InfoGenerator generator = new MixNetElGamalGen();
        ProtocolInfo protInfo = generator.newProtocolInfo();

        try { protInfo.parse(opt.getStringValue("protocolInfo")); }
        catch (InfoException ie)
        {
            throw new ProtocolError("Failed to parse protocolInfo file", ie);
        }

        int width = protInfo.getIntValue("width");
        // derive width if public key is not provided
        if (!opt.getBooleanValue("-pkey"))
        {
            width = MixNetElGamal.deriveWidth(width, opt);
        }

        // extract group over which we will execute the protocol
        String pGroupStr = protInfo.getStringValue("pgroup");
        PGroup pGroup = null;

        try {
            pGroup = Marshalizer.unmarshalHexAux_PGroup(pGroupStr, randomSource,
                givenCertainty);
        }
        catch (EIOException eioe)
        {
            throw new ProtocolError("Group can't be parsed", eioe);
        }

        // raw iface is the default
        String inIfaceStr = "raw";
        if( opt.valueIsGiven("-ini") )
        {
            inIfaceStr = opt.getStringValue("-ini");
        }
        String outIfaceStr = "raw";
        if( opt.valueIsGiven("-outi") )
        {
            outIfaceStr = opt.getStringValue("-outi");
        }

        try
        {
            MixNetElGamalInterface ini = getInterface(inIfaceStr);
            MixNetElGamalInterface outi = getInterface(outIfaceStr);

            // read the input files
            File inFile = new File(opt.getStringValue("in"));
            File outFile = new File(opt.getStringValue("out"));

            if( opt.getBooleanValue("-pkey") )
            {
                // try to read the given full public key
                try
                {
                    PGroupElement fullPubKey = ini.readPublicKey(inFile,
                        randomSource, givenCertainty);
                    outi.writePublicKey(fullPubKey, outFile);

                } catch (ProtocolFormatException pfexcept)
                {
                    throw new ProtocolError(pfexcept.getMessage(), pfexcept);
                }

            }
            else if (opt.getBooleanValue("-ciphs"))
            {
                // try to read the ciphertexts
                try
                {
                    PGroup ciphPGroup = null;
                    if(width != 1)
                    {
                        ciphPGroup = new PPGroup(new PPGroup(pGroup, width), 2);
                    } else {
                        ciphPGroup = new PPGroup(pGroup, 2);
                    }

                    PGroupElementArray ciphertexts = ini.readCiphertexts(ciphPGroup, inFile);
                    outi.writeCiphertexts(ciphertexts, outFile);

                } catch (ProtocolFormatException pfexcept)
                {
                    throw new ProtocolError(pfexcept.getMessage(), pfexcept);
                }

            }
            else if (opt.getBooleanValue("-plain"))
            {
                // try to read the plaintexts given
                try
                {
                    PGroup plainPGroup = pGroup;
                    if (width != 1)
                    {
                        plainPGroup = new PPGroup(pGroup, width);
                    }

                    ByteTreeReader plainReader = (new ByteTreeF(inFile)).getByteTreeReader();

                    PGroupElementArray plaintexts = plainPGroup.toElementArray(
                        0, plainReader);

                    outi.decodePlaintexts(plaintexts, outFile);

                } catch (ArithmFormatException afe)
                {
                    throw new ProtocolError(
                        "Unable to read the given plaintexts", afe);
                }
            }

        } catch (ProtocolError pe)
        {
            System.err.println("\n" + "ERROR: " + pe.getMessage() + "\n");
            System.exit(0);
        }
    }
}
