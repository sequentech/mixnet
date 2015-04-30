
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

package vfork.protocol.demo;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.ui.*;
import vfork.ui.gui.*;
import vfork.protocol.*;
import vfork.ui.info.*;
import vfork.ui.demo.*;
import vfork.ui.gen.*;
import vfork.ui.opt.*;
import vfork.util.*;

/**
 * Demonstrates a protocol by simulating each party as a
 * <code>Runnable</code>. To use this class, the implementor of a
 * protocol must write a factory class implementing the interface
 * {@link DemoProtocolFactory}.
 *
 * @author Douglas Wikstrom
 */
public class Demo {

    /**
     * Default length of seed to PRG.
     */
    protected final int DEFAULT_SEED_LENGTH = 10000;

    /**
     * Default name of working directory.
     */
    protected final String WORKING_DIRECTORY_NAME = "dir";

    /**
     * Name of the program running this class.
     */
    protected String commandName;

    /**
     * Demo directory.
     */
    protected File demoDir;

    /**
     * Protocol factory.
     */
    protected DemoProtocolFactory factory;

    /**
     * Signature key pairs of simulated parties.
     */
    protected SignatureKeyPair[] signatureKeyPairs;

    /**
     * Executes the generator template given as input.
     *
     * @param opt Options given by the user.
     * @param optString Option name to extract.
     * @param defString Representation of generator template.
     * @return String representation of generator template.
     */
    public String template(Opt opt, String optString, String defString) {
        try {
        GeneratorTemplate gt =
            new GeneratorTemplate(GeneratorTemplate.CPY, defString);
        String gtString = Marshalizer.marshalToHexHuman(gt, true);
        return GeneratorTemplate.
            execute(opt.getStringValue(optString, gtString));
        } catch (GenException ge) {
            throw new DemoError("Unable to generate from template!", ge);
        }
    }

    /**
     * Normalizes a URL to a fixed format.
     *
     * @param urlString String representing a URL.
     * @return String representing a normalized URL.
     */
    String normalizeURLString(String urlString) {
        if (!urlString.startsWith("http://")) {
            urlString = "http://" + urlString;
        }
        if (urlString.charAt(urlString.length() - 1) == '/') {
            urlString = urlString.substring(0, urlString.length() - 1);
        }
        return urlString;
    }

    /**
     * Adds default <code>PartyInfo</code> instances to the given
     * <code>ProtocolInfo</code> instance.
     *
     * @param pi Where the <code>PartyInfo</code>'s are added.
     * @param opt Parsed command line options.
     */
    public void addDefaultPartyInfos(ProtocolInfo pi, Opt opt) {

        int k = opt.getIntValue("-nopart", DemoConstants.NO_PARTIES);

        boolean httpext = false;
        String http = null;
        int http_port = 0;

        if (opt.getBooleanValue("-httpext")) {
            http = opt.getStringValue("-httpext");
            httpext = true;
        } else {
            http = opt.getStringValue("-httphost", ProtocolDefaults.HOST());
            http_port =
                opt.getIntValue("-httpport", ProtocolDefaults.HTTP_PORT);
        }

        http = normalizeURLString(http);

        String hint_host =
            opt.getStringValue("-hinthost", ProtocolDefaults.HOST());
        int hint_port =
            opt.getIntValue("-hintport", ProtocolDefaults.HINT_PORT);

        PartyInfoFactory pif = pi.getFactory();

        for (int j = 1; j <= k; j++) {
            PartyInfo p = pif.newInstance();
            p.addValue(PartyInfo.SORT_BY_ROLE, "DefaultRole");
            p.addValue(PrivateInfo.NAME, "Party" + j);
            p.addValue(PartyInfo.DESCRIPTION, "Description" + j);

            String pkeyString =
                Marshalizer.marshalToHexHuman(signatureKeyPairs[j].getPKey(),
                                               true);
            p.addValue(PartyInfo.PUB_KEY, pkeyString);

            if (httpext) {
                p.addValue(Protocol.HTTP, http + "/Party" + j);
            } else {
                p.addValue(Protocol.HTTP,
                           http + ":" + (http_port + j - 1));
            }

            p.addValue(Protocol.HINT, hint_host + ":" + (hint_port + j - 1));

            pi.addPartyInfo(p);
        }
    }

