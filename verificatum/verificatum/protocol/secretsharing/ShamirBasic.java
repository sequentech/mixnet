
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

package verificatum.protocol.secretsharing;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.protocol.*;

/**
 * Implements the basic functionality for executing Shamir's secret
 * sharing protocol over an instance of {@link PRing}.
 *
 * @author Douglas Wikstrom
 */
public class ShamirBasic extends Polynomial {

    /**
     * Creates an instance from the given coefficients of a
     * polynomial.
     *
     * @param coefficients Coefficients of the polynomial.
     */
    protected ShamirBasic(PRingElement[] coefficients) {
        super(coefficients);
    }

    /**
     * Creates an instance by using the input as sharing polynomial.
     *
     * @param poly Polynomial defining this instance.
     */
    protected ShamirBasic(Polynomial poly) {
        super(poly);
    }

    /**
     * Creates a random instance with the given degree and constant
     * coefficient.
     *
     * @param degree Degree of the random polynomial.
     * @param constcoefficient Value of the constant coefficient.
     * @param randomSource Source of random bits.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public ShamirBasic(int degree, PRingElement constcoefficient,
                       RandomSource randomSource, int statDist) {
        super(degree);

        coefficients[0] = constcoefficient;
        PRing pRing = constcoefficient.getPRing();

        for (int i = 1; i <= degree; i++) {
            coefficients[i] = pRing.randomElement(randomSource, statDist);
        }
        canonicalize();
    }

    /**
     * Creates an instance from a byte tree representation.
     *
     * @param pRing Ring over which the instance is defined.
     * @param maxDegree Maximal degree of polynomial.
     * @param btr Representation of instance.
     * @throws ProtocolFormatException If the input is not a
     * representation of an instance.
     */
    public ShamirBasic(PRing pRing, int maxDegree, ByteTreeReader btr)
    throws ProtocolFormatException {
        try {
            init(pRing, maxDegree, btr);
        } catch (ArithmFormatException afe) {
            String s = "Input does not represent an instance!";
            throw new ProtocolFormatException(s, afe);
        }
    }

    /**
     * Returns the instances corresponding to this one over the
     * factors of the underlying ring.
     *
     * @return Instances corresponding to this one over the factors of
     * the underlying ring.
     */
    public ShamirBasic[] getFactors() {
        Polynomial[] polyFactors = super.getFactors();
        ShamirBasic[] factors = new ShamirBasic[polyFactors.length];
        for (int i = 0; i < factors.length; i++) {
            factors[i] = new ShamirBasic(polyFactors[i]);
        }
        return factors;
    }

    /**
     * Recovers the secret from a list of Shamir secret shares. A
     * standard Lagrange interpolation is performed to compute the
     * output. It is assumed that the values given as input reside on
     * a polynomial with degree equal to the number of elements in the
     * two input arrays minus one, i.e. there are no superfluous data
     * points. If this is not the case, then the output is undefined.
     *
     * @param indices Distinct points at which values of the
     * polynomial are given.
     * @param values Values at these points.
     * @param noShares Number of shares to use, i.e., the degree + 1
     * shares.
     * @return Recovered secret.
     */
    public static PRingElement recover(int[] indices,
                                       PRingElement[] values,
                                       int noShares) {
        int degree = noShares - 1;

        PRing pRing = values[0].getPRing();
        PField pField = pRing.getPField();

        // Transform the integer indices into field elements
        PFieldElement[] indicesPField = new PFieldElement[noShares];
        for (int j = 0; j < noShares; j++) {
            indicesPField[j] = pField.toElement(indices[j]);
        }

        // Lagrange interpolation.
        PRingElement constCoeff = pRing.getZERO();

        PFieldElement product;
        for (int j = 0; j <= degree; j++) {

            product = pField.ONE;

            for (int l = 0; l <= degree; l++) {
                if (l != j) {
                    try {

                        product = product.
                            mul(indicesPField[l].
                                div(indicesPField[l].sub(indicesPField[j])));

                    } catch (ArithmException ae) {

                        // If this method is called with valid data,
                        // this never happens.
                        throw new ProtocolError("Unable to invert!", ae);
                    }
                }
            }
            constCoeff = constCoeff.add(values[j].mul(product));
        }
        return constCoeff;
    }

    /**
     * Returns the sum of this instance and the input.
     *
     * @param sb Instance added to this instance.
     * @return Sum of this instance and the input.
     */
    public ShamirBasic add(ShamirBasic sb) {
        return new ShamirBasic(super.add(sb));
    }
}
