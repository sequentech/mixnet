




/*
 * Copyright 2011 Niko Farhan, Douglas Wikström
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

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.*;

// Enabled calls to native code begins here.

import jecn.ECN;

// Enabled calls to native code ends here

import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.ui.*;
import verificatum.util.*;

/**
 * An abstract class that implements an Elliptic Curve group over a
 * prime order field defined by a polynomial y^2 = x^3-ax-b on
 * Weierstrass normal form.
 *
 * @author Niko Farhan
 * @author Douglas Wikström
 */
public class JECPGroup extends BPGroup {

    /**
     * Field to perform the operations over.
     */
    protected PField field;

    /**
     * x-coefficient of the curve polynomial.
     */
    private PFieldElement a;

    /**
     * Constant coefficient of the curve polynomial.
     */
    private PFieldElement b;

// Enabled calls to native code begins here.

    /**
     * Order of underlying field represented in two's complement.
     */
    byte[] fieldOrdera;

    /**
     * x-coefficient of curve represented in two's complement.
     */
    byte[] aa;

    /**
     * Constant coefficient of curve represented in two's complement.
     */
    byte[] ba;

// Enabled calls to native code ends here

    /**
     * Generator of the group.
     */
    private JECPGroupElement g;

    /**
     * Unit element of the group.
     */
    private JECPGroupElement one;

    /**
     * Creates a new instance of the group from a byte tree.
     *
     * @param btr Representation of instance.
     * @param rs Source of randomness.
     * @param certainty Determines the probability that a non-prime
     * modulus is deemed prime.
     * @return Group given by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * an instance.
     */
    public static JECPGroup newInstance(ByteTreeReader btr, RandomSource rs,
                                        int certainty)
    throws ArithmFormatException {
        try {

            LargeInteger a = new LargeInteger(btr.getNextChild());
            LargeInteger b = new LargeInteger(btr.getNextChild());
            LargeInteger modulus = new LargeInteger(btr.getNextChild());
            LargeInteger order = new LargeInteger(btr.getNextChild());
            LargeInteger gx = new LargeInteger(btr.getNextChild());
            LargeInteger gy = new LargeInteger(btr.getNextChild());

            return new JECPGroup(a, b, new PField(modulus, rs, certainty),
                                 order, gx, gy);

        } catch (EIOException eioe) {
            throw new ArithmFormatException("Invalid modulus!", eioe);
        }
    }

    /**
     * Constructs an instance from the curve parameters.
     *
     * @param a Constant coefficient.
     * @param b x-coefficient
     * @param field Field over which to perform calculations.
     * @param order Order of the group.
     * @param gx x-coordinate of the generator.
     * @param gy y-coordinate of the generator.
     * @throws ArithmFormatException If the input parameters are
     * inconsistent.
     */
    protected JECPGroup(LargeInteger a, LargeInteger b, PField field,
                       LargeInteger order, LargeInteger gx, LargeInteger gy)
    throws ArithmFormatException {
        super();
        super.init(new PField(order));
        this.field = field;
        this.a = new PFieldElement(field, a);
        this.b = new PFieldElement(field, b);
        this.g = new JECPGroupElement(this, gx, gy);
        this.one = JECPGroupElement.one(this);

// Enabled calls to native code begins here.

        // When using native code we avoid converting these parameters
        // for each native call.
        fieldOrdera = field.getOrder().toByteArray();
        aa = a.toByteArray();
        ba = b.toByteArray();

// Enabled calls to native code ends here
    }

    /**
     * @return Field over which calculations are performed.
     */
    public PField getModulusField() {
        return field;
    }

    /**
     * @return x-coefficient of the curve polynomial.
     */
    public LargeInteger getA() {
        return a.toLargeInteger();
    }

    /**
     * @return Constant coefficient of the curve polynomial.
     */
    public LargeInteger getB() {
        return b.toLargeInteger();
    }

    // Documented in PGroup.java

    @Override
    public String toString() {
        return "JECPGroup(p=" + getModulusField().getOrder().toString() +
            ", a=" + a.toLargeInteger().toString() +
            ", b=" + b.toLargeInteger().toString() +
            ", g_x=" + g.getX().toString() +
            ", g_y=" + g.getY().toString() + ")";
    }

    public JECPGroupElement getg() {
        return g;
    }

    public JECPGroupElement getONE() {
        return one;
    }

    public int getByteLength() {
        return getONE().toByteArray().length;
    }

