
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

package verificatum.arithm;

import java.io.*;
import java.net.*;
import java.util.*;

import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.ui.gen.*;
import verificatum.ui.opt.*;

/**
 * Class for using a table of pre-computed safe primes of different
 * bit-sizes.
 *
 * @author Douglas Wikstrom
 */
public class SafePrimeTable {

    /**
     * Minimal size of tabulated prime.
     */
    final static int MIN_BIT_LENGTH = 257;  // Inclusive

    /**
     * Maximal size of tabulated prime.
     */
    final static int MAX_BIT_LENGTH = 4120; // Exclusive

    /**
     * Name of file containing table of safe primes. This file is
     * stored as a resource together with this class.
     */
    final static String SAFE_PRIME_TABLE_FILE_NAME = "safe_prime_table.txt";

    /**
     * Computes the position in the file where the safe prime with the
     * given number of bits is located.
     *
     * @param bitLength Number of bits in safe prime.
     * @return Index of starting character of the smallest safe prime
     * of the suitable bit length.
     */
    protected static int seekPosition(int bitLength) {

        // We could compute this using an arithmetic sum, but the
        // following is easier to read and verify and this operation
        // is infrequent.
        int total = 0;
        for (int i = MIN_BIT_LENGTH; i < bitLength; i++) {

            // Each entry has:
            // 5 characters to represent the bit-size.
            // 1 character for a separating colon.
            // 1 extra most significant zero
            //
            // The number of characters needed to represent an i-bit
            // prime is given by (i + 3)/4.
            //
            // The entry ends with a single newline-character.
            total += 5 + 1 + 1 + (i + 3)/4 + 1;
        }
        return total;
    }

    /**
     * Reads the safe prime with the given number of bits from the
     * table on file.
     *
     * @param bitLength Number of bits in safe prime.
     * @return A safe prime with the given number of bits.
     * @throws ArithmFormatException If the bit-length is not between
     * {@link #MIN_BIT_LENGTH} (inclusive) and {@link #MAX_BIT_LENGTH}
     * (exclusive).
     */
    public static LargeInteger safePrime(int bitLength)
    throws ArithmFormatException {

        if (!(MIN_BIT_LENGTH <= bitLength || bitLength < MAX_BIT_LENGTH)) {
            String s = "Invalid bit length! (safe primes are only " +
                "precomputed for primes bit-sizes greater than " +
                MIN_BIT_LENGTH + " and less than " + MAX_BIT_LENGTH + ".";
            throw new ArithmFormatException(s);
        }

        URI uri = null;

        URL resourceURL = SafePrimeTable.class
            .getResource(SAFE_PRIME_TABLE_FILE_NAME);

        LargeInteger li = null;

        InputStream is = null;
        BufferedReader br = null;
        try {
            if (resourceURL.toString().indexOf(".jar!") != -1) {
                is = SafePrimeTable.class
                    .getResourceAsStream(SAFE_PRIME_TABLE_FILE_NAME);
            } else {
                try {

                    // Precomputed primes are stored along with the package.
                    uri = resourceURL.toURI();
                    is = new FileInputStream(new File(uri));
                } catch (FileNotFoundException fnfe) {
                    throw new ArithmError("Could not open file!", fnfe);
                } catch (URISyntaxException use) {
                    throw new ArithmError("Could not create URI!", use);
                }
            }
            br = new BufferedReader(new InputStreamReader(is));

            int position = seekPosition(bitLength);
            br.skip(position);

            String line = br.readLine();

            String spString[] = line.split(":");

            li = new LargeInteger(spString[1], 16);

        } catch (IOException ioe) {
            throw new ArithmError("Can not read safe prime!", ioe);
        } finally {
            ExtIO.strictClose(br);
            ExtIO.strictClose(is);
        }

        return li;
    }

