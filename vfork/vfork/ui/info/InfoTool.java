
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

package vfork.ui.info;

import java.io.*;
import java.util.*;

import vfork.*;
import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.protocol.*;
import vfork.ui.*;
import vfork.ui.gen.*;
import vfork.ui.opt.*;

/**
 * Command line tool for generating info files.
 *
 * @author Douglas Wikstrom
 */
public class InfoTool {

    /**
     * Extracts a comma separated string containing the names of all
     * options.
     *
     * @param defaultInfo Info instance containing some default
     * values.
     * @param required Decides if required or optional option values
     * are generated.
     * @return String representation of extracted options.
     */
    protected static String extractOptions(Info defaultInfo,
                                           boolean required) {
        StringBuilder sb = new StringBuilder();
        for (InfoField inf : defaultInfo.infoFields) {
            String name = inf.getName();

            if (!name.equals(RootInfo.VERSION)) {

                if (defaultInfo.hasValue(inf.getName()) != required) {
                    sb.append(",-" + name);
                }
            }
        }
        String s = sb.toString();
        if (s.startsWith(",")) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * Generates an option field in the given option instance for each
     * value in the given info.
     *
     * @param opt Destination of options.
     * @param info Source of options.
     */
    protected static void addOptions(Opt opt, Info info) {
        for (InfoField inf : info.infoFields) {

            if (!inf.getName().equals(RootInfo.VERSION)) {

                String optionName = "-" + inf.getName();
                if (!opt.hasOption(optionName)) {
                    opt.addOption(optionName,
                                  "value",
                                  inf.getDescription());
                }
            }
        }
    }

    /**
     * Generates an option instance from the given info instances. If
     * default values are given, then the options are optional and
     * otherwise they are required.
     *
     * @param commandName Name of command to be used when generating
     * usage information.
     * @param pi Empty protocol info with the needed fields.
     * @param dpi Protocol info containing some default values.
     * @param pri Empty private info with the needed fields.
     * @param dpri Private info containing some default values.
     * @param pai Empty party info with the needed fields.
     * @param dpai Party info containing some default values.
     * @return Instance representing the given command line.
     */
    protected static Opt opt(String commandName,
                             ProtocolInfo pi,
                             ProtocolInfo dpi,
                             PrivateInfo pri,
                             PrivateInfo dpri,
                             PartyInfo pai,
                             PartyInfo dpai) {

        String defaultErrorString =
            "Invalid usage form, please use \"" + commandName +
            " -h\" for usage information!";

        Opt opt = new Opt(commandName, defaultErrorString);

        opt.addParameter("inprotfile",
                         "Protocol info file containing joint parameters " +
                         "and possibly some party info entries.");
        opt.addParameter("outprivfile", "Private info output file.");
        opt.addParameter("outprotfile", "Protocol info output file.");

        opt.addParameter("file", "Info file.");

        opt.addOption("-h", "", "Display usage information");
        opt.addOption("-version", "", "Print the package version and list " +
                      "protocol versions it can handle.");
        opt.addOption("-prot", "",
                      "Generate basic protocol info file containing only " +
                      "joint parameters.");
        opt.addOption("-party", "",
                      "Generate private and protocol info files based on " +
                      "the given protocol info file.");
        opt.addOption("-seed", "value",
                      "Seed file for pseudo-random generator of this party.");
        opt.addOption("-digest", "",
                      "Compute hexadecimal encoded digest of file.");
        opt.addOption("-merge", "",
                      "Merge several protocol info files with identical " +
                      "joint parameters into a single protocol info file.");
        opt.addOption("-hash", "value",
                      "Name of an algorithm from the SHA-2 family, i.e., " +
                      "SHA-256, SHA-384, or SHA-512. (Default is SHA-256.)");
        opt.addOption("-schema", "type",
                      "Output the XML schema definition of private or " +
                      "protocol files. The type must be one of " +
                      "\"private\" and \"protocol\".");


        addOptions(opt, pi);
        addOptions(opt, pri);
        addOptions(opt, pai);

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "-prot," + extractOptions(dpi, true) +
                              "#" + extractOptions(dpi, false) +
                              "#outprotfile#");

        opt.addUsageForm();
        opt.appendToUsageForm(2, "-party," + extractOptions(dpri, true) +
                              "," + extractOptions(dpai, true) +
                              "#-seed," + extractOptions(dpri, false) +
                              "," + extractOptions(dpai, false) +
                              "#inprotfile,outprivfile,outprotfile#");

        opt.addUsageForm();
        opt.appendToUsageForm(3, "-merge##+inprotfile,outprotfile#");

        opt.addUsageForm();
        opt.appendToUsageForm(4, "-digest#-hash#file#");

        opt.addUsageForm();
        opt.appendToUsageForm(5, "-schema###");

        opt.addUsageForm();
        opt.appendToUsageForm(6, "-version###");

        String descr =
"This command is used to generate the configuration file of a protocol " +
"in three simple steps:" +
"\n\n" +
"   (1) One party generates a stub file with the global parameters.\n" +
"   (2) Each party generates private and public parameters.\n" +
"   (3) Each party merges all public parameters into a single public file." +
"\n\n" +
"The options \"-prot\", \"-party\", and \"-merge\" must appear as the first " +
"option. Use \"-prot\" to generate the global stub file " +
"containing only the global parameters that all parties agree on. For " +
"example:" +
"\n\n" +
"   " + commandName + " -prot -sid \"SID\" " +
"-name \"Execution\" -nopart 3 -thres 2 stub.xml" +
"\n\n" +
"Use \"-party\" to generate: (i) a private info file containing " +
"your private parameters, e.g., your secret signing key, and (ii) a new " +
"protocol info file based on the input protocol info file, where all your " +
"public information has been added. For example:" +
"\n\n" +
"   " + commandName + " -party -name \"Santa Claus\" " +
"stub.xml privInfo1.xml protInfo1.xml" +
"\n\n" +
"(If you use a PRG as the " +
"\"-rand\" (random source), then use the \"-seed\" option as " +
"well and specify a seed file (or device) containing a seed of suitable " +
"length.)" +
"\n\n" +
"Use \"-merge\" to generate a single protocol info file from " +
"several protocol info files with identical joint parameters. Assuming that " +
"the ith party names its info files as above:" +
"\n\n" +
"   " + commandName + " -merge protInfo1.xml protInfo2.xml protInfo3.xml protInfo.xml" +
"\n\n" +
"All optional values have reasonable defaults, i.e., you can actually use " +
"the above commands provided that /dev/urandom contains good randomness. " +
"Please generate dummy files " +
"to investigate exactly what these defaults are. It is unwise to touch " +
"the defaults unless you know exactly what you are doing." +
"\n\n" +
"The stub file name can be dropped when the \"-prot\" option is used, " +
"in which case it defaults to \"stub.xml\". Similarly, the filenames " +
"can be dropped when using the \"-party\" option, in which case they " +
"default to \"stub.xml\", \"privInfo.xml\", and \"localProtInfo.xml\". " +
"The name of the joint protocol info file can also be dropped when using the " +
"\"-merge\" option, in which case it defaults to \"protInfo.xml\"";

        opt.appendDescription(descr);

        return opt;
    }

