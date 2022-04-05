
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
import java.util.*;

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.util.*;

/**
 * Huge array of integers stored on file.
 *
 * @author Douglas Wikstrom
 */
public class LargeIntegerArrayF extends LargeIntegerArray {

    /**
     * Number of elements in this instance.
     */
    protected int size;

    /**
     * File storing the data of this instance.
     */
    protected File file;

    /**
     * Number of elements processed in each batch.
     */
    public int batchSize;

    /**
     * Expected number of bytes in arrays representing elements. This
     * is zero if there is no expected byte length.
     */
    public int expectedByteLength;

    /**
     * Default number of integers processed in each batch.
     */
    public final static int DEFAULT_BATCH_SIZE = 100000;

    /**
     * Creates an empty instance. It is the responsibility of the
     * programmer to fill this instance with data.
     *
     * @param size Number of elements in array.
     */
    protected LargeIntegerArrayF(int size) {
        this.size = size;
        this.file = TempFile.getFile();
        this.batchSize = DEFAULT_BATCH_SIZE;
    }

    /**
     * Returns a writer that allows writing {@link #size} byte trees.
     *
     * @return Writer for the contents of this instance.
     */
    protected ByteTreeWriterF getWriter() {
        try {
            return new ByteTreeWriterF(size, file);
        } catch (FileNotFoundException fnfe) {
            throw new ArithmError("Unable to create writer!", fnfe);
        } catch (IOException ioe) {
            throw new ArithmError("Unable to create writer!", ioe);
        }
    }

    /**
     * Returns a reader.
     *
     * @return Reader for the contents of this instance.
     */
    protected ByteTreeReader getReader() {
        return (new ByteTreeF(file)).getByteTreeReader();
    }

    /**
     * Constructs an instance with the given integers.
     *
     * @param integers Integers of this instance.
     */
    LargeIntegerArrayF(LargeInteger[] integers) {
        this(integers.length);

        ByteTreeWriterF btw = getWriter();
        btw.unsafeWrite(integers);
        btw.close();
    }

    /**
     * Returns the total number of integers in the given arrays.
     *
     * @param arrays Input arrays.
     * @return Total number of integers.
     */
    protected static int totalSize(LargeIntegerArray ... arrays) {
        int size = 0;
        for (int i = 0; i < arrays.length; i++) {
            size += arrays[i].size();
        }
        return size;
    }

