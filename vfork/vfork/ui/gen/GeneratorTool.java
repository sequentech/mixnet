
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

package vfork.ui.gen;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import javax.naming.*;

import vfork.*;
import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.ui.*;
import vfork.ui.opt.*;

/**
 * Uniform command line interface to all classes in the packages
 * <code>vfork.arithm</code> and <code>vfork.crypto</code>
 * that implements the interface {@link Generator} and follows the
 * convention of naming the generator
 * <code>&lt;classname&gt;Gen</code>, e.g., the generator of {@link
 * vfork.crypto.PRGElGamal} is {@link
 * vfork.crypto.PRGElGamalGen}.
 *
 * @author Douglas Wikstrom
 */
public class GeneratorTool {

    /**
     * List of packages where the tool searches for qualified names.
     */
    final static ArrayList<String> packages = new ArrayList<String>();

    /**
     * Class names that identify the packages where the tool searches
     * for qualified names.
     */
    final static ArrayList<String> clsnames = new ArrayList<String>();

    // We list one class from each package to be included.
    static {
        clsnames.add("vfork.crypto.PRG");
        clsnames.add("vfork.arithm.LargeInteger");
    }

    /**
     * Creates a default option instance containing the options -h and
     * -v for help and verbose outputs respectively.
     *
     * @param commandName Command name used when printing usage
     * information, i.e., when a java invokation is wrapped in a shell
     * script, this would typically be the name of the shell script.
     * @param ways Number of usage forms.
     * @return Default option instance.
     */
    public static Opt defaultOpt(String commandName, int ways) {

        String defaultErrorString =
            "Invalid usage form, please use \"" + commandName +
            " -h\" for usage information!";

        Opt opt = new Opt(commandName, defaultErrorString);
        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-v", "", "Verbose human readable description.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        for (int i = 1; i <= ways; i++) {
            opt.addUsageForm();
            opt.appendToUsageForm(i, "#-v##");
        }

        return opt;
    }

    /**
     * Performs default processing of the command line arguments.
     *
     * @param opt Option instance to use for parsing.
     * @param args Command line arguments.
     * @return Returns usage information if the <code>-h</code> option
     * is used and otherwise it returns <code>null</code>.
     * @throws GenException If the default processing fails.
     */
    public static String defaultProcess(Opt opt, String[] args)
    throws GenException {
        try {
            opt.parse(args);
        } catch (OptException oe) {
            throw new GenException(oe.getMessage());
        }
        if (opt.getBooleanValue("-h")) {
            return opt.usage();
        }
        return null;
    }

