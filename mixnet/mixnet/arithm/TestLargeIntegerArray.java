
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

package mixnet.arithm;

import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;

import mixnet.test.*;

/**
 * Tests some of the functionality of {@link LargeInteger}.
 *
 * @author Douglas Wikstrom
 */
public class TestLargeIntegerArray {

    static LargeInteger modulus = null;

    static LargeIntegerArray a = null;
    static LargeIntegerArray b = null;
    static LargeIntegerArray c = null;
    static LargeIntegerArray d = null;
    static LargeIntegerArray e = null;

    static LargeInteger x = null;
    static LargeInteger y = null;
    static LargeInteger z = null;
    static LargeInteger w = null;

    static int size;

    protected static void setTestDir(File tmpDir) {
        TempFile.init(tmpDir);
	LargeIntegerArray.useFileBased();
    }

    protected static void generateArrays(TestParameters tp) throws Exception {

	if (a == null) {

	    modulus = SafePrimeTable.safePrime(1024);

	    RandomSource rs =
		new PRGHeuristic(tp.prgseed.getBytes());

	    size = 20; //tp.testSize;

	    a = LargeIntegerArray.random(size, modulus, 20, rs);
	    b = LargeIntegerArray.random(size, modulus, 20, rs);
	    c = LargeIntegerArray.random(size, modulus, 20, rs);
	    d = LargeIntegerArray.random(size, modulus, 20, rs);
	    e = LargeIntegerArray.random(size, modulus, 20, rs);

	    x = new LargeInteger(1024, rs);
	    y = new LargeInteger(1024, rs);
	    z = new LargeInteger(1024, rs);
	    w = new LargeInteger(1024, rs);
	}
    }

    protected static LargeIntegerArrayIM toIM(LargeIntegerArray lia) {
	return new LargeIntegerArrayIM(lia.integers());
    }

    protected static LargeIntegerArrayF toF(LargeIntegerArray lia) {
	return new LargeIntegerArrayF(lia.integers());
    }

    public static boolean permuteElements(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        Permutation permutation = new Permutation(a.size(), rs, 100);
        LargeIntegerArray permuted = a.permute(permutation);

        LargeIntegerArray aa = permuted.permute(permutation.inv());

        return a.equals(aa);
    }


    protected static void printPara(LargeIntegerArray x1Array,
				    LargeIntegerArray x2Array) {
	LargeInteger[] x1 = x1Array.integers();
	LargeInteger[] x2 = x2Array.integers();
	for (int i = 0; i < x1.length; i++) {
	    System.err.println("x1[" + i + "] = " + x1[i].toString());
	    System.err.println("x2[" + i + "] = " + x2[i].toString());
	}
    }

    public static boolean copyOfRange(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        int startIndex = size / 4;
        int endIndex = 3 * (size / 4);

        LargeIntegerArray fres = a.copyOfRange(startIndex, endIndex);
        LargeIntegerArray mres = toF(toIM(a).copyOfRange(startIndex, endIndex));

        return fres.equals(mres);
    }

    public static boolean extract(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        boolean[] indices = new boolean[size];
        Arrays.fill(indices, false);
        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
        	indices[i] = true;
            }
        }

        LargeIntegerArray fres = a.extract(indices);
        LargeIntegerArray mres = toF(toIM(a).extract(indices));

        return fres.equals(mres);
    }

    public static boolean equalsAll(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeInteger[] aa = a.integers();
        LargeInteger[] bb = a.integers();

        for (int i = 0; i < aa.length; i++) {
            if (i % 3 == 0) {
        	aa[i] = aa[i].add(bb[i]);
            }
        }

        LargeIntegerArray fa = LargeIntegerArray.toLargeIntegerArray(aa);
        LargeIntegerArray fb = LargeIntegerArray.toLargeIntegerArray(aa);
        boolean[] fr = fa.equalsAll(fb);

        LargeIntegerArray ma = new LargeIntegerArrayIM(aa);
        LargeIntegerArray mb = new LargeIntegerArrayIM(aa);
        boolean[] mr = ma.equalsAll(mb);

        return Arrays.equals(fr, mr);
    }

    public static boolean modMul(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeIntegerArray ma = toF(toIM(a).modMul(toIM(b), modulus));
        LargeIntegerArray fa = a.modMul(b, modulus);

        return fa.equals(ma);
    }

    public static boolean modInner(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeInteger fp = a.modInner(b, modulus);
        LargeInteger mp = toIM(a).modInner(toIM(b), modulus);

        return fp.equals(mp);
    }

    public static boolean modPow0(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeIntegerArray fp = a.modPow(b, modulus);
        LargeIntegerArray mp = toF(toIM(a).modPow(toIM(b), modulus));

        return fp.equals(mp);
    }

    public static boolean modPow1(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeIntegerArray fp = a.modPow(x, modulus);
        LargeIntegerArray mp = toF(toIM(a).modPow(x, modulus));

        return fp.equals(mp);
    }

    public static boolean modPowVariant(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeIntegerArray fp = a.modPowVariant(x, modulus);
        LargeIntegerArray mp = toF(toIM(a).modPowVariant(x, modulus));

        return fp.equals(mp);
    }

    public static boolean modPowProd(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeInteger fp = a.modPowProd(b, modulus);
        LargeInteger mp = toIM(a).modPowProd(toIM(b), modulus);

        return fp.equals(mp);
    }

    public static boolean modProd(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeInteger fp = a.modProd(modulus);
        LargeInteger mp = toIM(a).modProd(modulus);

        return fp.equals(mp);
    }

    public static boolean modProds(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeIntegerArray fp = a.modProds(modulus);
        LargeIntegerArray mp = toF(toIM(a).modProds(modulus));

        return fp.equals(mp);
    }

    public static boolean modSum(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeInteger fp = a.modSum(modulus);
        LargeInteger mp = toIM(a).modSum(modulus);

        return fp.equals(mp);
    }

    public static boolean get(TestParameters tp) throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeIntegerArray aa = toIM(a);

        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(aa.get(i))) {
        	return false;
            }
        }
        return true;
    }

    public static boolean quadraticResidues(TestParameters tp)
        throws Exception {
        setTestDir(tp.tmpDir);
        generateArrays(tp);

        LargeIntegerArray aa = a.modMul(a, modulus);

        return aa.quadraticResidues(modulus);
    }
}
