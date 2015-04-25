
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

import java.util.*;

import verificatum.eio.*;

/**
 * Implements an immutable polynomial over a {@link PRing} instance.
 *
 * @author Douglas Wikstrom
 */
public class Polynomial
    implements ByteTreeConvertible, PRingAssociated {

    /**
     * Coefficients of the polynomial.
     */
    protected PRingElement[] coefficients;

    /**
     * Creates an uninitialized polynomial. It is the responsibility
     * of the programmer to properly initialize the created instance
     * if this constructor is used.
     */
    protected Polynomial() {}

    /**
     * Creates a partially initialized polynomial. It is the
     * responsibility of the programmer to properly initialize the
     * created instance if this constructor is used.
     *
     * @param degree Degree of the polynomial.
     */
    protected Polynomial(int degree) {
        this.coefficients = new PRingElement[degree + 1];
    }

    /**
     * Creates a zero-degree polynomial.
     *
     * @param constant Value of the constant coefficient.
     */
    public Polynomial(PRingElement constant) {
        this(0);
        this.coefficients[0] = constant;
    }

    /**
     * Canonicalizes the underlying array of ring elements if needed,
     * i.e., reduces the degree of the polynomial as long as the
     * maximal degree coefficient is zero.
     */
    protected void canonicalize() {
        int d = coefficients.length - 1;
        while (d > 0 && coefficients[d].equals(getPRing().getZERO())) {
            d--;
        }
        if (d < coefficients.length - 1) {
            coefficients = Arrays.copyOfRange(coefficients, 0, d + 1);
        }
    }

    /**
     * Creates a polynomial from a list of coefficients. The
     * coefficients are not copied.
     *
     * @param coefficients Coefficients of the polynomial.
     */
    public Polynomial(PRingElement ... coefficients) {
        if (coefficients.length == 0) {
            throw new ArithmError("No coefficients!");
        }
        this.coefficients = coefficients;
        canonicalize();
    }

    /**
     * Returns the polynomials corresponding to this one over the
     * factors of the underlying ring.
     *
     * @return Polynomials corresponding to this one over the factors
     * of the underlying ring.
     * @throws ArithmError If this instance is not defined over a
     * product ring.
     */
    public Polynomial[] getFactors() {
        if (!(coefficients[0] instanceof PPRingElement)) {
            throw new ArithmError("Element is not a product!");
        }
        int width = ((PPRing)coefficients[0].getPRing()).getWidth();

        PRingElement[][] factored = new PRingElement[coefficients.length][];
        for (int i = 0; i < factored.length; i++) {
            factored[i] = ((PPRingElement)coefficients[i]).getFactors();
        }
        Polynomial[] polys = new Polynomial[width];
        for (int l = 0; l < width; l++) {
            PRingElement[] tmp = new PRingElement[coefficients.length];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = factored[i][l];
            }
            polys[l] = new Polynomial(tmp);
        }
        return polys;
    }

    /**
     * Creates a polynomial from another instance.
     *
     * @param poly A polynomial.
     */
    public Polynomial(Polynomial poly) {
        this(poly.coefficients);
    }

    /**
     * Initializes a polynomial from the input representation.
     *
     * @param pRing Ring over which this instance is defined.
     * @param maxDegree Maximal degree of polynomial.
     * @param btr Representation of polynomial.
     *
     * @throws ArithmFormatException If the input does not represent a
     * polynomial over the given ring.
     */
    protected void init(PRing pRing, int maxDegree, ByteTreeReader btr)
    throws ArithmFormatException {
        coefficients = pRing.toElements(maxDegree + 1, btr);
        canonicalize();
    }

    /**
     * Creates a polynomial from the input representation.
     *
     * @param pRing Ring over which this instance is defined.
     * @param maxDegree Maximal degree of polynomial.
     * @param btr Representation of polynomial.
     *
     * @throws ArithmFormatException If the input does not represent a
     * polynomial over the given ring.
     */
    public Polynomial(PRing pRing, int maxDegree, ByteTreeReader btr)
        throws ArithmFormatException {
        init(pRing, maxDegree, btr);
    }


    /**
     * Creates a representation of this instance.
     *
     * @return Representation.
     */
    public ByteTreeBasic toByteTree() {
        return getPRing().toByteTree(coefficients);
    }

    /**
     * Returns the degree of the polynomial.
     *
     * @return Degree of this polynomial.
     */
    public int getDegree() {
        return coefficients.length - 1;
    }

    /**
     * Returns the <code>index</code>th coefficient of the
     * polynomial. This is zero for all indices greater than the
     * degree.
     *
     * @param index Index of coefficient.
     * @return <code>index</code>th coefficient.
     */
    public PRingElement getCoefficient(int index) {
        if (index < coefficients.length) {
            return coefficients[index];
        } else {
            return getPRing().getZERO();
        }
    }

    /**
     * Evaluates the polynomial at the integer given as input.
     *
     * @param j Input.
     * @return Image of input under polynomial.
     */
    public PRingElement evaluate(int j) {
        return evaluate(coefficients[0].getPRing().getPField().toElement(j));
    }

    /**
     * Evaluates the polynomial at the point given as input and
     * returns the result.
     *
     * @param el Point at which this polynomial is evaluated.
     * @return Image of input under polynomial.
     */
    public PRingElement evaluate(PFieldElement el) {
        if (!getPRing().getPField().equals(el.getPRing())) {
            throw new ArithmError("Distinct fields!");
        }
        PRingElement value = coefficients[0];
        PFieldElement elPower = el;

        for(int i = 1; i <= getDegree(); i++) {
            value = value.add(coefficients[i].mul(elPower));
            elPower = elPower.mul(el);
        }
        return value;
    }

    /**
     * Adds the input to this instance and returns the result, i.e.,
     * the sum of this polynomial and the input.
     *
     * @param poly Polynomial added to this instance.
     * @return Sum of this polynomial and the input.
     */
    public Polynomial add(Polynomial poly) {
        if (!getPRing().equals(poly.getPRing())) {
            throw new ArithmError("Distinct rings!");
        }
        Polynomial p1 = this;
        Polynomial p2 = poly;
        if (p1.getDegree() < p2.getDegree()) {
            Polynomial temp = p1;
            p1 = p2;
            p2 = temp;
        }
        PRingElement[] sum = new PRingElement[p1.getDegree() + 1];
        int i = 0;
        for(; i <= p2.getDegree(); i++) {
            sum[i] = p1.coefficients[i].add(p2.coefficients[i]);
        }
        System.arraycopy(p1.coefficients, i,
                         sum, i,
                         (p1.getDegree() + 1) - i);

        Polynomial sumPoly = new Polynomial(sum);
        sumPoly.canonicalize();

        return sumPoly;
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the input represents the same polynomial as this polynomial or
     * not.
     *
     * @param obj Polynomial to compare with.
     * @return <code>true</code> or <code>false</code> depending on if
     * the input equals this polynomial or not.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Polynomial)) {
            return false;
        }
        Polynomial p = (Polynomial)obj;
        if (!getPRing().equals(p.getPRing())) {
            return false;
        }
        if (getDegree() != p.getDegree()) {
            return false;
        }
        for (int i = 0; i < coefficients.length; i++) {
            if (!coefficients[i].equals(p.coefficients[i])) {
                return false;
            }
        }
        return true;
    }

    // Documented in PRingAssociated.java

    public PRing getPRing() {
        return coefficients[0].getPRing();
    }
}