    /**
     * Formats the description of the class with the given name.
     *
     * @param f Formatter used.
     * @param fullName Full name of a class implementing {@link
     * Generator}.
     */
    @SuppressWarnings("unchecked")
    protected static void formatGeneratorDescription(Formatter f,
                                                     String fullName) {
        try {
            Class klass = Class.forName(fullName);
            String description =
                ((Generator)klass.newInstance()).briefDescription();

            String broken = Util.breakLines(description, 75);
            String[] lines = Util.split(broken, "\n");
            f.format("* %s\n  %s",
                     fullName.substring(0, fullName.length() - 3), lines[0]);
            for (int i = 1; i < lines.length; i++) {
                f.format("\n  %s", lines[i]);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new GenError("Class can not be found!", cnfe);
        } catch (InstantiationException ie) {
            throw new GenError("No default constructor!", ie);
        } catch (IllegalAccessException iae) {
            throw new GenError("Can not access class!", iae);
        }
    }

    /**
     * Creates the option instance of this tool.
     *
     * @param commandName Command name used when printing usage
     * information, i.e., when a java invokation is wrapped in a shell
     * script, this would typically be the name of the shell script.
     * @return Option instance of this tool.
     */
    protected static Opt opt(String commandName) {
        Formatter f;


        String defaultErrorString =
            "Invalid usage form, please use \"" + commandName +
            " -h\" for usage information!";
        Opt opt = new Opt(commandName, defaultErrorString);

        opt.addParameter("classname",
                         "Name of class that allows generation.");
        opt.addParameter("parameters",
                         "Parameters of generator of class named <classname>.");
        opt.addParameter("shellcmd", "Shell command turned into template.");

        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-version", "", "Print package version.");
        opt.addOption("-list", "",
               "List subclasses of class <classname> with descriptions.");
        opt.addOption("-pkgs", "names",
                      "Packages searched. Given as a colon-separated list " +
                      "of full class names; one class/interface contained in " +
                      "each package to be searched.");
        opt.addOption("-gen", "", "Invoke generator of class <classname>.");
        opt.addOption("-tem", "", "Make a template for the given parameters, " +
                      "i.e., a shell command (only for debugging).");
        opt.addOption("-rndinit", "",
                      "Initialize the random source used by this command.");
        opt.addOption("-seed", "file",
                      "File containing truly random bits (master seed).");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "-list#-pkgs##classname");

        opt.addUsageForm();
        opt.appendToUsageForm(2, "-gen#-pkgs#classname#parameters");

        opt.addUsageForm();
        opt.appendToUsageForm(3, "-tem##shellcmd#");

        opt.addUsageForm();
        opt.appendToUsageForm(4, "-rndinit#-seed#classname#parameters");

        opt.addUsageForm();
        opt.appendToUsageForm(5, "-version###");

        String s =
"This command provides a uniform interface to all objects that can be " +
"generated and used in initialization files of protocols or as inputs " +
"to other calls to this tool, e.g., cryptographic keys, collision-free " +
"hashfunctions, etc." +
"\n\n" +
"The two most important options are: \"-list\" which lists, for a given " +
"class, all its sub-classes/interfaces, and \"-gen\" which invokes the " +
"generator " +
"of the given class. For such a class the option \"-h\" should give a " +
"usage description. For example, the following describes the possible " +
"ways of generating groups of squares modulo a safe prime." +
"\n\n" +
"   " + commandName + " -gen ModPGroup -h" +
"\n\n" +
"Some classes requires an instance of another class as input. Using " +
"shell-quoting it is possible to write any such invokation as a " +
"single shell command. In Bash you can quote with \"$(\" and \")\" " +
"and generate a instance of Pedersen's collision-free hashfunction as " +
"follows." +
"\n\n" +
"   " + commandName + " -gen HashfunctionPedersen -width 2 \\\n" +
"                           \"$(" + commandName +
            " -gen ModPGroup -fixed 2048)\"" +
"\n\n" +
"The \"-rndinit\" option can only be used once. It initializes the source " +
"of randomness used by this tool in all future invokations. If this option " +
"has not been used at all, then the calls that needs a random source " +
"complains, but all other calls complete without errors. The following, " +
"which uses the Un*x standard /dev/urandom as a source of bits, is usually a " +
"reasonable default, but please make sure that this is the case on your " +
"platform before you use this." +
"\n\n" +
"   " + commandName + " -rndinit RandomDevice /dev/urandom" +
"\n\n" +
"Some usage examples:" +
"\n" +
"   " + commandName +
" -list PRG                        # Sub-classes/interfaces of PRG.\n" +
"   " + commandName +
" -gen PRGHeuristic                # SHA-2 with counter.\n" +
"   " + commandName +
" -gen ModPGroup -fixed 1024       # Squares modulo safe prime.";

        opt.appendDescription(s);
        return opt;
    }

    /**
     * Verifies that a random source is available and throws an
     * exception otherwise.
     *
     * @param randomSource Random source that is verified.
     * @throws GenException If the random source is null.
     */
    public static void verify(RandomSource randomSource) throws GenException {
        String s =
            "This call requires that a random source is available! "
            + "Please use the \"-h\" option for instructions how "
            + "to do this.";
        if (randomSource == null) {
            throw new GenException(s);
        }
    }