    /**
     * Adds some default values to the given <code>PrivateInfo</code>
     * instance.
     *
     * @param pi Where the values are added.
     * @param j Index of party.
     * @param opt Parsed command line options.
     */
    public void addDefaultValues(PrivateInfo pi, int j, Opt opt) {
        pi.addValue(PrivateInfo.VERSION, "demoversion");
        pi.addValue(PrivateInfo.NAME, "Party" + j);

        // HTTP server directory used in demonstration.
        File partyDir = new File(demoDir, "Party" + j);
        File dir = new File(partyDir, WORKING_DIRECTORY_NAME);

        pi.addValue(PrivateInfo.DIR, dir.toString());

        String keyPairString =
            Marshalizer.marshalToHexHuman(signatureKeyPairs[j], true);
        pi.addValue(PrivateInfo.PRIV_KEY, keyPairString);

        RandomSource rs = new PRGHeuristic();
        String rsString = Marshalizer.marshalToHexHuman(rs, true);
        if (opt.valueIsGiven("-rs")) {
            try {
                String gtString = opt.getStringValue("-rs");
                GeneratorTemplate gt =
                    Marshalizer.unmarshalHex_GeneratorTemplate(gtString);
                rsString = gt.execute();
            } catch (EIOException eioe) {
                System.out.println(opt.usage());
                System.exit(0);
            } catch (GenException ge) {
                System.out.println(opt.usage());
                System.exit(0);
            }
        }
        pi.addValue(PrivateInfo.RANDOMNESS, rsString);

        int certainty = opt.getIntValue("-cert", ProtocolDefaults.CERTAINTY);
        pi.addValue(PrivateInfo.CERTAINTY, certainty);

        File http_dir = new File(partyDir, "http");
        if (opt.valueIsGiven("-httpdir")) {
            http_dir = new File(opt.getStringValue("-httpdir"));
        }
        http_dir.mkdir();
        pi.addValue(Protocol.HTTP_DIR, http_dir.toString());

        String http_type = "internal";
        if (opt.valueIsGiven("-httpext")) {
            http_type = "external";
        }
        pi.addValue(Protocol.HTTP_TYPE, http_type);

        String httpl =
            opt.getStringValue("-httphostl", ProtocolDefaults.HOST());
        int http_port_l =
            opt.getIntValue("-httpportl", ProtocolDefaults.HTTP_PORT);
        httpl = normalizeURLString(httpl);
        pi.addValue(Protocol.HTTPL, httpl + ":" + (http_port_l + j - 1));

        String hint_host_l =
            opt.getStringValue("-hinthostl", ProtocolDefaults.HOST());
        int hint_port_l =
            opt.getIntValue("-hintportl", ProtocolDefaults.HINT_PORT);
        pi.addValue(Protocol.HINTL,
                    hint_host_l + ":" + (hint_port_l + j - 1));

    }

