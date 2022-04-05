
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
import java.lang.reflect.*;
import java.util.*;

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.util.*;

/**
 * Abstract class representing a group for cryptographic use, where
 * each element has the same prime order. The group is not necessarily
 * cyclic. (To check if a group is cyclic one can check if its
 * associated {@link PRing} is a {@link PField}.) Elements in the
 * group are represented by the abstract class {@link PGroupElement}
 * and the "exponents" of this group is represented by {@link
 * PRing}. Arrays of group elements are handled by {@link
 * PGroupElementArray}.
 *
 * @author Douglas Wikstrom
 */
public abstract class PGroup implements Marshalizable, PRingAssociated {

    /**
     * Ring associated with this instance.
     */
    protected PRing pRing;

    /**
     * Breakpoint at which exponentiation of
     * <code>PGroupElement[]</code> is threaded.
     */
    protected int expThreadThreshold = 100;

    /**
     * Breakpoint at which multiplication (and other operations with
     * similar cost) of <code>PGroupElement[]</code> is threaded.
     */
    protected int mulThreadThreshold = 1000;

    /**
     * Creates a group. It is the responsibility of the programmer to
     * initialize this instance, e.g., by calling {@link
     * #init(PRing)}.
     */
    protected PGroup() {}

    /**
     * Creates a group with the given associated ring.
     *
     * @param pRing Ring associated with this instance.
     */
    protected PGroup(PRing pRing) {
        init(pRing);
    }

    /**
     * Initializes this instance with the given ring.
     *
     * @param pRing Ring associated with this instance.
     */
    protected void init(PRing pRing) {
        this.pRing = pRing;
    }

    /**
     * Returns the order of every non-unit element in this group.
     *
     * @return Order of every non-unit element in this group.
     */
    public LargeInteger getElementOrder() {
        return pRing.getPField().getOrder();
    }

    /**
     * Sets the breakpoint at which exponentiation of
     * <code>PGroupElement[]</code> is threaded.
     *
     * @param expThreadThreshold New threshold.
     */
    synchronized public void setExpThreadThreshold(int expThreadThreshold) {
        this.expThreadThreshold = expThreadThreshold;
    }

    /**
     * Sets the breakpoint at which multiplication (and other
     * operations with similar cost) of <code>PGroupElement[]</code> is
     * threaded.
     *
     * @param mulThreadThreshold New threshold.
     */
    synchronized public void setMulThreadThreshold(int mulThreadThreshold) {
        this.mulThreadThreshold = mulThreadThreshold;
    }

    /**
     * Outputs a human readable description of the group. This should
     * only be used for debugging.
     *
     * @return Human readable description of the group.
     */
    public abstract String toString();

    /**
     * Returns the standard generator of the group, where "generator"
     * means a generator under the action of the associated ring,
     * (which is not necessarily a field).
     *
     * @return Standard generator.
     */
    public abstract PGroupElement getg();

    /**
     * Returns the unit in the group.
     *
     * @return Unit in the group.
     */
    public abstract PGroupElement getONE();

    /**
     * Returns the fixed number of bytes used to injectively map an
     * instance to a <code>byte[]</code>.
     *
     * @return Fixed byte length.
     */
    public abstract int getByteLength();

    /**
     * Returns the maximal number of bytes that can be encoded in an
     * element of the group.
     *
     * @return Maximal number of bytes that can be encoded in a group
     * element.
     */
    public abstract int getEncodeLength();

    /**
     * Creates a {@link PGroupElement} instance from the given
     * representation.
     *
     * @param btr Representation of an instance.
     * @return Group element represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * an element of this group.
     */
    public abstract PGroupElement toElement(ByteTreeReader btr)
            throws ArithmFormatException;

    /**
     * Encodes a part of an arbitrary <code>byte[]</code> as an
     * element in the group. The input is truncated if it is longer
     * than {@link #getEncodeLength()} bytes. The resulting bytes can
     * be recovered using {@link PGroupElement#decode(byte[],int)}.
     *
     * @param bytes Bytes to be encoded.
     * @param startIndex Starting index.
     * @param length Length of bytes to be encoded.
     * @return Group element encoding the input.
     */
    public abstract PGroupElement encode(byte[] bytes,
                                         int startIndex,
                                         int length);

    /**
     * Returns a randomly chosen element.
     *
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Random element of this group.
     */
    public abstract PGroupElement randomElement(RandomSource rs, int statDist);

    /**
     * Creates an instance containing the given elements.
     *
     * @param elements Elements to be contained in the array.
     * @return Array containing the input group elements.
     */
    public abstract PGroupElementArray toElementArray(PGroupElement[] elements);

    /**
     * Returns the concatenation of the inputs.
     *
     * @param arrays Arrays to be concatenated.
     */
    public abstract PGroupElementArray
        toElementArray(PGroupElementArray ... arrays);