    /**
     * Searches for the class given only an unqualified name.
     *
     * @param packages Packages to search for the given class.
     * @param name Name of class.
     * @return Class corresponding to the input name.
     * @throws GenException If no class with the given name is found.
     */
    protected static Class getClass(ArrayList<String> packages, String name)
    throws GenException {
        if (name.contains(".")) {
            for (String packageName : packages) {
                if (name.startsWith(packageName)) {
                    try {
                        Class klass = Class.forName(name);
                        return klass;
                    } catch (ClassNotFoundException cnfe) {
                        break;
                    }
                }
            }
        } else {
            for (String packageName : packages) {
                try {
                    String prefix = "";
                    if (!packageName.equals("")) {
                        prefix = packageName + ".";
                    }
                    Class klass = Class.forName(prefix + name);
                    return klass;
                } catch (ClassNotFoundException cnfe) {
                    continue;
                }
            }
        }
        throw new GenException("Error! Can not find class implementing "
                               + "the interface "
                               + "vfork.ui.gen.Generator which is "
                               + "named " + name + "!");
    }

    /**
     * Returns an initialized random source as defined by parameters.
     *
     * @param rsFile File containing a string that can be input to
     * {@link Marshalizer#unmarshalHex_RandomSource(String)}.
     * @param seedFile If the random source is a {@link PRG}, then it
     * must contain a sufficiently long seed.
     * @param tmpSeedFile Temporary seed file used to implement atomic
     * write of a new seed.
     * @return Source of random bits.
     * @throws GenException If it is not possible to create a random
     * source from the data on the given files.
     */
    public static RandomSource standardRandomSource(File rsFile,
                                                    File seedFile,
                                                    File tmpSeedFile)
    throws GenException {
        try {
            String rsString = ExtIO.readString(rsFile);
            RandomSource randomSource =
                Marshalizer.unmarshalHex_RandomSource(rsString);

            // If the random source is a PRG, then there must
            // exist an associated seed file of sufficient length.
            if (randomSource instanceof PRG) {
                try {
                    ((PRG)randomSource).
                        setSeedReplaceStored(seedFile, tmpSeedFile);
                } catch (IOException ioe) {
                    String e = "Unable to read/write PRG seed file! " +
                        "Make sure that " + seedFile + " is readable," +
                        " writeable, and is of sufficient length.";
                    throw new GenException(e, ioe);
                }
            }
            return randomSource;
        } catch (IOException ioe) {
            throw new GenException("Unable to read random source file!", ioe);
        } catch (EIOException eioe) {
            String e = "Unable to create random source! " +
                "Make sure that " + rsFile + " is valid!";
            throw new GenException(e, eioe);
        }
    }

    /**
     * Constructs a list of the resources where the input packages are
     * found.
     *
     * @param packages Packages to be located.
     * @return List of resources.
     * @throws GenException If a resource can not be found.
     */
    public static ArrayList<String> getResources(ArrayList<String> packages)
    throws GenException {
        ArrayList<String> resources = new ArrayList<String>();

        for (int i = 0; i < packages.size(); i++) {

            String path = "/" + clsnames.get(i).replace(".", "/") + ".class";

            URL packageURL = GeneratorTool.class.getResource(path);

            if (packageURL != null) {

                File dir = new File(packageURL.getFile());

                String dirString = dir.getParent();

                if (dirString.startsWith("jar:")) {
                    dirString = dirString.substring(4, dirString.length());
                }
                if (dirString.startsWith("file:")) {
                    dirString = dirString.substring(5, dirString.length());
                }

                int index = dirString.indexOf(".jar!");
                if (index > 0) {
                    dirString = dirString.substring(0, index + 4);
                } else {
                    if (dirString.endsWith("/")) {
                        dirString =
                            dirString.substring(0, dirString.length() - 1);
                    }
                }
                resources.add(dirString);

            } else {

                throw new GenException("Can not find class! (" + path + ")");

            }
        }

        return resources;
    }

    /**
     * Tests if the named class is a subclass of the given super class
     * and also that the corresponding named generator class is a
     * subclass of {@link Generator}.
     *
     * @param superKlass Superclass.
     * @param genClassName Candidate generator class name.
     * @param className Class to be tested.
     * @return Boolean indicating if the test succeeded or not.
     */
    public static boolean isSubclassAndGenerator(Class<?> superKlass,
                                                 String genClassName,
                                                 String className) {
        try {

            Class<?> genClass = Class.forName(genClassName);
            Class<?> klass = Class.forName(className);

            return Generator.class.isAssignableFrom(genClass)
                              && superKlass.isAssignableFrom(klass);

        } catch (ClassNotFoundException cnfex) {
            return false;
        }
    }