    /**
     * Creates an option instance containing the options of the
     * command line tool.
     *
     * @param commandName Name of the command to use when printing
     * usage information.
     * @return Option instance.
     */
    public static Opt opt(String commandName) {

        String defaultErrorString =
            "Invalid usage form, please use \"" + commandName +
            " -h\" for usage information!";

        Opt opt = new Opt(commandName, defaultErrorString);

        opt.addOption("-h", "", "Print usage information.");
        opt.addParameter("start", "Bitlength lower bound of generated safe " +
                         "primes, i.e., the starting point.");
        opt.addParameter("end", "Bitlength upper bound (exclusive) of " +
                         "generated safe primes, i.e., the ending point.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "##start,end#");

        String s = "Identifies safe primes in the given interval. Each " +
            "prime is derived as the smallest safe prime larger than a " +
            "given integer which in turn is is generated by hashing the " +
            "bitlength with SHA-256 along with counters to extract as many " +
            "bits as is needed. Please consult the source of " +
            "verificatum.arithm.SafePrimeTable for detailed information.";

        opt.appendDescription(s);

        return opt;
    }

    /**
     * Derives a positive integer of the given bitlength by evaluation
     * SHA-256 on the bitlength. An additional input allows varying
     * the output slightly.
     *
     * @param bitLength Bitlength of the derived integer.
     * @param variant If one needs an integer with a particular form,
     * this parameter can be varied until a suitable output is found.
     * @return Derived integer.
     */
    public static LargeInteger deriveIntegerUsingSHA_256(int bitLength,
                                                         int variant) {

        Hashfunction roHashfunction = new HashfunctionHeuristic("SHA-256");
        RandomOracle ro = new RandomOracle(roHashfunction, bitLength);
        byte[] bitLengthBytes = new byte[8];

        // Turn bitlength and variant into an array of bytes.
        for (int i = 3; i >= 0; i--) {
            bitLengthBytes[i] = (byte)(bitLength >>> i);
            bitLengthBytes[i + 4] = (byte)(variant >>> i);
        }

        // Hash derived bytes.
        byte[] liBytes = ro.hash(bitLengthBytes);

        // Make sure there is a leading one in the right position.
        int mask = 0x80;
        int r = bitLength % 8;
        if (r != 0) {
            mask = (mask >>> (8-r));
        }
        liBytes[0] |= mask;

        return LargeInteger.toPositive(liBytes);
    }

    /**
     * Command line interface used to generate primes stored in the
     * table used by this class.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Executed without wrapper providing a " +
                               "random source!? Please consult source code.");
            System.exit(1);
        }

        String commandName = args[0];
        Opt opt = opt(commandName);

        if (args.length != 5) {
            System.out.println(opt.usage());
            System.exit(1);
        }

        int certainty = 100; // This is overkill, but should work in
                             // any application, and then we don't need
                             // another parameter.

        try {

            RandomSource rs =
                GeneratorTool.standardRandomSource(new File(args[1]),
                                                   new File(args[2]),
                                                   new File(args[2] + "_TMP"));

            try {
                opt.parse(Arrays.copyOfRange(args, 3, args.length));
            } catch (OptException oe) {
                String e = "\n" + "ERROR: " + oe.getMessage() + "\n";
                System.err.println(e);
                System.exit(1);
            }

            int start = opt.getIntValue("start");
            int end = opt.getIntValue("end");

            for (int i = start; i < end; i++) {

                LargeInteger safePrime = null;
                for (int variant = 0;; variant++) {

                    LargeInteger n = deriveIntegerUsingSHA_256(i, variant);
                    safePrime = n.nextSafePrime(rs, certainty);

                    if (safePrime.bitLength() == i) {
                        break;
                    }
                }

                System.out.printf("%5d:0", i);
                System.out.println(safePrime.toString(16));
            }

            System.exit(0);

        } catch (GenException ge) {
            System.out.println(ge.getMessage());
            System.out.println(opt.usage());
        } catch (NumberFormatException nfe) {
            System.out.println("Please specify bitsizes as integers!");
            System.out.println(opt.usage());
        }
        System.exit(1);
    }
}