    /**
     * Generates a basic command line parser instance.
     *
     * @param factory Protocol factory for demonstration.
     * @return Command line parser.
     */
    public Opt defaultOpt(DemoProtocolFactory factory) {

        String commandName = factory.getClass().getName();
        int index = commandName.lastIndexOf(".");
        commandName = commandName.substring(index + 1);

        Opt opt = new Opt(commandName, "My error");

        opt.addParameter("demodir", "Root directory for demonstrator.");

        opt.addOption("-nopart", "value", "Number of parties.");
        opt.addOption("-thres", "value", "Threshold number of parties.");

        opt.addOption("-httphost", "hostname", "Host address of http servers.");
        opt.addOption("-httpport", "port",
                      "Offset port number of http servers.");
        opt.addOption("-httphostl", "hostname",
                      "Listening host address of http servers.");
        opt.addOption("-httpportl", "port",
                      "Listening offset port number of http servers.");
        opt.addOption("-httpext", "url", "Use external http server.");

        opt.addOption("-httpdir", "dir",
                     "Prefix of directories of http servers.");

        opt.addOption("-hinthost", "hostname",
                      "Hostname of hint server (defaults to \"localhost\").");
        opt.addOption("-hintport", "port",
                      "Offset port number of hint servers.");
        opt.addOption("-hinthostl", "hostname",
             "Listening hostname of hint server (defaults to \"localhost\").");
        opt.addOption("-hintportl", "port",
                      "Listening offset port number of hint servers.");
        opt.addOption("-siggen", "siggen", "Signature key pair generator.");

        opt.addOption("-seed", "", "Generate random seed for each party " +
                      "(only works when -rs is not a random device).");

        opt.addOption("-g", "", "Generate configuration files.");
        opt.addOption("-e", "", "Run demonstrator.");
        opt.addOption("-hide", "", "Hide the demonstration window.");
        opt.addOption("-close", "",
                     "Close the demonstration window when parties exit.");
        opt.addOption("-rs", "rand tmpl", "Randomness template.");

        opt.addUsageForm();
        opt.appendToUsageForm(0,
                              "-g#-hinthost,-hintport,-hinthostl,-hintportl," +
                              "-httphost,-httpport,-httpext,-httpdir," +
                              "-httphostl,-httpportl,-seed,-siggen," +
                              "-nopart,-thres,-hide,-close,-e,-rs#demodir#");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "-e#-hide,-close#demodir#");

        String s =
          "Generates configuration files, or executes a demonstration based "
          + "on given execution files. It is also possible to perform both "
          + "steps in a single call."
          + "\n\n"
          + "In an execution call, the demo directory must contain a directory "
          + "for each party named \"Party*\", i.e., any directory with a name "
          + "beginning with \"Party\" is assumed to represent a party in the "
          + "protocol. The directory must contain the files: protocolInfo.xml "
          + "and privateInfo.xml. The directory must also contain any "
          + "subdirectories identified in the two info-files, depending on the "
          + "protocol demonstrated."
          + "\n\n"
          + "All other files are ignored. The user may look at the directory "
          + "generated by the second type of call for examples, but the "
          + "directory can just as well be written by hand. There must be at "
          + "least three parties, but additional constraints may apply, in "
          + "which case this is explained below.";

        opt.appendDescription(s);