    /**
     * Generate a simple option instance to be used to generate usage
     * information.
     *
     * @param commandName Name of command to be used when generating
     * usage information.
     * @return Instance representing the given command line.
     */
    protected static Opt simpleOpt(String commandName) {

        Opt opt = new Opt(commandName, "hejhopp");

        opt.addParameter("commandName",
                         "Name of shell script wrapper.");
        opt.addParameter("rsFile",
                         "File containing a representation of a random " +
                         "source.");
        opt.addParameter("seedFile",
                         "File containing a seed for the random source, " +
                         "if it is PRG (otherwise this input is ignored).");
        opt.addParameter("classname",
                         "Name of protocol class for which to generate " +
                         "info files.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "##commandName,rsFile,seedFile,classname#");


        String s =
"Generate info files for a given class which implements the " +
"ui.info.InfoGenerator interface. If you give the classname as a " +
"parameter you get class specific usage information. You should normally " +
"only invoke this command in this way if you are a programmer. A wrapper " +
"script should be provided to users." +
"\n\n" +
"The option -Djava.security.egd=file:/dev/./urandom is needed due to a bug " +
"in JDK 6. Please have a look at the documentation of " +
"vfork.crypto.PRGHeuristic for more information." +
"\n\n" +
"If you are a normal user, then this is almost certainly a bug. Please " +
"report it!";
        opt.appendDescription(s);

        return opt;
    }

    /**
     * Transfers values to the given info instance either from the
     * option instance as given by the user or from the default info
     * instance when no user given values are available.
     *
     * @param info Destination of values.
     * @param opt Source of values given by user.
     * @param defaultInfo Source of default values.
     */
    protected static void transferValues(Info info, Opt opt, Info defaultInfo) {

        for (InfoField inf : info.infoFields) {
            String name = inf.getName();
            String optionName = "-" + name;
            if (opt.valueIsGiven(optionName)) {
                info.addValue(name, opt.getStringValue(optionName));
            } else {
                info.copyValues(name, defaultInfo);
            }
        }
    }

    /**
     * Invokes the stand-alone command line tool.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        if (args.length < 4) {
            String cn = "java -Djava.security.egd=" +
                "file:/dev/./urandom vfork.ui.info.InfoTool";
            System.out.println(simpleOpt(cn).usage());
            System.exit(0);
        }

        String commandName = args[0];

        RandomSource randomSource = null;
        try {
            File rsFile = new File(args[1]);
            File seedFile = new File(args[2]);
            File tmpSeedFile = new File(args[2] + "_TMP");

            randomSource =
                GeneratorTool.standardRandomSource(rsFile,
                                                   seedFile,
                                                   tmpSeedFile);
        } catch (GenException ge) {
            String e = "\n" + "ERROR: " + ge.getMessage() + "\n";
            System.err.println(e);
            System.exit(0);
        }

        String className = args[3];

        args = Arrays.copyOfRange(args, 4, args.length);

        String stubInfoFileName = "stub.xml";
        String privInfoFileName = "privInfo.xml";
        String protInfoFileName = "protInfo.xml";
        String localProtInfoFileName = "localProtInfo.xml";

        try {

            Class klass = null;
            try {
                klass = Class.forName(className + "Gen");
            } catch (ClassNotFoundException cnfe) {
                String s = "There exists no associated class " + className +
                    "Gen for the class " + className + "!";
                throw new InfoException(s, cnfe);
            }

            if (!InfoGenerator.class.isAssignableFrom(klass)) {
                String s = "The class " + klass.getName() + " does not " +
                    "implement the ui.info.InfoGenerator interface!";
                throw new InfoException(s);
            }

            InfoGenerator generator = null;
            try {
                generator = (InfoGenerator)klass.newInstance();
            } catch (IllegalAccessException iae) {
                throw new InfoException("Could not instantiate the " +
                                        "InfoGenerator!", iae);
            } catch (InstantiationException ie) {
                throw new InfoException("Could not instantiate the " +
                                        "InfoGenerator!", ie);
            }

            ProtocolInfo pi = generator.newProtocolInfo();
            PrivateInfo pri = generator.newPrivateInfo();
            PartyInfo pai = generator.newPartyInfo(pi);

            ProtocolInfo dpi = generator.defaultProtocolInfo();
            PrivateInfo dpri = generator.defaultPrivateInfo(dpi, randomSource);
            PartyInfo dpai =
                generator.defaultPartyInfo(dpi, dpri, randomSource);

            Opt opt = opt(commandName, pi, dpi, pri, dpri, pai, dpai);

            try {
                opt.parse(args);
            } catch (OptException oe) {

                // If parsing fails, then we assume that the user is
                // executing the commands "in the working directory"
                // and make another attempt with the default file
                // names.

                if (args.length > 0) {

                    String[] newargs = new String[0];
                    if (args[0].equals("-prot")) {

                        newargs = new String[args.length + 1];
                        System.arraycopy(args, 0, newargs, 0, args.length);
                        newargs[args.length] = stubInfoFileName;

                    } else if (args[0].equals("-party")) {

                        newargs = new String[args.length + 3];
                        System.arraycopy(args, 0, newargs, 0, args.length);
                        newargs[args.length] = stubInfoFileName;
                        newargs[args.length + 1] = privInfoFileName;
                        newargs[args.length + 2] = localProtInfoFileName;

                    } else {

                        throw new InfoException(oe.getMessage(), oe);
                    }

                    opt = opt(commandName, pi, dpi, pri, dpri, pai, dpai);
                    try {
                        opt.parse(newargs);
                    } catch (OptException oe2) {
                        throw new InfoException(oe.getMessage(), oe);
                    }

                } else {
                    throw new InfoException(oe.getMessage(), oe);
                }
            }

            if (opt.getBooleanValue("-h")) {

                System.out.println(opt.usage());

            } else if (opt.getBooleanValue("-version")) {

                System.out.println(Version.packageVersion + " " +
                         "(" + generator.compatibleProtocolVersions() + ")");

            } else if (opt.getBooleanValue("-prot")) {

                pi.addValue(RootInfo.VERSION, generator.protocolVersion());

                transferValues(pi, opt, dpi);

                File protocolInfoFile =
                    new File(opt.getStringValue("outprotfile"));
                pi.toXML(protocolInfoFile);

            } else if (opt.getBooleanValue("-party")) {

                pi.parse(opt.getStringValue("inprotfile"));

                String piVersion = pi.getStringValue(ProtocolInfo.VERSION);
                if (!generator.compatible(piVersion)) {
                    String s = "Input stub protocol info file has " +
                        "incompatible version " + piVersion + "!";
                    throw new InfoException(s);
                }

                pri.addValue(RootInfo.VERSION, generator.protocolVersion());


                // We use dynamic defaults for the internal port
                // numbers of the HTTP and hint servers.
                if (opt.valueIsGiven("-http") && !opt.valueIsGiven("-httpl")) {
                    opt.storeOptionValue("-httpl", opt.getStringValue("-http"));
                }

                if (opt.valueIsGiven("-hint") && !opt.valueIsGiven("-hintl")) {
                    opt.storeOptionValue("-hintl", opt.getStringValue("-hint"));
                }

                transferValues(pri, opt, dpri);
                File privateInfoFile =
                    new File(opt.getStringValue("outprivfile"));
                pri.toXML(privateInfoFile);
                privateInfoFile.setWritable(false, false);

                RandomSource rand = Protocol.randomSource(pri);
                if (rand instanceof PRG) {
                    if (opt.valueIsGiven("-seed")) {

                        File seedFile = new File(opt.getStringValue("-seed"));
                        if (!seedFile.exists()) {
                            String e = "Given seed file does not exist!";
                            throw new InfoException(e);
                        }
                        if (!seedFile.canRead()) {
                            String e = "Unable to read from given seed file!";
                            throw new InfoException(e);
                        }

                        DataInputStream dis = null;
                        try {
                            dis =
                             new DataInputStream(new FileInputStream(seedFile));
                            int noSeedBytes = ((PRG)rand).minNoSeedBytes();
                            byte[] seedBytes = new byte[noSeedBytes];
                            dis.readFully(seedBytes);

                            File partySeedFile = Protocol.seedFile(pri);
                            File parentFile = partySeedFile.getParentFile();
                            if (parentFile != null) {
                                parentFile.mkdirs();
                            }
                            ExtIO.writeString(partySeedFile,
                                              Hex.toHexString(seedBytes));
                        } catch (IOException ioe) {
                            String e = "Could not create seed file!";
                            throw new InfoException(e);
                        } finally {
                            ExtIO.strictClose(dis);
                        }
                    } else {
                        throw new InfoException("A seed is required!");
                    }
                }

                transferValues(pai, opt, dpai);
                pi.addPartyInfo(pai);
                File protocolInfoFile =
                    new File(opt.getStringValue("outprotfile"));

                pi.toXML(protocolInfoFile);
                protocolInfoFile.setWritable(false, false);

            } else if (opt.getBooleanValue("-merge")) {

                File protocolInfoFile =
                    new File(opt.getStringValue("outprotfile"));

                String[] mpars = opt.getMultiParameters();

                pi.parse(mpars[0]);

                if (pi.getIntValue(ProtocolInfo.NOPARTIES) != mpars.length) {

                    if (pi.getIntValue(ProtocolInfo.NOPARTIES) ==
                        mpars.length + 1) {

                        String[] tmp = new String[mpars.length + 1];
                        System.arraycopy(mpars, 0, tmp, 0, mpars.length);
                        tmp[mpars.length] = protocolInfoFile.toString();
                        mpars = tmp;
                        protocolInfoFile = new File("protInfo.xml");

                    } else {
                        throw new InfoException("Wrong number of protocol " +
                                                "info files!");
                    }
                }

                for (int i = 1; i < mpars.length; i++) {

                    ProtocolInfo tpi = generator.newProtocolInfo();
                    tpi.parse(mpars[i]);
                    if (!pi.equalInfoFields(tpi)) {
                        String s =
                            "All input protocol info files must contain " +
                            "identical common data fields.";
                        throw new InfoException(s);
                    }
                    pi.addPartyInfos(tpi);
                }

                String piVersion = pi.getStringValue(ProtocolInfo.VERSION);
                if (!generator.compatible(piVersion)) {
                    String s = "Input protocol info files have " +
                        "incompatible version " + piVersion + "!";
                    throw new InfoException(s);
                }

                pi.toXML(protocolInfoFile);
                protocolInfoFile.setWritable(false, false);

            } else if (opt.getBooleanValue("-digest")) {

                File file = new File(opt.getStringValue("file"));
                String algorithm = "SHA-256";
                try {

                    byte[] bytes = ExtIO.readString(file).getBytes();

                    if (opt.valueIsGiven("-hash")) {
                        algorithm = opt.getStringValue("-hash");
                    }
                    Hashfunction hf = new HashfunctionHeuristic(algorithm);

                    System.out.println(Hex.toHexString(hf.hash(bytes)));

                } catch (FileNotFoundException fnfe) {
                    throw new InfoException("Can not find file! (" +
                                            file.toString() + ")");
                } catch (IOException ioe) {
                    throw new InfoException("Can not read file! (" +
                                            file.toString() + ")");
                } catch (CryptoError ce) {
                    throw new InfoException("Unknown hash function! (" +
                                            algorithm + ")", ce);
                }

            } else if (opt.getBooleanValue("-schema")) {
                System.out.println(pi.generateSchema());
            }

        } catch (InfoException ie) {

            String e = "\n" + "ERROR: " + ie.getMessage() + "\n";
            System.err.println(e);
        }
    }
}