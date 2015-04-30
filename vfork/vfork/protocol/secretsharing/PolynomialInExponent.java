
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

package vfork.protocol.secretsharing;

import java.util.*;

import vfork.arithm.*;
import vfork.eio.*;
import vfork.protocol.*;

/**
 * Implementation of a "polynomial in the exponent". This immutable
 * class is used in Pedersen's (Feldman's) secret sharing scheme
 * {@link Pedersen}.
 *
 * @author Douglas Wikstrom
 */
class PolynomialInExponent implements PGroupAssociated {

    /**
     * Underlying homomorphism.
     */
    protected HomPRingPGroup hom;

    /**
     * Coefficients in the exponent.
     */
    protected PGroupElement[] coeffInExponent;

    /**
     * Creates an instance.
     *
     * @param hom Underlying homomorphism.
     * @param poly Underlying polynomial.
     */
    public PolynomialInExponent(HomPRingPGroup hom, Polynomial poly) {
        this.hom = hom;
        if (!hom.getDomain().equals(poly.getPRing())) {
            throw new ProtocolError("Incompatible rings!");
        }
        coeffInExponent = new PGroupElement[poly.getDegree() + 1];

        for (int i = 0; i < coeffInExponent.length; i++) {
            coeffInExponent[i] = hom.map(poly.getCoefficient(i));
        }
        canonicalize();
    }

    /**
     * Creates a trivial instance. This is syntactic sugar for calling
     * {@link #PolynomialInExponent(HomPRingPGroup, Polynomial)} with
     * the constant polynomial equal to 1.
     *
     * @param hom Underlying homomorphism.
     */
    PolynomialInExponent(HomPRingPGroup hom) {
        this(hom, new Polynomial(hom.getDomain().getPRing().getONE()));
    }

    /**
     * Initializes an instance with the given group elements. This
     * does not copy the input array.
     *
     * @param Underlying homomorphism.
     * @param coeffInExponent Array of coefficients in the exponent.
     */
    protected PolynomialInExponent(HomPRingPGroup hom,
                                   PGroupElement[] coeffInExponent) {
        this.hom = hom;
        this.coeffInExponent = coeffInExponent;
        if (coeffInExponent.length == 0) {
            throw new ProtocolError("No coefficients!");
        }
        canonicalize();
    }

    /**
     * Creates an instance from a byte tree representation as output
     * by {@link #toByteTree()}.
     *
     * @param Underlying homomorphism.
     * @param maxDegree Maximal degree of polynomial in exponent.
     * @param btr A representation of an instance.
     * @throws ProtocolFormatException If the input does not represent
     * an instance.
     */
    public PolynomialInExponent(HomPRingPGroup hom, int maxDegree,
                                ByteTreeReader btr)
        throws ProtocolFormatException {
        try {
            this.hom = hom;
            PGroup pGroup = hom.getRange();

            coeffInExponent = pGroup.toElements(maxDegree + 1, btr);

            if (coeffInExponent.length == 0) {
                throw new ProtocolFormatException("Zero length!");
            }
            canonicalize();
        } catch (ArithmFormatException afe) {
            throw new ProtocolFormatException("Malformed element!", afe);
        }
    }