    /**
     * Recovers an array of group elements from the given
     * representation.
     *
     * @param size Expected number of elements in array. If this is
     * set to zero, then an array of any size is accepted.
     * @param btr Representation of array.
     * @return Array of group elements.
     *
     * @throws ArithmFormatException If the input does not represent
     * an array of group elements of the given size.
     */
    public abstract PGroupElementArray toElementArray(int size,
                                                      ByteTreeReader btr)
        throws ArithmFormatException;

    /**
     * Creates an instance of the given size filled with copies of the
     * given element.
     *
     * @param size Number of elements in resulting array.
     * @param element Element to use.
     * @return Array of copied elements.
     */
    public abstract PGroupElementArray toElementArray(int size,
                                                      PGroupElement element);

    /**
     * Generate a random array of group elements. Note that it must be
     * infeasible to compute any non-trivial relation between the
     * resulting elements. Thus, simply raising the standard generator
     * to a random power is not acceptable.
     *
     * @param size Number of elements to generate.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Array of random group elements.
     */
    public abstract PGroupElementArray randomElementArray(int size,
                                                          RandomSource rs,
                                                          int statDist);

    /**
     * Returns true if and only if the input group equals this group.
     *
     * @param obj Instance to compare with.
     * @return True if and only if the input group equals this group.
     */
    public abstract boolean equals(Object obj);


    // ############ Implemented in terms of the above. ###########

    /**
     * Returns the product of all elements in <code>bases</code> to
     * the respective powers in <code>exponents</code>. This is
     * naively implemented, but with threading.
     *
     * @param bases Bases to be exponentiated.
     * @param exponents Powers to be taken.
     * @return Product of all bases to the powers of the given exponents.
     */
    public PGroupElement naiveExpProd(final PGroupElement[] bases,
                                      final PRingElement[] exponents) {
        if (bases.length != exponents.length) {
            throw new ArithmError("Different lengths of inputs!");
        }

        final List<PGroupElement> parts =
            Collections.synchronizedList(new LinkedList<PGroupElement>());

        ArrayWorker worker =
            new ArrayWorker(bases.length) {
                public void work(int start, int end) {
                    PGroupElement part = getONE();

                    for (int i = start; i < end; i++) {
                        part = part.mul(bases[i].exp(exponents[i]));
                    }

                    parts.add(part);
                }
            };
        worker.work(expThreadThreshold);

        PGroupElement res = getONE();
        for (PGroupElement part : parts) {
            res = res.mul(part);
        }
        return res;
    }

    /**
     * Returns the product of all elements in <code>bases</code> to
     * the respective powers in <code>exponents</code>. This uses
     * simultaneous exponentiation and threading.
     *
     * @param bases Bases to be exponentiated.
     * @param exponents Powers to be taken.
     * @return Product of all bases to the powers of the given exponents.
     */
    public PGroupElement expProd(final PGroupElement[] bases,
                                 final PRingElement[] exponents) {
	if (bases.length != exponents.length) {
	    throw new ArithmError("Different lengths of inputs!");
	}

        // Convert exponents to integers and compute the maximal bit
        // length of the exponents.
        final LargeInteger[] integers = new LargeInteger[exponents.length];
        int tmpBitLength = 0;

        for (int i = 0; i < exponents.length; i++) {

            integers[i] = ((PFieldElement)exponents[i]).toLargeInteger();
            tmpBitLength = Math.max(integers[i].bitLength(), tmpBitLength);
        }

        final int bitLength = tmpBitLength;
	final int maxWidth = PGroupSimExpTab.optimalWidth(bitLength);

        // We need to collect partial results from multiple threads in
        // a thread-safe way.
	final List<PGroupElement> parts =
	    Collections.synchronizedList(new LinkedList<PGroupElement>());

	ArrayWorker worker =
	    new ArrayWorker(bases.length) {

		public void work(int start, int end) {

		    PGroupElement part = getONE();

		    int offset = start;

		    // Splits parts recieved from ArrayWorker and run through
		    // these smaller parts.
		    while (offset < end) {

                        int width = Math.min(maxWidth, end - offset);

                        // Compute table for simultaneous
                        // exponentiation.
                        PGroupSimExpTab tab =
                            new PGroupSimExpTab(bases, offset, width);

                        // Perform simultaneous exponentiation.
                        PGroupElement batch =
                            tab.expProd(integers, offset, bitLength);

                        part = part.mul(batch);

                        offset += width;
                    }
		    parts.add(part);
		}
	    };

	worker.work(expThreadThreshold);

	PGroupElement res = getONE();
	for (PGroupElement part : parts) {
	    res = res.mul(part);
	}
	return res;
    }

