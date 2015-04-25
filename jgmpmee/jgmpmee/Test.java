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
 * Tests the functionality of {@link JGMPMEE}.
 *
 * @author Douglas Wikstrom
 */
public class Test {

    /**
     * Number of individual spowm tests performed.
     */
    final static int no_spowm_tests = 10;

    public static void usage() {
	System.out.println("Usage: (-e|-f|-se) <len> "
			   + "<modulus bit len> [<exponent bit len>]");
	System.out.println("-e  Test plain modular exponentiation.");
	System.out.println("-f  Test fixed base modular exponentiation.");
	System.out.println("-se Test simultaneous modular exponentiation.");
	System.exit(0);
    }

    public static void main(String args[]) {
	if (args.length < 3) {
	    usage();
	}

	String cmd = args[0];

	int len = Integer.parseInt(args[1]);
	int modulusBitlen = Integer.parseInt(args[2]);

	int exponentBitlen = modulusBitlen;
	if (args.length == 4) {
	    exponentBitlen = Integer.parseInt(args[3]);
	}

	if (cmd.equals("-e")) {
	    System.out.print("Testing JGMPMEE.powm... ");
	    if (test_powm(len, modulusBitlen, exponentBitlen)) {
		System.out.println("done.");
	    } else {
		System.out.println("failed.");
	    }
	} else if (cmd.equals("-f")) {
	    System.out.print("Testing JGMPMEE.fpowm... ");
	    if (test_fpowm(len, modulusBitlen, exponentBitlen)) {
		System.out.println("done.");
	    } else {
		System.out.println("failed.");
	    }
	} else if (cmd.equals("-se")) {
	    System.out.print("Testing JGMPMEE.spowm... ");
	    if (test_spowm(len / no_spowm_tests,
			   modulusBitlen, exponentBitlen)) {
		System.out.println("done.");
	    } else {
		System.out.println("failed.");
	    }
	}
    }

    /**
     * Tests {@link JGMPMEE#powm(BigInteger, BigInteger, BigInteger)} by
     * randomly selecting elements and moduli and verifying the
     * results with {@link BigInteger#modPow(BigInteger, BigInteger,
     * BigInteger)}.
     *
     * @param len Number of exponentiations performed.
     * @param modulusBitlen Bit length of the moduli and bases.
     * @param exponentBitlen Bit length of exponents.
     * @return Returns <code>true</code> or <code>false</code>
     * depending on if the test succeeded or not.
     */
    public static boolean test_powm(int len,
				    int modulusBitlen,
				    int exponentBitlen) {

	Random random = new Random();
	BigInteger[] moduli = new BigInteger[len];
	BigInteger[] bases = new BigInteger[len];
	BigInteger[] exponents = new BigInteger[len];

	for (int i = 0; i < len; i++) {
	    bases[i] = new BigInteger(modulusBitlen, random);
	    exponents[i] = new BigInteger(exponentBitlen, random);
	    moduli[i] = new BigInteger(modulusBitlen, random);
	    if (moduli[i].compareTo(BigInteger.ZERO) == 0) {
		moduli[i] = BigInteger.ONE;
	    }
	}

	for (int i = 0; i < len; i++) {

	    BigInteger java_res = bases[i].modPow(exponents[i], moduli[i]);
	    BigInteger mpz_res =
		JGMPMEE.powm(bases[i], exponents[i], moduli[i]);

	    if (java_res.compareTo(mpz_res) != 0) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Tests {@link FpowmTab#fpowm(BigInteger)} by randomly selecting
     * elements and moduli and verifying the results with {@link
     * BigInteger#modPow(BigInteger, BigInteger, BigInteger)}.
     *
     * @param len Number of exponentiations performed.
     * @param modulusBitlen Bit length of the moduli and bases.
     * @param exponentBitlen Bit length of exponents.
     * @return Returns <code>true</code> or <code>false</code>
     * depending on if the test succeeded or not.
     */
    public static boolean test_fpowm(int len,
				     int modulusBitlen,
				     int exponentBitlen) {

	Random random = new Random();
	BigInteger modulus = new BigInteger(modulusBitlen, random);
	if (modulus.compareTo(BigInteger.ZERO) == 0) {
	    modulus = BigInteger.ONE;
	}
	BigInteger base = new BigInteger(modulusBitlen, random);
	base = base.mod(modulus);

	FpowmTab tab = new FpowmTab(base, modulus, exponentBitlen);

	for (int i = 0; i < len; i++) {
	    BigInteger exponent = new BigInteger(exponentBitlen, random);

	    BigInteger java_res = base.modPow(exponent, modulus);
	    BigInteger mpz_res = tab.fpowm(exponent);

	    if (java_res.compareTo(mpz_res) != 0) {
		return false;
	    }
	}
	return true;
    }



    /**
     * Computes a simultaneous modular exponentiation. This is a naive
     * implementation in Java.
     *
     * @param bases Basis element.
     * @param exponents Exponent used to compute power.
     * @param modulus Modulus.
     * @return Product of the bases to the powers of
     * <code>exponents</code> modulo <code>modulus</code>.
     */
    protected static BigInteger modPowProd(BigInteger[] bases,
					   BigInteger[] exponents,
					   BigInteger modulus) {
	BigInteger res = BigInteger.ONE;
	for (int i = 0; i < bases.length; i++) {
	    res = res.multiply(bases[i].modPow(exponents[i], modulus));
	    res = res.mod(modulus);
	}
	return res;
    }


    /**
     * Tests {@link JGMPMEE#spowm(BigInteger[], BigInteger[], BigInteger)}
     * by randomly selecting elements and moduli and verifying the
     * results with {@link JGMPMEE#modProdPow(BigInteger[], BigInteger[],
     * BigInteger)}.
     *
     * @param len Number of exponentiations performed.
     * @param modulusBitlen Bit length of the moduli and bases.
     * @param exponentBitlen Bit length of exponents.
     * @return Returns <code>true</code> or <code>false</code>
     * depending on if the test succeeded or not.
     */
    public static boolean test_spowm(int len,
				     int modulusBitlen,
				     int exponentBitlen) {

	Random random = new Random();
	BigInteger modulus;
	BigInteger[] bases = new BigInteger[len];
	BigInteger[] exponents = new BigInteger[len];

	for (int j = 0; j < no_spowm_tests; j++) {

	    for (int i = 0; i < len; i++) {
		bases[i] = new BigInteger(modulusBitlen, random);
		exponents[i] = new BigInteger(exponentBitlen, random);
	    }
	    modulus = new BigInteger(modulusBitlen, random);
	    if (modulus.compareTo(BigInteger.ZERO) == 0) {
		modulus = BigInteger.ONE;
	    }

	    BigInteger java_res = modPowProd(bases, exponents, modulus);
	    BigInteger mpz_res = JGMPMEE.spowm(bases, exponents, modulus);

	    if (java_res.compareTo(mpz_res) != 0) {
		return false;
	    }
	}
	return true;
    }
}