    /**
     * Returns the polynomials in the exponents corresponding to this
     * one over the factors of the underlying group. This assumes that
     * factoring is possible.
     *
     * @return Polynomials corresponding to this one over the factors
     * of the underlying group.
     */
    public PolynomialInExponent[] getFactors() {

        HomPRingPGroup[] homs = ((PHomPRingPGroup)hom).getFactors();

        int width = ((PPGroup)coeffInExponent[0].getPGroup()).getWidth();

        PGroupElement[][] factored =
            new PGroupElement[coeffInExponent.length][];

        for (int i = 0; i < factored.length; i++) {
            factored[i] = ((PPGroupElement)coeffInExponent[i]).getFactors();
        }

        PolynomialInExponent[] polys = new PolynomialInExponent[width];
        for (int l = 0; l < width; l++) {
            PGroupElement[] tmp = new PGroupElement[coeffInExponent.length];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = factored[i][l];
            }
            polys[l] = new PolynomialInExponent(homs[l], tmp);
        }
        return polys;
    }

    /**
     * Returns a representation of the instance that can given as
     * input to {@link
     * #PolynomialInExponent(PGroup,ByteTreeReader)}. It is the
     * responsibility of the programmer to separately store the group
     * over which this instance is defined.
     *
     * @return Representation of this instance.
     */
    public ByteTreeBasic toByteTree() {
        return getPGroup().toByteTree(coeffInExponent);
    }

    /**
     * Make sure that the top most coefficient in the exponent is a
     * non-unit element.
     */
    void canonicalize() {
        int index = coeffInExponent.length - 1;
        while (index > 0
               && coeffInExponent[index].equals(getPGroup().getONE())) {
            index--;
        }
        if (index < coeffInExponent.length - 1) {
            coeffInExponent =
                Arrays.copyOfRange(coeffInExponent, 0, index + 1);
        }
    }

    /**
     * Returns the degree of this polynomial in the exponent.
     *
     * @return Degree of this polynomial in the exponent.
     */
    public int getDegree() {
        return coeffInExponent.length - 1;
    }

    /**
     * Returns a given coefficient in the exponent.
     *
     * @param i Index of the coefficient in the exponent to
     * return.
     * @return Coefficient with index <code>i</code>.
     */
    public PGroupElement getElement(int i) {
        if (i >= coeffInExponent.length) {
            return getPGroup().getONE();
        } else {
            return coeffInExponent[i];
        }
    }

    /**
     * Evaluates the polynomial in the exponent at the {@link PField}
     * element given as input.
     *
     * @param el Point at which the polynomial is evaluated in the
     * exponent.
     * @return Value at the given point.
     */
    PGroupElement evaluate(PFieldElement el) {
        PGroupElement value = coeffInExponent[0];
        PFieldElement elPower = el;

        for(int i = 1; i < coeffInExponent.length; i++) {
            value = value.mul(coeffInExponent[i].exp(elPower));
            elPower = elPower.mul(el);
        }
        return value;
    }

    /**
     * Evaluates the polynomial in the exponent at the point
     * corresponding to the integer given as input.
     *
     * @param j Integer representing the point at which the polynomial
     * is evaluated in the exponent.
     * @return Value at the given point.
     */
    PGroupElement evaluate(int j) {
        return evaluate(getPGroup().getPRing().getPField().toElement(j));
    }

    /**
     * Takes the coefficient-wise product of this instance with the
     * input.
     *
     * @param pie Polynomial in the exponent with which this
     * instance is multiplied.
     * @return Product of this instance and the input.
     */
    public PolynomialInExponent mul(PolynomialInExponent pie) {

        PolynomialInExponent p1 = this;
        PolynomialInExponent p2 = pie;

        if (p1.getDegree() < p2.getDegree()) {
            PolynomialInExponent temp = p1;
            p1 = p2;
            p2 = temp;
        }

        PGroupElement[] product = new PGroupElement[p1.getDegree() + 1];
        int i = 0;
        for (; i < p2.coeffInExponent.length; i++) {
            product[i] = p1.coeffInExponent[i].mul(p2.coeffInExponent[i]);
        }
        for (; i < p1.coeffInExponent.length; i++) {
            product[i] = p1.coeffInExponent[i];
        }

        return new PolynomialInExponent(hom, product);
    }

    /**
     * Returns the underlying homomorphism.
     *
     * @return Underlying homomorphism.
     */
    public HomPRingPGroup getHom() {
        return hom;
    }

    // Documented in arithm.PGroupAssociated.java.

    public PGroup getPGroup() {
        return coeffInExponent[0].getPGroup();
    }
}