    /**
     * Constructs the concatenation of the given inputs.
     *
     * @param arrays Source arrays.
     * @return Concatenation of the input arrays.
     */
    LargeIntegerArrayF(LargeIntegerArray ... arrays) {
        this(totalSize(arrays));

        ByteTreeWriterF btw = getWriter();

        for (int i = 0; i < arrays.length; i++) {

            ByteTreeReader btr = ((LargeIntegerArrayF)arrays[i]).getReader();

            while (btr.getRemaining() > 0) {

                LargeInteger[] integers = readBatch(btr);
                btw.unsafeWrite(integers);

            }
            btr.close();
        }
        btw.close();
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of integers to generate.
     * @param bitLength Number of bits in each integer.
     * @param randomSource Source of random bits used to initialize
     * the array.
     */
    LargeIntegerArrayF(int size, int bitLength, RandomSource randomSource) {
        this(size);

        ByteTreeWriterF btw = getWriter();
        for (int i = 0; i < size; i++) {
            LargeInteger li = new LargeInteger(bitLength, randomSource);
            btw.unsafeWrite(li);
        }
        btw.close();
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of integers to generate.
     * @param modulus Modulus.
     * @param statDist Decides the statistical distance from the
     * @param randomSource Source of random bits used to initialize
     * the array.
     * @return Array of random integers.
     */
    LargeIntegerArrayF(int size, LargeInteger modulus, int statDist,
                       RandomSource randomSource) {
        this(size);
        ByteTreeWriterF btw = getWriter();
        for (int i = 0; i < size; i++) {
            LargeInteger li = new LargeInteger(modulus, statDist, randomSource);
            btw.unsafeWrite(li);
        }
        btw.close();
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of elements in this instance.
     * @param value Value used to initialize this instance.
     */
    LargeIntegerArrayF(int size, LargeInteger value) {
        this(size);

        ByteTreeWriterF btw = getWriter();
        ByteTreeBasic bt = value.toByteTree();
        for (int i = 0; i < size; i++) {
            btw.unsafeWrite(bt);
        }
        btw.close();
    }

    /**
     * Returns the actual size or expected size depending on if the
     * latter is zero or not.
     *
     * @return Actual size or expected size depending on if the latter
     * is zero or not.
     */
    protected static int zeroMapped(int expectedSize, int actualSize) {
        if (expectedSize == 0) {
            return actualSize;
        } else {
            return expectedSize;
        }
    }

    /**
     * Returns the array of integers represented by the input. This
     * constructor requires that each integer falls into the given
     * interval, but also that the representation of each integer is
     * of equal size to the byte tree representation of the upper
     * bound.
     *
     * @param size Expected number of elements in array.
     * @param btr Should contain a representation of an array of
     * integers.
     * @param lb Non-negative inclusive lower bound for integers.
     * @param ub Positive exclusive upper bound for integers.
     *
     * @throws ArithmFormatException If the input does not represent a
     * an array of integers satisfying the given bounds.
     */
    LargeIntegerArrayF(int size, ByteTreeReader btr,
                       LargeInteger lb, LargeInteger ub)
        throws IOException, EIOException, ArithmFormatException {

        // It is important to not mix up size and this.size, since the
        // former may be zero when the latter is not.
        this(zeroMapped(size, btr.getRemaining()));

        if (size != 0 && btr.getRemaining() != size) {
            throw new ArithmFormatException("Unexpected number of integers!");
        }

        int expectedByteLength = ub.toByteArray().length;

        ByteTreeWriterF btw = getWriter();

        // We need this.size here and not size.
        for (int i = 0; i < this.size; i++) {

            LargeInteger integer =
                new LargeInteger(expectedByteLength, btr.getNextChild(), null);

            if (lb.compareTo(integer) <= 0 && integer.compareTo(ub) < 0) {

                btw.unsafeWrite(integer);

            } else {

                btw.close();
                throw new ArithmFormatException("Integer outside interval!");

            }
        }
        btw.close();
    }

    /**
     * Reads a batch of large integers. The size of the batch is the
     * minimum of {@link #batchSize} and the remaining number of
     * integers in the reader.
     *
     * @param btr Source of integers.
     * @return Array of integers.
     */
    protected LargeInteger[] readBatch(ByteTreeReader btr) {
        return readBatch(Math.min(batchSize, btr.getRemaining()), btr);
    }

    /**
     * Reads the given number of integers from the reader.
     *
     * @param len Number integers to read.
     * @param btr Representation of the integers.
     * @return Array of integers.
     */
    protected static LargeInteger[] readBatch(int len, ByteTreeReader btr) {
        try {

            LargeInteger[] res = new LargeInteger[len];
            for (int i = 0; i < len; i++) {
                res[i] = new LargeInteger(btr.getNextChild());
            }
            return res;
        } catch (EIOException eioe) {
            throw new ArithmError("Unable to read data!");
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Unable to read data!");
        }
    }

    // Documented in LargeIntegerArray.java

    public LargeIntegerIterator getIterator() {
	return new LargeIntegerIteratorF(file);
    }

    public LargeIntegerArray modInv(LargeInteger modulus)
    throws ArithmException {

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            LargeInteger[] integers = readBatch(btr);
            btw.unsafeWrite(LargeInteger.modInv(integers, modulus));
        }
        btr.close();
        btw.close();

        return res;
    }

    public LargeIntegerArray copyOfRange(int startIndex, int endIndex) {

        int resSize = endIndex - startIndex;

        LargeIntegerArrayF res = new LargeIntegerArrayF(resSize);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr = getReader();

        try {
            btr.skipChildren(startIndex);
        } catch (EIOException eioe) {
            throw new ArithmError("Unable to skip content!", eioe);
        }

        while (resSize > 0) {

            int len = Math.min(batchSize, resSize);
            LargeInteger[] integers = readBatch(len, btr);
            btw.unsafeWrite(integers);
            resSize -= integers.length;
        }
        btr.close();
        btw.close();

        return res;
    }

    public LargeInteger[] integers() {
        ByteTreeReader btr = getReader();
        LargeInteger[] res = readBatch(size, btr);
        btr.close();
        return res;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LargeIntegerArrayF)) {
            return false;
        }
        return compareTo((LargeIntegerArrayF)obj) == 0;
    }

