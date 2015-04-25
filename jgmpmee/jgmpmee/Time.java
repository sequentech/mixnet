/*

Copyright 2008 2009 Douglas Wikstrom

This file is part of Java GMP Modular Exponentiation Extension
(JGMPMEE).

JGMPMEE is free software: you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JGMPMEE is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
License for more details.

You should have received a copy of the GNU Lesser General Public
License along with JGMPMEE.  If not, see
<http://www.gnu.org/licenses/>.

*/


package jgmpmee;

import java.math.*;
import java.util.*;

/**
 * Times the methods {@link JGMPMEE.powm(BigInteger, BigInteger,
 * BigInteger)} and {@link JGMPMEE.spowm(BigInteger[], BigInteger[],
 * BigInteger)}.
 *
 * @author Douglas Wikstrom
 */
public class Time {

    public static void usage() {
	System.out.println("Usage: (-e|-se|-f) (-j|-n) <len> "
			   + "<modulus bit len> [<exponent bit len>]");
	System.out.println("-e  Time plain modular exponentiation.");
	System.out.println("-se Time simultaneous modular exponentiation.");
	System.out.println("-f  Time fixed base modular exponentiation.");
	System.exit(0);
    }

    static boolean nativeCode = true;

    public static void main(String args[]) {
	if (args.length < 4) {
	    usage();
	}

	String cmd = args[0];

	String imp = args[1];

	int len = Integer.parseInt(args[2]);
	int modulusBitlen = Integer.parseInt(args[3]);

	int exponentBitlen = modulusBitlen;
	if (args.length == 5) {
	    exponentBitlen = Integer.parseInt(args[4]);
	}

	if (imp.equals("-j")) {
	    nativeCode = false;
	}

	float t = 0;
	if (cmd.equals("-e")) {
	    t = timePowm(len, modulusBitlen, exponentBitlen);
	} else if (cmd.equals("-f")) {
	    t = timeFpowm(len, modulusBitlen, exponentBitlen);
	} else if (cmd.equals("-se")) {
	    t = timeSpowm(len, modulusBitlen, exponentBitlen);
	} else {
	    usage();
	}
	System.out.println(String.format("%.2f", t));
    }

    /**
     * Times modular exponentiation.
     *
     * @param len Number of exponentiations performed.
     * @param modulusBitlen Bit length of the moduli and bases.
     * @param exponentBitlen Bit length of exponents.
     * @return Running time in seconds.
     */
    public static float timePowm(int len,
				 int modulusBitlen,
				 int exponentBitlen) {

	Random random = new Random();
	BigInteger modulus = new BigInteger(modulusBitlen, random);
	if (modulus.compareTo(BigInteger.ZERO) == 0) {
	    modulus = BigInteger.ONE;
	}
	BigInteger[] bases = new BigInteger[len];
	BigInteger[] exponents = new BigInteger[len];


	for (int i = 0; i < len; i++) {
	    bases[i] = new BigInteger(modulusBitlen, random);
	    exponents[i] = new BigInteger(exponentBitlen, random);
	}

	long startTime = System.currentTimeMillis();

	if (nativeCode) {

	    for (int i = 0; i < len; i++) {
		BigInteger res = JGMPMEE.powm(bases[i], exponents[i], modulus);
	    }

	} else {

	    for (int i = 0; i < len; i++) {
		BigInteger res = bases[i].modPow(exponents[i], modulus);
	    }

	}

	long elapsed = System.currentTimeMillis() - startTime;

	return ((float)elapsed) / 1000;
    }

    /**
     * Times fixed base modular exponentiation.
     *
     * @param len Number of exponentiations performed.
     * @param modulusBitlen Bit length of the moduli and bases.
     * @param exponentBitlen Bit length of exponents.
     * @return Running time in seconds.
     */
    public static float timeFpowm(int len,
				  int modulusBitlen,
				  int exponentBitlen) {

	Random random = new Random();
	BigInteger modulus = new BigInteger(modulusBitlen, random);
	BigInteger base = new BigInteger(modulusBitlen, random);
	if (modulus.compareTo(BigInteger.ZERO) == 0) {
	    modulus = BigInteger.ONE;
	}

	BigInteger[] exponents = new BigInteger[len];

	for (int i = 0; i < len; i++) {
	    exponents[i] = new BigInteger(exponentBitlen, random);
	}

	long startTime = System.currentTimeMillis();

	if (nativeCode) {

	    FpowmTab tab = new FpowmTab(base, modulus, exponentBitlen);

	    for (int i = 0; i < len; i++) {
		BigInteger res = tab.fpowm(exponents[i]);
	    }

	} else {

	    for (int i = 0; i < len; i++) {
		BigInteger res = base.modPow(exponents[i], modulus);
	    }

	}

	long elapsed = System.currentTimeMillis() - startTime;

	return ((float)elapsed) / 1000;
    }

    /**
     * Times simultaneous modular exponentiation.
     *
     * @param len Number of exponentiations performed.
     * @param modulusBitlen Bit length of the moduli and bases.
     * @param exponentBitlen Bit length of exponents.
     * @return Running time in seconds.
     */
    public static float timeSpowm(int len,
				   int modulusBitlen,
				   int exponentBitlen) {

	Random random = new Random();
	BigInteger modulus = new BigInteger(modulusBitlen, random);
	if (modulus.compareTo(BigInteger.ZERO) == 0) {
	    modulus = BigInteger.ONE;
	}
	BigInteger[] bases = new BigInteger[len];
	BigInteger[] exponents = new BigInteger[len];

	for (int i = 0; i < len; i++) {
	    bases[i] = new BigInteger(modulusBitlen, random);
	    exponents[i] = new BigInteger(exponentBitlen, random);
	}

	long startTime = System.currentTimeMillis();

	if (nativeCode) {

	    BigInteger res = JGMPMEE.spowm(bases, exponents, modulus);

	} else {

	    BigInteger res = BigInteger.ONE;
	    for (int i = 0; i < bases.length; i++) {

		res = res.multiply(bases[i].modPow(exponents[i], modulus)).
		    mod(modulus);

	    }

	}

	long elapsed = System.currentTimeMillis() - startTime;

	return ((float)elapsed) / 1000;
    }
}