    /**
     * Computes the element-wise product of the inputs.
     *
     * @param op1 Array of group elements.
     * @param op2 Array of group elements.
     * @return Element-wise product of the inputs.
     */
    public PGroupElement[] mul(final PGroupElement[] op1,
                               final PGroupElement[] op2) {
        if (op1.length != op2.length) {
            throw new ArithmError("Different lengths of inputs!");
        }
        final PGroupElement[] res = new PGroupElement[op1.length];

        ArrayWorker worker =
            new ArrayWorker(res.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = op1[i].mul(op2[i]);
                    }
                }
            };
        worker.work(mulThreadThreshold);
        return res;
    }

    /**
     * Returns the element-wise inverse of the input array.
     *
     * @param elements Elements to be inverted.
     * @return Array of results.
     */
    public PGroupElement[] inv(final PGroupElement[] elements) {
        final PGroupElement[] res = new PGroupElement[elements.length];

        ArrayWorker worker =
            new ArrayWorker(res.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = elements[i].inv();
                    }
                }
            };
        worker.work(mulThreadThreshold);
        return res;
    }

    /**
     * Returns the element-wise division of the elements in the two
     * arrays.
     *
     * @param numerators Numerators.
     * @param denominators Denominators.
     * @return Array of results.
     */
    public PGroupElement[] div(final PGroupElement[] numerators,
                               final PGroupElement[] denominators) {
        if (numerators.length != denominators.length) {
            throw new ArithmError("Different lengths!");
        }
        final PGroupElement[] res = new PGroupElement[numerators.length];

        ArrayWorker worker =
            new ArrayWorker(res.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = numerators[i].div(denominators[i]);
                    }
                }
            };
        worker.work(mulThreadThreshold);
        return res;
    }

    /**
     * Returns all elements in <code>bases</code> to the respective
     * powers in <code>exponents</code>.
     *
     * @param bases Bases to be exponentiated.
     * @param exponents Powers to be taken.
     * @return All bases to the powers of the given exponents.
     */
    public PGroupElement[] exp(final PGroupElement[] bases,
                               final PRingElement[] exponents) {
        if (bases.length != exponents.length) {
            throw new ArithmError("Different lengths!");
        }
        final PGroupElement[] res = new PGroupElement[bases.length];

        ArrayWorker worker =
            new ArrayWorker(res.length) {
                public void work(int start, int end) {

                    for (int i = start; i < end; i++) {
                        res[i] = bases[i].exp(exponents[i]);
                    }
                }
            };
        worker.work(expThreadThreshold);
        return res;
    }

    /**
     * Returns elements in <code>bases</code> to the power of
     * <code>exponent</code>.
     *
     * @param bases Bases to be exponentiated.
     * @param exponent Power to be taken.
     * @return All bases to the power of the given exponent.
     */
    public PGroupElement[] exp(final PGroupElement[] bases,
                               final PRingElement exponent) {
        final PGroupElement[] res = new PGroupElement[bases.length];

        ArrayWorker worker =
            new ArrayWorker(res.length) {
                public void work(int start, int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = bases[i].exp(exponent);
                    }
                }
            };
        worker.work(expThreadThreshold);
        return res;
    }

    /**
     * Returns the product of the input elements.
     *
     * @param elements Elements to be multiplied.
     * @return Product of input elements.
     */
    public PGroupElement prod(final PGroupElement[] elements) {
        PGroupElement res = getONE();

        final List<PGroupElement> parts =
            Collections.synchronizedList(new LinkedList<PGroupElement>());

        ArrayWorker worker =
            new ArrayWorker(elements.length) {
                public void work(int start, int end) {
                    PGroupElement part = getONE();

                    for (int i = start; i < end; i++) {
                        part = part.mul(elements[i]);
                    }

                    parts.add(part);
                }
            };
        worker.work(mulThreadThreshold);

        for (PGroupElement part : parts) {
            res = res.mul(part);
        }
        return res;
    }

    /**
     * Tests if the elements in the two inputs are equal.
     *
     * @param a Array of elements.
     * @param b Array of elements.
     * @return <code>true</code> or <code>false</code> depending on if
     * the elements in the two arrays are equal or not.
     */
    public boolean equals(PGroupElement[] a, PGroupElement[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Encodes an arbitrary <code>byte[]</code> as an array of
     * elements in the group. The number of elements is chosen such
     * that all bits can be encoded and if needed the last encoded
     * chunk is padded with zeros before encoding. The resulting array
     * can then be decoded again using {@link
     * #decode(PGroupElement[])}.
     *
     * @param bytes Bytes to be encoded.
     * @param rs Source of randomness.
     * @return Array of group elements encoding the input.
     */
    public PGroupElement[] encode(byte[] bytes, RandomSource rs) {
        int encodeLength = getEncodeLength();
        int noPGroupElements = (bytes.length + encodeLength - 1) / encodeLength;

        final PGroupElement[] res = new PGroupElement[noPGroupElements];

        int i = 0;
        int j = 0;
        for (i = 0; i < noPGroupElements - 1; i++) {
            res[i] = encode(bytes, j, encodeLength);
            j += encodeLength;
        }
        res[i] = encode(bytes, j, bytes.length - j);

        return res;
    }

    /**
     * Recovers a <code>byte[]</code> from its encoding as an array of
     * elements in the group, i.e., the output of {@link
     * #encode(byte[])}.
     *
     * @param elements Elements to be decoded.
     * @return Decoded data.
     */
    public byte[] decode(PGroupElement[] elements) {
        byte[] tmp = new byte[elements.length * getEncodeLength()];

        int j = 0;
        for (int i = 0; i < elements.length; i++) {
            j += elements[i].decode(tmp, j);
        }
        return Arrays.copyOfRange(tmp, 0, j);
    }

    /**
     * Creates a {@link PGroupElement} instance from the given
     * representation.
     *
     * @param btr A representation of an instance.
     * @return Group element represented by the input.
     *
     * @throws ArithmError If the input does not represent a group
     * element of this group.
     */
    public PGroupElement unsafeToElement(ByteTreeReader btr) {
        try {
            return toElement(btr);
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Fatal error!", afe);
        }
    }

    /**
     * Returns a byte tree representation of the input.
     *
     * @param array Array to represent.
     * @return Representation of the input array.
     */
    public ByteTreeBasic toByteTree(PGroupElement[] array) {
        ByteTreeBasic[] byteTrees = new ByteTreeBasic[array.length];
        for (int i = 0; i < byteTrees.length; i++) {
            byteTrees[i] = array[i].toByteTree();
        }
        return new ByteTreeContainer(byteTrees);
    }

    /**
     * Recovers a <code>PGroupElement[]</code> from the given
     * representation.
     *
     * @param maxSize Maximal number of elements read.
     * @param btr Representation of an array of elements.
     * @return Array of group elements represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * a <code>PGroupElement[]</code>.
     */
    public PGroupElement[] toElements(int maxSize, ByteTreeReader btr)
    throws ArithmFormatException {
        try {
            if (btr.getRemaining() > maxSize) {
                throw new ArithmFormatException("Too many elements!");
            }
            PGroupElement[] res = new PGroupElement[btr.getRemaining()];
            for (int i = 0; i < res.length; i++) {
                res[i] = toElement(btr.getNextChild());
            }
            return res;
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Recovers an array of group elements from the given
     * representation.
     *
     * @param length Number of elements in array.
     * @param btr Representation of array.
     * @return Array of group elements.
     *
     * @throws ArithmError If the input does not represent an array of
     * group elements.
     */
    public PGroupElementArray
        unsafeToElementArray(int length, ByteTreeReader btr) {
        String s = "Failed to read element array!";
        try {
            return toElementArray(length, btr);
        } catch (ArithmFormatException afe) {
            throw new ArithmError(s, afe);
        }
    }

    /**
     * Returns a representation of an array of arrays of group
     * elements.
     *
     * @param pea Representation of array of arrays of group elements.
     * @return Representation of array of arrays of group elements.
     */
    public ByteTreeContainer toByteTree(PGroupElementArray[] pea) {
        ByteTreeBasic[] btb = new ByteTreeBasic[pea.length];

        for (int i = 0; i < pea.length; i++) {
            btb[i] = pea[i].toByteTree();
        }
        return new ByteTreeContainer(btb);
    }

    /**
     * Verifies that all instances are associated to the same instance
     * <code>PGroup</code>.
     *
     * @param els Group associated instances to be tested.
     * @return true or false depending on if all elements are
     * compatible or not.
     */
    public static boolean compatible(PGroupAssociated ... els) {
            if (els.length == 0) {
                return true;
            } else {
                PGroup pGroup = els[0].getPGroup();
                for (int i = 1; i < els.length; i++) {
                    if (!els[i].getPGroup().equals(pGroup)) {
                        return false;
                    }
                }
                return true;
            }
    }

    /**
     * Verifies that all <code>PGroupAssociated</code> inputs are
     * associated with the same <code>PGroup</code> instance, and that
     * the <code>PRingAssociated</code> instance is associated with
     * the <code>PRing</code> instance of this group instance.
     *
     * @param x Ring associated instance to be tested.
     * @param els Group associated instances to be tested.
     * @return true or false depending on if all elements are
     * compatible or not.
     */
    public static boolean compatible(PRingAssociated x,
                                         PGroupAssociated ... els) {
            return compatible(els)
                && (els.length == 0
                    || x.getPRing().equals(els[0].getPGroup().getPRing()));
    }


    // Documented in PRingAssociated.java

    public PRing getPRing() {
        return pRing;
    }
}