        return opt;
    }

    /**
     * Creates a demonstrator of a protocol.
     *
     * @param args Command line arguments.
     * @param factory Protocol factory.
     */
    public Demo(String[] args, DemoProtocolFactory factory) {

        this.factory = factory;

        Opt opt = null;

        boolean errorOccurred = false;
        try {

            opt = factory.generateOpt(this);

            opt.parse(args);

            if (opt.getBooleanValue("-httpext")) {
                if (opt.valueIsGiven("-httphost")
                    || opt.valueIsGiven("-httpport")) {
                    System.out.println("\nERROR: You may not use both " +
                                   "-httpext and (-httphost or -httpport).\n");
                    System.out.println(opt.usage());
                    System.exit(0);
                }
            }

            // Demo directory that holds each party's directory.
            demoDir = new File(opt.getStringValue("demodir"));


            File tmpDir = new File(demoDir, "tmp");
            TempFile.init(tmpDir);

            // Generate configuration files.
            if (opt.getBooleanValue("-g")) {

                if (demoDir.exists()) {
                    System.exit(0);
                }
                demoDir.mkdir();
                tmpDir.mkdirs();


                RandomDevice randomSource = new RandomDevice();

                SignatureKeyGen signatureKeyGen;
                if (opt.valueIsGiven("-siggen")) {
                    // TODO(signature key generator has not been implemented)
                    signatureKeyGen = null;
                } else {
                    signatureKeyGen =
                       new SignatureKeyGenHeuristic(ProtocolDefaults.SEC_PARAM);
                }

                // Number of parties.
                int k = opt.getIntValue("-k", 3);

                signatureKeyPairs = new SignatureKeyPair[k + 1];
                for (int j = 1; j <= k; j++) {
                    signatureKeyPairs[j] = signatureKeyGen.gen(randomSource);
                }

                for (int j = 1; j <= k; j++) {

                    // Create a directory for the jth party.
                    File partyDir = new File(demoDir, "Party" + j);
                    partyDir.mkdir();

                    // Create a working for the jth party.
                    File workingDir =
                        new File(partyDir, WORKING_DIRECTORY_NAME);
                    workingDir.mkdir();

                    // Write seed for PRG.
                    try {
                        File seedFile =
                            new File(workingDir, Protocol.SEED_FILENAME);
                        if (opt.getBooleanValue("-seed")) {
                            byte[] seed =
                                randomSource.getBytes(DEFAULT_SEED_LENGTH);
                            String seedString = Hex.toHexString(seed);
                            ExtIO.writeString(seedFile, seedString);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < 2 * DEFAULT_SEED_LENGTH; i++) {
                                sb.append(Hex.toHex(i % 16));
                            }
                            ExtIO.writeString(seedFile, sb.toString());
                        }
                    } catch (IOException ioe) {
                        throw new ProtocolError("Unable to create seed!", ioe);
                    }

                    // Generate the protocol info file.
                    File protocolInfoFile =
                        new File(partyDir, "protocolInfo.xml");
                    factory.generateProtocolInfoFile(this,
                                                     partyDir,
                                                     protocolInfoFile,
                                                     opt);

                    // Generate the private info file.
                    File privateInfoFile =
                        new File(partyDir, "privateInfo.xml");
                    factory.generatePrivateInfoFile(this,
                                                    partyDir,
                                                    privateInfoFile,
                                                    j,
                                                    opt);
                }
            }

            // Execute demonstrator.
            if (opt.getBooleanValue("-e")) {

                String[] partyDirNames =
                    demoDir.list(new PartyFilenameFilter());
                Arrays.sort(partyDirNames);

                if (partyDirNames == null) {
                    System.out.println("There are no party subdirectories!\n");
                    System.out.println(opt.usage());
                    System.exit(0);
                }

                int k = partyDirNames.length;

                if (k < 2) {
                    System.out.println("There are less than two parties!");
                    System.out.println(opt.usage());
                    System.exit(0);
                }

                File[] partyDirs = new File[k + 1];

                for (int j = 1; j <= k; j++) {
                    partyDirs[j] = new File(demoDir, partyDirNames[j - 1]);
                    if (!partyDirs[j].isDirectory()) {
                        System.out.println(partyDirs[j].toString()
                                           + " is not a directory!");
                        System.out.println(opt.usage());
                        System.exit(0);
                    }
                }

                // Instantiate the demo interfaces of all parties
                DemoJFrame demoJFrame =
                    new DemoJFrame(k, factory.getClass().getName());

                Runnable[] servers = new Runnable[k + 1];

                try {
                    for (int j = 1; j <= k; j++) {
                        File privateInfoFile =
                            new File(partyDirs[j], "privateInfo.xml");
                        File protocolInfoFile =
                            new File(partyDirs[j], "protocolInfo.xml");
                        servers[j] =
                            factory.newProtocol(privateInfoFile.toString(),
                                                protocolInfoFile.toString(),
                                                demoJFrame.uiAt(j));
                    }
                } catch (ProtocolError pe) {
                    System.err.println("Unable to perform demonstration!");
                    pe.printStackTrace(System.err);
                    System.exit(0);
                }

                if (!opt.getBooleanValue("-hide")) {
                    demoJFrame.setVisible(true);
                }

                // Execute the parties
                Thread[] threads = new Thread[k + 1];
                for (int j = 1; j <= k; j++) {
                    threads[j] = new Thread(servers[j]);
                    threads[j].start();
                }
                for (int j = 1; j <= k; j++) {
                    try {
                        threads[j].join();
                    } catch (InterruptedException ie) {
                        System.err.println("Unable to perform demonstration!");
                        ie.printStackTrace(System.err);
                        System.exit(0);
                    }
                }
                if (opt.getBooleanValue("-close")) {
                    System.exit(0);
                }
            }
        } catch (OptException oe) {
            System.out.println("\nERROR: " + oe.getMessage() + "\n");
            errorOccurred = true;
        } catch (OptError oe) {
            System.out.println("\nERROR: " + oe.getMessage() + "\n");
            errorOccurred = true;
        }
        if (errorOccurred) {
            System.exit(0);
        }
    }
}

class PartyFilenameFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return name.indexOf("Party") == 0;
    }
}