    public int getEncodeLength() {

        // Add 4 bytes for size and 2 for padding.
        return getModulusField().getEncodeLength() - 6;
    }

    @Override
    public PGroupElement toElement(ByteTreeReader btr)
    throws ArithmFormatException {
        return new JECPGroupElement(this, btr);
    }


    /*
     * This function returns an (x,y) that conforms to the
     * curve equation in the following form:
     *      Most significant                                Least significant
     *  x = [length bytes of data][4 bytes of length of data][2 bytes padding]
     *  We need the padding to make sure there is a possible y for the encoded
     *  data.
     */
    public PGroupElement encode(byte[] byteArray, int startIndex, int length) {

        // Number of bytes to encode.
        int len = Math.min(length, getEncodeLength());

        // Bytes to encode.
        byte[] data = new byte[len];
        System.arraycopy(byteArray, startIndex, data, 0, len);

        // "Append" the length of the data to be encoded as an int and
        // padding as a short.
        BigInteger x = new BigInteger(1, data);
        x = x.shiftLeft(4 * 8);
        x = x.add(BigInteger.valueOf(data.length));
        x = x.shiftLeft(2 * 8);

        BigInteger fieldOrder = getModulusField().getOrder().value;

        // Increase padding until we can interpret l as the x
        // coefficient of a point of the curve.

        BigInteger y = null;
        int i = 0;
        while (y == null && i < (1<<16)) {
            try {

                BigInteger fx = f(x).value.value;
                y = ressol(fx, fieldOrder).mod(fieldOrder);

            } catch (ArithmException e) {
                i++;
                x = x.add(BigInteger.ONE);
            }
        }

        if (y == null) {
            throw new ArithmError("Failed to encode!");
        }

        try {
            return new JECPGroupElement(this,
                                        new LargeInteger(x),
                                        new LargeInteger(y));
        } catch (ArithmFormatException e) {
            throw new ArithmError("Failed to encode! " +
                                  "(this should never happen)", e);
        }
    }

    public PGroupElement randomElement(RandomSource rs, int statDist) {
        for (int j = 0; j < Integer.MAX_VALUE; j++) {

            try {
                LargeInteger x = new LargeInteger(getModulusField().getOrder(),
                                                  statDist,
                                                  rs);
                BigInteger bi = ressol(f(x.value).value.value,
                                       getModulusField().getOrder().value);
                LargeInteger y = new LargeInteger(bi);

                return new JECPGroupElement(this, x, y);

            } catch (ArithmException e) {

                // continue

            } catch (ArithmFormatException e) {
                throw new ArithmError("Unexpected format exception", e);
            }
        }
        throw new ArithmError("Failed to randomize a ro element");
    }

    public PGroupElementArray randomElementArray(int size, RandomSource rs,
                                                 int statDist) {
        PGroupElement[] res = new PGroupElement[size];
        for (int i = 0; i < size; i++) {
            res[i] = randomElement(rs, statDist);
        }

        return toElementArray(res);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JECPGroup)) {
            return false;
        }
        JECPGroup other = (JECPGroup) obj;

        if (!a.value.equals(other.a.value)) {
            return false;
        }
        if (!b.value.equals(other.b.value)) {
            return false;
        }
        if (!field.getOrder().equals(other.field.getOrder())) {
            return false;
        }
        if (!g.equals(other.g)) {
            return false;
        }
        return true;
    }


    // This overrides a method in PGroup.java

// Enabled calls to native code begins here.
    public PGroupElement expProd(final PGroupElement[] bases,
                                 final PRingElement[] exponents) {
	if (bases.length != exponents.length) {
	    throw new ArithmError("Different lengths of inputs!");
	}

        // We need to collect partial results from multiple threads in
        // a thread-safe way.
	final List<PGroupElement> parts =
	    Collections.synchronizedList(new LinkedList<PGroupElement>());

        final JECPGroup pGroup = this;

	ArrayWorker worker =
	    new ArrayWorker(bases.length) {

		public void work(int start, int end) {

                    int batchSize = end - start;

                    byte[][] basesx = new byte[batchSize][];
                    byte[][] basesy = new byte[batchSize][];
                    byte[][] integers = new byte[batchSize][];

                    for (int i = 0, j = start; i < batchSize; i++, j++) {

                        basesx[i] =
                            ((JECPGroupElement)bases[j]).x.toByteArray();
                        basesy[i] =
                            ((JECPGroupElement)bases[j]).y.toByteArray();
                        integers[i] =
                            ((PFieldElement)exponents[j]).toLargeInteger().
                            toByteArray();
                    }

                    byte[][] res =
                        ECN.sexp(fieldOrdera, aa, ba, basesx, basesy, integers);

                    try {
                        PGroupElement part =
                            new JECPGroupElement(pGroup,
                                                 new LargeInteger(res[0]),
                                                 new LargeInteger(res[1]));
                        parts.add(part);

                    } catch (ArithmFormatException afe) {
                        throw new ArithmError("Unable to create point!", afe);
                    }
                }
            };
	worker.work(expThreadThreshold);

	PGroupElement res = getONE();
	for (PGroupElement part : parts) {
	    res = res.mul(part);
	}
	return res;
    }