    public LargeIntegerArray extract(boolean[] valid) {
        if (size != valid.length) {
            throw new ArithmError("Different lengths!");
        }

        int total = 0;
        for (int i = 0; i < valid.length; i++) {
            if (valid[i]) {
                total++;
            }
        }

        LargeIntegerArrayF res = new LargeIntegerArrayF(total);
        ByteTreeWriterF btw = res.getWriter();

        LargeIntegerIterator lii = getIterator();

        for (int i = 0; i < valid.length; i++) {
            LargeInteger integer = lii.next();
            if (valid[i]) {
                btw.unsafeWrite(integer);
            }
        }
        lii.close();
        btw.close();

        return res;
    }

    public LargeIntegerArray permute(Permutation permutation) {

        if (size != permutation.size()) {
            throw new ArithmError("Wrong size!");
        }

        LargeIntegerIterator lii = getIterator();

        File fileIn = TempFile.getFile();
        File fileOut = TempFile.getFile();

        // Write newline separated list of hex-coded bytetrees.
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileOutputStream(fileIn));

            for (int i = 0; i < size; i++) {

                byte[] bytes = lii.next().toByteTree().toByteArray();
                String hex = Hex.toHexString(bytes);
                pw.format("%08x %s", permutation.map(i), hex);

                if (lii.hasNext()) {
                    pw.print("\n");
                }
            }
        } catch (FileNotFoundException fnfe) {
            throw new ArithmError("Unable to permute array!", fnfe);
        } finally {
            ExtIO.strictClose(pw);
            lii.close();
        }

        // Sort with respect to the mapping.
        try {
            ExtIO.sort(fileIn, fileOut);
        } catch (InterruptedException ie) {
            throw new ArithmError("Unable to permute array!", ie);
        } catch (IOException ioe) {
            throw new ArithmError("Unable to permute array!", ioe);
        } finally {
            fileIn.delete();
        }

        // Prepare the result array.
        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();

        // Read newline separated list of hex-coded bytetrees.
        BufferedReader br = null;
        try {
            InputStreamReader isr =
                new InputStreamReader(new FileInputStream(fileOut));
            br = new BufferedReader(isr);

            for (int i = 0; i < size; i++) {

                String line = br.readLine();
                String hex = line.split(" ")[1];

                byte[] bytes = Hex.toByteArray(hex);
                ByteTree bt = new ByteTree(bytes, null);
                btw.write(bt);
            }
        } catch (EIOException eioe) {
            throw new ArithmError("Unable to permute array!", eioe);
        } catch (FileNotFoundException fnfe) {
            throw new ArithmError("Unable to permute array!", fnfe);
        } catch (IOException ioe) {
            throw new ArithmError("Unable to permute array!", ioe);
        } finally {
            btw.close();
            ExtIO.strictClose(br);
            fileOut.delete();
        }
        return res;
    }

    public LargeIntegerArray modAdd(LargeIntegerArray termsArray,
                                    LargeInteger modulus) {
        if (size != termsArray.size()) {
            throw new ArithmError("Different lengths!");
        }

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr1 = getReader();
        ByteTreeReader btr2 = ((LargeIntegerArrayF)termsArray).getReader();

        while (btr1.getRemaining() > 0) {

            LargeInteger[] integers1 = readBatch(btr1);
            LargeInteger[] integers2 = readBatch(btr2);

            btw.unsafeWrite(LargeInteger.modAdd(integers1, integers2, modulus));
        }
        btr2.close();
        btr1.close();
        btw.close();

        return res;
    }

    public LargeIntegerArray modNeg(LargeInteger modulus) {

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            LargeInteger[] integers = readBatch(btr);
            btw.unsafeWrite(LargeInteger.modNeg(integers, modulus));
        }
        btr.close();
        btw.close();

        return res;
    }

    public LargeIntegerArray modMul(LargeIntegerArray factorsArray,
                                    LargeInteger modulus) {
        if (size != factorsArray.size()) {
            throw new ArithmError("Different lengths!");
        }

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr1 = getReader();
        ByteTreeReader btr2 = ((LargeIntegerArrayF)factorsArray).getReader();

        while (btr1.getRemaining() > 0) {

            LargeInteger[] integers1 = readBatch(btr1);
            LargeInteger[] integers2 = readBatch(btr2);

            btw.unsafeWrite(LargeInteger.modMul(integers1, integers2, modulus));
        }
        btr2.close();
        btr1.close();
        btw.close();

        return res;
    }

    public LargeIntegerArray modMul(LargeInteger scalar, LargeInteger modulus) {

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            LargeInteger[] integers = readBatch(btr);
            btw.unsafeWrite(LargeInteger.modMul(integers, scalar, modulus));
        }
        btr.close();
        btw.close();

        return res;
    }

    public LargeIntegerArray modPow(LargeIntegerArray exponentsArray,
                                    LargeInteger modulus) {
        if (size != exponentsArray.size()) {
            throw new ArithmError("Different lengths!");
        }

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr1 = getReader();
        ByteTreeReader btr2 = ((LargeIntegerArrayF)exponentsArray).getReader();

        while (btr1.getRemaining() > 0) {

            LargeInteger[] integers1 = readBatch(btr1);
            LargeInteger[] integers2 = readBatch(btr2);

            btw.unsafeWrite(LargeInteger.modPow(integers1, integers2, modulus));
        }
        btr2.close();
        btr1.close();
        btw.close();

        return res;
    }

    public LargeIntegerArray modPow(LargeInteger exponent,
                                    LargeInteger modulus) {

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            LargeInteger[] integers = readBatch(btr);
            btw.unsafeWrite(LargeInteger.modPow(integers, exponent, modulus));

        }
        btr.close();
        btw.close();

        return res;
    }

    public LargeIntegerArray modPowVariant(LargeInteger basis,
					   LargeInteger modulus) {

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            LargeInteger[] integers = readBatch(btr);
            btw.unsafeWrite(basis.modPow(integers, modulus));
        }
        btr.close();
        btw.close();

        return res;
    }

    public LargeInteger modPowProd(LargeIntegerArray exponentsArray,
                                   LargeInteger modulus) {
        if (size != exponentsArray.size()) {
            throw new ArithmError("Different lengths!");
        }

        ByteTreeReader btr1 = getReader();
        ByteTreeReader btr2 = ((LargeIntegerArrayF)exponentsArray).getReader();

        LargeInteger res = LargeInteger.ONE;
        while (btr1.getRemaining() > 0) {

            LargeInteger[] integers1 = readBatch(btr1);
            LargeInteger[] integers2 = readBatch(btr2);

            res = res.mul(LargeInteger.modPowProd(integers1,
                                                  integers2,
                                                  modulus)).mod(modulus);
        }
        btr2.close();
        btr1.close();

        return res;
    }

    public LargeIntegerArray modProds(LargeInteger modulus) {

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr = getReader();

        LargeInteger agg = LargeInteger.ONE;
        while (btr.getRemaining() > 0) {

            LargeInteger[] integers = readBatch(btr);
            LargeInteger[] tmp = LargeInteger.modProds(agg, integers, modulus);
            agg = tmp[tmp.length - 1];
            btw.unsafeWrite(tmp);
        }
        btr.close();
        btw.close();

        return res;
    }

    public LargeInteger get(int index) {
        try {
            ByteTreeReader btr = getReader();
            btr.skipChildren(index);
            LargeInteger res = new LargeInteger(btr.getNextChild());
            btr.close();
            return res;
        } catch (EIOException eioe) {
            throw new ArithmError("Unable to read from array!", eioe);
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Unable to read from array!", afe);
        }

    }

    public LargeIntegerArray shiftPush(LargeInteger integer) {

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr = getReader();

        btw.unsafeWrite(integer);

        while (btr.getRemaining() > 1) {

            int len = Math.min(batchSize, btr.getRemaining() - 1);
            LargeInteger[] tmp = readBatch(len, btr);
            btw.unsafeWrite(tmp);
        }
        btr.close();
        btw.close();

        return res;
    }

    public Pair<LargeIntegerArray,LargeInteger>
        modRecLin(LargeIntegerArray array, LargeInteger modulus) {

        if (size != array.size()) {
            throw new ArithmError("Different lengths!");
        }

        LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        ByteTreeWriterF btw = res.getWriter();
        ByteTreeReader btr1 = getReader();
        ByteTreeReader btr2 = ((LargeIntegerArrayF)array).getReader();

        LargeInteger agg = LargeInteger.ZERO;
        while (btr1.getRemaining() > 0) {

            LargeInteger[] integers1 = readBatch(btr1);
            LargeInteger[] integers2 = readBatch(btr2);

            LargeInteger[] tmp = new LargeInteger[integers1.length];
            for (int i = 0; i < tmp.length; i++) {
                agg = agg.mul(integers2[i]).add(integers1[i]).mod(modulus);
                tmp[i] = agg;
            }

            btw.unsafeWrite(tmp);
        }
        btr2.close();
        btr1.close();
        btw.close();

        return new Pair<LargeIntegerArray, LargeInteger>(res, agg);
    }

    public ByteTreeBasic toByteTree() {
        return new ByteTreeF(file);
    }

    public ByteTreeBasic toByteTree(int expectedByteLength) {

        if (this.expectedByteLength == expectedByteLength) {
            return new ByteTreeF(file);
        }

        if (this.expectedByteLength != 0) {
            throw new ArithmError("Attempting to change expected byte length!");
        }

        this.expectedByteLength = expectedByteLength;

        File btFile = TempFile.getFile();

        ByteTreeReader btr = getReader();
        ByteTreeWriterF btw;
        try {
            btw = new ByteTreeWriterF(size, btFile);
        } catch (IOException ioe) {
            throw new ArithmError("Unable to create temporary file!");
        }

        while (btr.getRemaining() > 0) {

            LargeInteger[] tmp = readBatch(btr);

            for (int i = 0; i < tmp.length; i++) {
                btw.unsafeWrite(tmp[i].toByteTree(expectedByteLength));
            }
        }
        btw.close();
        btr.close();

        file.delete();
        btFile.renameTo(file);

        return new ByteTreeF(file);
    }

    public boolean quadraticResidues(LargeInteger prime) {

        ByteTreeReader btr = getReader();

        boolean res = true;
        while (btr.getRemaining() > 0 && res) {

            LargeInteger[] integers = readBatch(btr);
            res = LargeInteger.quadraticResidues(integers, prime);
        }
        btr.close();

        return res;
    }

    public int size() {
        return size;
    }

    public void free() {
        file.delete();
    }
}