    /**
     * Extracts the generator names corresponding to subclasses of the
     * superclass from the given jar.
     *
     * @param generators Destination of extracted generator names.
     * @param superKlass Superclass.
     * @param jarFileName Name of jar file.
     * @param packageName Name of package to be searched.
     */
    public static void getGeneratorsFromJar(ArrayList<String> generators,
                                            Class superKlass,
                                            String jarFileName,
                                            String packageName) {
        try {
            File jarFile = new File(jarFileName);
            FileInputStream is = new FileInputStream(jarFile);
            JarInputStream jis = new JarInputStream(is);

            String slashedPackageName = packageName.replace(".", "/");

            JarEntry je;
            while ((je = jis.getNextJarEntry()) != null) {
                String entryName = je.getName();
                if (entryName.startsWith(slashedPackageName)
                    && entryName.endsWith("Gen.class")) {

                    String genClassName =
                        entryName.substring(0, entryName.length() - 6)
                        .replace("/", ".");

                    String className =
                        genClassName.substring(0, genClassName.length() - 3);

                    if (isSubclassAndGenerator(superKlass,
                                               genClassName,
                                               className)) {
                        generators.add(genClassName);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new Error("Failed to read from jar!", ioe);
        }
    }

    /**
     * Extracts the generator names corresponding to subclasses of the
     * superclass from the given directory.
     *
     * @param generators Destination of extracted generator names.
     * @param superKlass Superclass.
     * @param dirPath Path to directory.
     * @param packageName Name of package to be searched.
     */
    public static void getGeneratorsFromDirectory(ArrayList<String> generators,
                                                  Class superKlass,
                                                  String dirPath,
                                                  String packageName) {
        File dir = new File(dirPath);

        String[] fileNames = dir.list();

        String prefix = "";
        if (!packageName.equals("")) {
            prefix = packageName + ".";
        }

        for (int i = 0; i < fileNames.length; i++) {

            if (fileNames[i].endsWith("Gen.class")) {

                String genClassName =
                    prefix + fileNames[i].substring(0,
                                                    fileNames[i].length() - 6);
                String className =
                    genClassName.substring(0, genClassName.length() - 3);

                if (isSubclassAndGenerator(superKlass,
                                           genClassName,
                                           className)) {

                    generators.add(genClassName);
                 }
            }
        }
    }


    /**
     * Returns an array of the names of all classes which implements
     * or subclasses the given class and also implements the {@link
     * Generator} interface. Only the listed packages are searched.
     *
     * @param superKlass Class of which subclasses are sought.
     * @param packages Packages to search.
     * @return List of names of generators.
     * @throws GenException If a resource can not be found.
     */
    public static ArrayList<String> getGenerators(Class superKlass,
                                                  ArrayList<String> packages)
    throws GenException {

        ArrayList<String> resources = getResources(packages);

        ArrayList<String> generators = new ArrayList<String>();

        for (int i = 0; i < packages.size(); i++) {

            String resource = resources.get(i);
            String packageName = packages.get(i);

            int index = resource.indexOf(".jar");

            // Resource is a jar-file.
            if (index > -1) {

                String jarFileName = resource.substring(0, index + 4);
                getGeneratorsFromJar(generators, superKlass,
                                     jarFileName, packageName);

            // Resource is a directory.
            } else {

                getGeneratorsFromDirectory(generators, superKlass,
                                           resource, packageName);

            }

        }

        return generators;
    }

    /**
     * Execute a given generator using the given random source and
     * command line arguments.
     *
     * @param randomSource Source of randomness.
     * @param commandName Command name used when printing usage
     * information, i.e., when a java invokation is wrapped in a shell
     * script, this would typically be the name of the shell script.
     * @param args Command line arguments.
     * @return Output of generator.
     * @throws GenException If the generation fails, in which case the
     * error message describes the cause.
     */
    public static String gen(RandomSource randomSource,
                             String commandName, String[] args)
        throws GenException {

        try {
            Opt opt = opt(commandName);
            int parsedArgs = opt.parse(args, 1);

            return gen(randomSource, opt, parsedArgs, args);

        } catch (OptException oe) {
            throw new GenException(oe.getMessage(), oe);
        }
    }

    /**
     * Initializes the set of packages to be searched.
     *
     * @param opt Options given by the user.
     */
    public static void initPackages(Opt opt) {
        if (opt.valueIsGiven("-pkgs")) {

            String optcnsString = opt.getStringValue("-pkgs");

            String[] optcns = Util.split(optcnsString, ":");

            for (int i = 0; i < optcns.length; i++) {
                clsnames.add(optcns[i]);
            }
        }

        for (String className : clsnames) {
            int index = className.lastIndexOf(".");
            if (index == -1) {
                packages.add("");
            } else {
                packages.add(className.substring(0, index));
            }
        }
    }

    /**
     * Execute a given generator using the given random source and
     * command line arguments.
     *
     * @param randomSource Source of randomness.
     * @param opt Options given by the user in parsed form.
     * @param parsedArgs Number of command line arguments that have
     * been parsed already.
     * @param args Command line arguments.
     * @return Output of generator.
     * @throws GenException If the generation fails, in which case the
     * error message describes the cause.
     */
    public static String gen(RandomSource randomSource, Opt opt,
                             int parsedArgs, String[] args)
        throws GenException {

        if (opt.getBooleanValue("-rndinit")) {

            throw new GenException("Attempting to reinitialize random source!");

        } if (opt.getBooleanValue("-h")) {

            return opt.usage();

        } if (opt.getBooleanValue("-version")) {

            return Version.packageVersion;

        } else if (opt.getBooleanValue("-list")) {

            if (args.length != 2) {
                throw new GenException("You must specify at exactly one " +
                                       "class name when listing implementors!");
            }

            Class klass = Object.class;
            if (opt.valueIsGiven("classname")) {
                klass = getClass(packages,
                                 opt.getStringValue("classname"));
            }

            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb);
            ArrayList<String> fullNames = getGenerators(klass, packages);
            Collections.sort(fullNames);

            f.format("\nClasses/interfaces that inherit/implement " +
                     klass.getName() + ":\n");
            for (String fullName : fullNames) {
                f.format("\n");
                formatGeneratorDescription(f, fullName);
            }
            sb.append("\n");

            return sb.toString();

        } else if (opt.getBooleanValue("-gen")) {

            Class klass = getClass(packages,
                                   opt.getStringValue("classname") + "Gen");
            try {

                Generator generator = (Generator)klass.newInstance();

                String[] cmdArgs =
                    Arrays.copyOfRange(args,
                                       Math.min(parsedArgs + 1, args.length),
                                       args.length);
                return generator.gen(randomSource, cmdArgs);

            } catch (InstantiationException ie) {
                throw new GenException(ie.getMessage(), ie);
            } catch (IllegalAccessException iae) {
                throw new GenException(iae.getMessage(), iae);
            }

        } else if (opt.getBooleanValue("-tem")) {

            GeneratorTemplate gt =
                new GeneratorTemplate(GeneratorTemplate.CMD,
                                      opt.getStringValue("shellcmd"));

            // Before constructing the template we try to use
            // it to see that the template works.
            try {
                gt.execute();
            } catch (GenException ge) {
                String s =
                    "The command to be templated is malformed. " +
                    "Please try this command on its own before " +
                    "templating it!";
                throw new GenException(s);
            }

            return Marshalizer.marshalToHexHuman(gt, true);

        } else {
            throw new GenError("Invalid option!");
        }
    }

    /**
     * Execute the generator tool as a stand-alone application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            throw new GenError("No command name specified!");
        }
        if (args.length < 4) {
            throw new GenError("Random source files are missing!");
        }
        String commandName = args[0];

        String[] additional = args[1].split(":");
        for (int i = 0; i < additional.length; i++) {
            if (!additional[i].equals("")) {
                clsnames.add(additional[i]);
            }
        }

        try {

            File rsFile = new File(args[2]);
            File tmprsFile = new File(args[2] + "_TMP");

            File seedFile = new File(args[3]);
            File tmpSeedFile = new File(args[3] + "_TMP");

            // Drop the first 4 arguments and parse the rest up to the
            // first parameter.
            args = Arrays.copyOfRange(args, 4, args.length);

            Opt opt;
            int parsedArgs;
            try {
                opt = opt(commandName);
                parsedArgs = opt.parse(args, 1);
            } catch (OptException oe) {

                throw new GenException(oe.getMessage(), oe);
            }

            RandomSource randomSource = null;


            // Initialize the packages to be searched.
            initPackages(opt);


            // If a random source file exists, then we try to use it,
            // and report any failures.
            if (rsFile.exists()) {

                randomSource =
                    standardRandomSource(rsFile, seedFile, tmpSeedFile);
                System.out.println(gen(randomSource, opt, parsedArgs, args));

            // Otherwise we either initialize the random source, or we
            // hope that the call can proceed without the random source.
            } else {

                if (opt.valueIsGiven("-rndinit")) {

                    if (seedFile.exists()) {
                        throw new GenException("Please delete the existing " +
                                               "seed file " + seedFile +
                                               "before trying again!");
                    }

                    String s = "Successfully initialized random source!";
                    try {
                        Class klass =
                            getClass(packages,
                                     opt.getStringValue("classname") + "Gen");

                        Generator generator = (Generator)klass.newInstance();
                        String[] cmdArgs =
                            Arrays.copyOfRange(args,
                                               parsedArgs + 1,
                                               args.length);

                        String randomSourceString =
                            generator.gen(randomSource, cmdArgs);


                        // Try to instantiate.
                        RandomSource rs;
                        try {
                            rs = Marshalizer.
                                unmarshalHex_RandomSource(randomSourceString);
                        } catch (EIOException eioe) {
                            String e =
                                "Failed to generate random source! Did you " +
                                "perhaps pass the \"-h\" option to the class " +
                                "generator? You are probably not using the " +
                                "\"-gen\" option in that case.";
                            throw new GenException(e, eioe);
                        }

                        // If the random source is a PRG, then we need
                        // a seed.
                        if (rs instanceof PRG) {

                            if (opt.valueIsGiven("-seed")) {

                                PRG prg = (PRG)rs;
                                int minNoSeedBytes = prg.minNoSeedBytes();

                                File srcFile =
                                    new File(opt.getStringValue("-seed"));

                                if (!srcFile.exists()) {
                                    String e = "Seed file does not exist!";
                                    throw new GenException(e);
                                }

                                BufferedReader br =
                                    new BufferedReader(new FileReader(srcFile));

                                char[] cbuf = new char[minNoSeedBytes];
                                if (br.read(cbuf, 0, cbuf.length)
                                    != cbuf.length) {
                                    String e = "Seed is too short!";
                                    throw new GenException(e);
                                }

                                if (srcFile.isFile()) {
                                    if (srcFile.delete()) {
                                        s = s + " Deleted seed file.";
                                    } else {
                                        s = s +
                                            "Unable to delete seed file!";
                                        throw new GenException(s);
                                    }
                                }

                                byte[] bytes = new byte[cbuf.length];
                                for (int i = 0; i < bytes.length; i++) {
                                    bytes[i] = (byte)cbuf[i];
                                }

                                ExtIO.atomicWriteString(tmpSeedFile,
                                                        seedFile,
                                                        Hex.toHexString(bytes));

                            } else {
                                String e =
                                    "Missing seed file! " +
                                    "To use a PRG as a random source you " +
                                    "must use the \"-seed\" option and " +
                                    "provide a seed.";
                                throw new GenException(e);
                            }
                        }
                        ExtIO.atomicWriteString(tmprsFile,
                                                rsFile,
                                                randomSourceString);
                    } catch (IOException ioe) {
                        String e = "Unable to use random source files!";
                        throw new GenException(e, ioe);
                    } catch (InstantiationException ie) {
                        throw new GenException(ie.getMessage(), ie);
                    } catch (IllegalAccessException iae) {
                        throw new GenException(iae.getMessage(), iae);
                    }
                    System.out.println(s);

                } else {

                    String res = gen(randomSource, opt, parsedArgs, args);
                    System.out.println(res);
                }
            }

        } catch (GenException ge) {

            String e = "\n" + "ERROR: " + ge.getMessage() + "\n";
            System.err.println(e);
        }
    }
}