// Enabled calls to native code ends here


    // Documented in Marshalizable.java

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(y^2 = x^3 + ax + b mod p, bit-length of p = " +
            field.getOrder().bitLength() + ")";
    }

    public ByteTree toByteTree() {
        return new ByteTree(a.toByteTree(),
                            b.toByteTree(),
                            field.getOrder().toByteTree(),
                            getElementOrder().toByteTree(),
                            g.getX().toByteTree(),
                            g.getY().toByteTree());
    }


    // Helper functions from here on.

    /**
     * Checks whether the point (x,y) is on the curve as defined by
     * this group.
     *
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @return true if the point is on the curve.
     */
    public boolean isPointOnCurve(LargeInteger x, LargeInteger y) {
        if (JECPGroupElement.isUnity(x, y)) {
            return true;
        }
        if (x.compareTo(LargeInteger.ZERO) < 0 ||
            y.compareTo(LargeInteger.ZERO) < 0) {
            return false;
        }
        if (x.compareTo(field.getOrder()) >= 0 ||
            y.compareTo(field.getOrder()) >= 0) {
            return false;
        }
        PFieldElement pfey = new PFieldElement(field, y);
        PFieldElement right = f(x.value);

        return right.equals(pfey.mul(pfey));
    }

    /**
     * Applies the curve's formula f(x) = x^3 - ax - b on the given
     * parameter.
     * @param x Parameter of the formula.
     * @return Polynomial evaluated at the given point.
     */
    private PFieldElement f(BigInteger x) {
        PFieldElement pfex = new PFieldElement(field, new LargeInteger(x));
        PFieldElement right = pfex.mul(pfex).mul(pfex);
        right = right.add(pfex.mul(a));
        right = right.add(b);
        return right;
    }


    /* The following code was copied with small modifications from The
     * flexi package http://www.flexiprovider.de/ , commons subpackage
     * (license: LGPL)
     */

    private static final BigInteger TWO = BigInteger.valueOf(2);

    private static final int[] jacobiTable = { 0, 1, 0, -1, 0, -1, 0, 1 };

    /**
     * Computes the value of the Jacobi symbol (A|B). The following properties
     * hold for the Jacobi symbol which makes it a very efficient way to
     * evaluate the Legendre symbol
     * <p>
     * (A|B) = 0 IF gcd(A,B) > 1<br>
     * (-1|B) = 1 IF n = 1 (mod 1)<br>
     * (-1|B) = -1 IF n = 3 (mod 4)<br>
     * (A|B) (C|B) = (AC|B)<br>
     * (A|B) (A|C) = (A|CB)<br>
     * (A|B) = (C|B) IF A = C (mod B)<br>
     * (2|B) = 1 IF N = 1 OR 7 (mod 8)<br>
     * (2|B) = 1 IF N = 3 OR 5 (mod 8)
     * <p>
     *
     * @param A Integer value
     * @param B Integer value
     * @return value of the jacobi symbol (A|B)
     */
    public static int jacobi(BigInteger A, BigInteger B) {
        BigInteger a, b, v;
        long k = 1;

        k = 1;

        // test trivial cases
        if (B.equals(ZERO)) {
            a = A.abs();
            return a.equals(ONE) ? 1 : 0;
        }

        if (!A.testBit(0) && !B.testBit(0)) {
            return 0;
        }

        a = A;
        b = B;

        if (b.signum() == -1) { // b < 0
            b = b.negate(); // b = -b
            if (a.signum() == -1) {
                k = -1;
            }
        }

        v = ZERO;
        while (!b.testBit(0)) {
            v = v.add(ONE); // v = v + 1
            b = b.divide(TWO); // b = b/2
        }

        if (v.testBit(0)) {
            k = k * jacobiTable[a.intValue() & 7];
        }

        if (a.signum() < 0) { // a < 0
            if (b.testBit(1)) {
                k = -k; // k = -k
            }
            a = a.negate(); // a = -a
        }

        // main loop
        while (a.signum() != 0) {
            v = ZERO;
            while (!a.testBit(0)) { // a is even
                v = v.add(ONE);
                a = a.divide(TWO);
            }
            if (v.testBit(0)) {
                k = k * jacobiTable[b.intValue() & 7];
            }

            if (a.compareTo(b) < 0) { // a < b
                // swap and correct intermediate result
                BigInteger x = a;
                a = b;
                b = x;
                if (a.testBit(1) && b.testBit(1)) {
                    k = -k;
                }
            }
            a = a.subtract(b);
        }

        return b.equals(ONE) ? (int) k : 0;
    }

    /**
     * Computes the square root of a BigInteger modulo a prime
     * employing the Shanks-Tonelli algorithm.
     *
     * @param a Value out of which we extract the square root
     * @param p Prime modulus that determines the underlying field
     * @return a number <tt>b</tt> such that b<sup>2</sup> = a (mod p) if
     *         <tt>a</tt> is a quadratic residue modulo <tt>p</tt>.
     * @throws NoQuadraticResidueException
     *             if <tt>a</tt> is a quadratic non-residue modulo <tt>p</tt>
     */
    public static BigInteger ressol(BigInteger a, BigInteger p)
    throws ArithmException {

        BigInteger v = null;

        if (a.compareTo(ZERO) < 0) {
            a = a.add(p);
        }

        if (a.equals(ZERO)) {
            return ZERO;
        }

        if (p.equals(TWO)) {
            return a;
        }

        // p = 3 mod 4
        if (p.testBit(0) && p.testBit(1)) {
            if (jacobi(a, p) == 1) { // a quadr. residue mod p
                v = p.add(ONE); // v = p+1
                v = v.shiftRight(2); // v = v/4
                return a.modPow(v, p); // return a^v mod p
                // return --> a^((p+1)/4) mod p
            }
            throw new ArithmException("sqrt(" + a.toString() + ") mod " +
                                      p.toString());
        }

        long t = 0;

        // initialization
        // compute k and s, where p = 2^s (2k+1) +1

        BigInteger k = p.subtract(ONE); // k = p-1
        long s = 0;
        while (!k.testBit(0)) { // while k is even
            s++; // s = s+1
            k = k.shiftRight(1); // k = k/2
        }

        k = k.subtract(ONE); // k = k - 1
        k = k.shiftRight(1); // k = k/2

        // initial values
        BigInteger r = a.modPow(k, p); // r = a^k mod p

        BigInteger n = r.multiply(r).remainder(p); // n = r^2 % p
        n = n.multiply(a).remainder(p); // n = n * a % p
        r = r.multiply(a).remainder(p); // r = r * a %p

        if (n.equals(ONE)) {
            return r;
        }

        // non-quadratic residue
        BigInteger z = TWO; // z = 2
        while (jacobi(z, p) == 1) {
            // while z quadratic residue
            z = z.add(ONE); // z = z + 1
        }

        v = k;
        v = v.multiply(TWO); // v = 2k
        v = v.add(ONE); // v = 2k + 1
        BigInteger c = z.modPow(v, p); // c = z^v mod p

        // iteration
        while (n.compareTo(ONE) == 1) { // n > 1
            k = n; // k = n
            t = s; // t = s
            s = 0;

            while (!k.equals(ONE)) { // k != 1
                k = k.multiply(k).mod(p); // k = k^2 % p
                s++; // s = s + 1
            }

            t -= s; // t = t - s
            if (t == 0) {
                throw new ArithmException("sqrt(" + a.toString() + ") mod " +
                                          p.toString());
            }

            v = ONE;
            for (long i = 0; i < t - 1; i++) {
                v = v.shiftLeft(1); // v = 1 * 2^(t - 1)
            }
            c = c.modPow(v, p); // c = c^v mod p
            r = r.multiply(c).remainder(p); // r = r * c % p
            c = c.multiply(c).remainder(p); // c = c^2 % p
            n = n.multiply(c).mod(p); // n = n * c % p
        }
        return r;
    }
}
