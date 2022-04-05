
// FORK copyright
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

package mixnet.eio;

import java.io.*;
import java.lang.reflect.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.ui.gen.*;

/**
 * This class provides a uniform way of marshalling an instance of a
 * class that implements {@link Marshalizable} into a {@link
 * ByteTreeBasic}. It also provides a uniform way of recovering the
 * converted instance from such a representation using a {@link
 * ByteTreeReader}.
 *
 * <p>
 *
 * This is implemented using the Java reflection API. Using this API
 * it is straightforward to provide the first functionality. We simply
 * embed the class name of an instance as part of a slightly larger
 * representation.
 *
 * <p>
 *
 * The second functionality is also straightforward to implement if
 * one is willing to explicitly verify the class of an instance and
 * cast the output from the conversion method to the appropriate type
 * every time an instance is recovered. The drawback of this naive
 * approach is that there is no type safety.
 *
 * <p>
 *
 * We instead encapsulate all needed explicit type casts in
 * automatically generated methods of the form:<br>
 *
 * <code>public static TYPE unmarshal_TYPE({@link ByteTreeReader} btr)</code><br>
 * <code>public static TYPE unmarshalAux_TYPE({@link ByteTreeReader} btr, {@link RandomSource} rs, int statDist)</code><br>
 *
 * for some type TYPE. In other words, these methods can be used and
 * will be generated if needed. Thus, this class is partly
 * automatically generated. Audits can then be performed on the
 * generated Java source which is type-safe.
 *
 * @author Douglas Wikstrom
 */
public class Marshalizer {

    /**
     * Maximal number of characters in a class name.
     */
    final static int MAX_CLASSNAME_LENGTH = 2048;

    /**
     * Reads a <code>ByteTreeBasic</code> representation output by
     * {@link #marshal(Marshalizable)} and returns the corresponding
     * instance. The verification of the input may be probabilistic,
     * but must in that case guarantee that correctness holds with
     * probability <i>2<sup>-<code>certainty</code></sup></i>.
     *
     * @param btr An instance to be converted.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Instance recovered from the input representation.
     *
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    @SuppressWarnings("unchecked")
    protected static Object unmarshalAux(ByteTreeReader btr,
                                         RandomSource rs,
                                         int certainty)
        throws EIOException {

        String className = "ingen";
        try {

            ByteTreeReader cnbtr = btr.getNextChild();
            if (cnbtr.getRemaining() > MAX_CLASSNAME_LENGTH) {
                throw new EIOException("Too long classname!");
            }
            className = cnbtr.readString();

            Class klass = Class.forName(className);

            try {
                Method method = klass.getMethod("newInstance",
                                                ByteTreeReader.class,
                                                RandomSource.class,
                                                java.lang.Integer.TYPE);

                return method.invoke(null, btr.getNextChild(),
                                     rs, certainty);
            } catch (NoSuchMethodException nsme) {
                Method method =
                    klass.getMethod("newInstance", ByteTreeReader.class);
                return method.invoke(null, btr.getNextChild());
            }
        } catch (InvocationTargetException ite) {
            throw new EIOException("Unable to interpret, unknown target!", ite);
        } catch (IllegalAccessException iae) {
            throw new EIOException("Unable to interpret, illegal access!", iae);
        } catch (ClassNotFoundException cnfe) {
            throw new EIOException("Unable to interpret, unknown class (" +
                                   className + ")!", cnfe);
        } catch (NoSuchMethodException nsme) {
            throw new EIOException("Unable to interpret, no method!", nsme);
        }
    }

    /**
     * Reads a <code>ByteTreeBasic</code> representation output by
     * {@link #marshal(Marshalizable)} and returns the corresponding
     * instance.
     *
     * @param btr Representation of instance.
     * @return Instance recovered from the input representation.
     *
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    @SuppressWarnings("unchecked")
    protected static Object unmarshal(ByteTreeReader btr)
        throws EIOException {
        String className = "";
        try {

            ByteTreeReader cnbtr = btr.getNextChild();
            if (cnbtr.getRemaining() > MAX_CLASSNAME_LENGTH) {
                throw new EIOException("Too long classname!");
            }
            className = cnbtr.readString();

            Class klass = Class.forName(className);
            Method method =
                klass.getMethod("newInstance", ByteTreeReader.class);

            return method.invoke(null, btr.getNextChild());

        } catch (InvocationTargetException ite) {
            throw new EIOException("Unable to interpret, unknown target!", ite);
        } catch (IllegalAccessException iae) {
            throw new EIOException("Unable to interpret, illegal access!", iae);
        } catch (ClassNotFoundException cnfe) {
            throw new EIOException("Unable to interpret, unknown class (" +
                                   className + ")!", cnfe);
        } catch (NoSuchMethodException nsme) {
            throw new EIOException("Unable to interpret, no method!", nsme);
        }
    }

    /**
     * Recovers an instance from the given <code>byte[]</code>
     * representation.
     *
     * @param bytes Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    protected static Object unmarshalBytesAux(byte[] bytes,
                                              RandomSource rs,
                                              int certainty)
        throws EIOException {
        return unmarshalAux(new ByteTreeReaderBT(new ByteTree(bytes, null)),
                            rs,
                            certainty);
    }

    /**
     * Recovers an instance from the given <code>byte[]</code>
     * representation.
     *
     * @param bytes Representation of an instance.
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    protected static Object unmarshalBytes(byte[] bytes)
        throws EIOException {
        return unmarshal((new ByteTree(bytes, null)).getByteTreeReader());
    }

    /**
     * Recovers an instance from the given hexidecimal representation.
     *
     * @param bytes Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    protected static Object unmarshalHexAux(String hex,
                                            RandomSource rs,
                                            int certainty)
        throws EIOException {
        int index = hex.lastIndexOf("::");
        if (index != -1) {
            hex = hex.substring(index + 2);
        }
        return unmarshalBytesAux(Hex.toByteArray(hex), rs, certainty);
    }

    /**
     * Recovers an instance from the given hexadecimal representation.
     *
     * @param bytes Representation of an instance.
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    protected static Object unmarshalHex(String hex)
        throws EIOException {
        int index = hex.lastIndexOf("::");
        if (index != -1) {
            hex = hex.substring(index + 2);
        }
        return unmarshalBytes(Hex.toByteArray(hex));
    }

    /**
     * Outputs a representation of this instance.  This method uses
     * Java reflection to encode the class name of the input instance
     * as part of the output. This allows recovering the correct type
     * of instance.
     *
     * @param m An instance to be marshalled.
     * @return Representation of input.
     */
    public static ByteTreeBasic marshal(Marshalizable m) {
        Class klass = m.getClass();
        try {

            String className = klass.getName();
            ByteTree bt = new ByteTree(className.getBytes("UTF8"));
            return new ByteTreeContainer(bt, m.toByteTree());

        } catch (UnsupportedEncodingException uee) {
            throw new CryptoError("This is a bug!", uee);
        }
    }

    /**
     * Outputs a representation of this instance.  This method uses
     * Java reflection to encode the class name of the input instance
     * as part of the output. This allows recovering the correct type
     * of instance.
     *
     * @param m An instance to be marshalled.
     * @return Representation of input.
     */
    public static byte[] marshalToBytes(Marshalizable m) {
        return marshal(m).toByteArray();
    }

    /**
     * Outputs a hexadecimal encoded representation of this instance.
     * This method uses Java reflection to encode the class name of
     * the input instance as part of the output. This allows
     * recovering the correct type of instance.
     *
     * @param m An instance to be marshalled.
     * @return Representation of input.
     */
    public static String marshalToHex(Marshalizable m) {
        return Hex.toHexString(marshalToBytes(m));
    }

    /**
     * Outputs a hexadecimal encoded representation of this instance
     * prepended with brief content description.  This method uses
     * Java reflection to encode the class name of the input instance
     * as part of the output. This allows recovering the correct type
     * of instance.
     *
     * @param m An instance to be marshalled.
     * @param verbose Use verbose human readable comment string.
     * @return Representation of input.
     */
    public static String marshalToHexHuman(Marshalizable m, boolean verbose) {
        return m.humanDescription(verbose) + "::" + marshalToHex(m);
    }


/*
 **************************************************************************
 **************** METHODS BELOW THIS LINE ARE GENERATED! ******************
 **************************************************************************
 */


    /**
    * Converts the input into an instance of {@link CryptoKeyGen}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static CryptoKeyGen
        unmarshalAux_CryptoKeyGen(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof CryptoKeyGen) {
            return (CryptoKeyGen)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link CryptoPKey}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static CryptoPKey
        unmarshalAux_CryptoPKey(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof CryptoPKey) {
            return (CryptoPKey)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link CryptoSKey}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static CryptoSKey
        unmarshalAux_CryptoSKey(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof CryptoSKey) {
            return (CryptoSKey)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link Hashfunction}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static Hashfunction
        unmarshalAux_Hashfunction(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof Hashfunction) {
            return (Hashfunction)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link HashfunctionFixedLength}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static HashfunctionFixedLength
        unmarshalAux_HashfunctionFixedLength(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof HashfunctionFixedLength) {
            return (HashfunctionFixedLength)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link PGroup}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static PGroup
        unmarshalAux_PGroup(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof PGroup) {
            return (PGroup)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link PRG}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static PRG
        unmarshalAux_PRG(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof PRG) {
            return (PRG)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link SignaturePKey}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static SignaturePKey
        unmarshalAux_SignaturePKey(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof SignaturePKey) {
            return (SignaturePKey)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link SignatureSKey}.
    *
    * @param bt Representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static SignatureSKey
        unmarshalAux_SignatureSKey(ByteTreeReader btr, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof SignatureSKey) {
            return (SignatureSKey)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link Hashfunction}.
    *
    * @param bt Representation of the output.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static Hashfunction
        unmarshal_Hashfunction(ByteTreeReader btr)
    throws EIOException {
        Object obj = unmarshal(btr);
        if (obj instanceof Hashfunction) {
            return (Hashfunction)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link PRG}.
    *
    * @param bt Representation of the output.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static PRG
        unmarshal_PRG(ByteTreeReader btr)
    throws EIOException {
        Object obj = unmarshal(btr);
        if (obj instanceof PRG) {
            return (PRG)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link RandomSource}.
    *
    * @param bt Representation of the output.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static RandomSource
        unmarshal_RandomSource(ByteTreeReader btr)
    throws EIOException {
        Object obj = unmarshal(btr);
        if (obj instanceof RandomSource) {
            return (RandomSource)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link CryptoKeyGen}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static CryptoKeyGen
        unmarshalHexAux_CryptoKeyGen(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof CryptoKeyGen) {
            return (CryptoKeyGen)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link Hashfunction}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static Hashfunction
        unmarshalHexAux_Hashfunction(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof Hashfunction) {
            return (Hashfunction)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link HashfunctionFixedLength}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static HashfunctionFixedLength
        unmarshalHexAux_HashfunctionFixedLength(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof HashfunctionFixedLength) {
            return (HashfunctionFixedLength)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link PGroup}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static PGroup
        unmarshalHexAux_PGroup(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof PGroup) {
            return (PGroup)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link PRG}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static PRG
        unmarshalHexAux_PRG(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof PRG) {
            return (PRG)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link SignatureKeyGen}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static SignatureKeyGen
        unmarshalHexAux_SignatureKeyGen(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof SignatureKeyGen) {
            return (SignatureKeyGen)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link SignatureKeyPair}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static SignatureKeyPair
        unmarshalHexAux_SignatureKeyPair(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof SignatureKeyPair) {
            return (SignatureKeyPair)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link SignaturePKey}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static SignaturePKey
        unmarshalHexAux_SignaturePKey(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof SignaturePKey) {
            return (SignaturePKey)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link SignatureSKey}.
    *
    * @param hex Hex code representation of the output.
    * @param rs Source of randomness used in testing.
    * @param certainty Determines the probability of accepting a
    * an incorrect input.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static SignatureSKey
        unmarshalHexAux_SignatureSKey(String hex, RandomSource rs, int certainty)
    throws EIOException {
        Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof SignatureSKey) {
            return (SignatureSKey)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link GeneratorTemplate}.
    *
    * @param hex Hex code representation of the output.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static GeneratorTemplate
        unmarshalHex_GeneratorTemplate(String hex)
    throws EIOException {
        Object obj = unmarshalHex(hex);
        if (obj instanceof GeneratorTemplate) {
            return (GeneratorTemplate)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link LargeInteger}.
    *
    * @param hex Hex code representation of the output.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static LargeInteger
        unmarshalHex_LargeInteger(String hex)
    throws EIOException {
        Object obj = unmarshalHex(hex);
        if (obj instanceof LargeInteger) {
            return (LargeInteger)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link RandomSource}.
    *
    * @param hex Hex code representation of the output.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static RandomSource
        unmarshalHex_RandomSource(String hex)
    throws EIOException {
        Object obj = unmarshalHex(hex);
        if (obj instanceof RandomSource) {
            return (RandomSource)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link SignaturePKey}.
    *
    * @param hex Hex code representation of the output.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static SignaturePKey
        unmarshalHex_SignaturePKey(String hex)
    throws EIOException {
        Object obj = unmarshalHex(hex);
        if (obj instanceof SignaturePKey) {
            return (SignaturePKey)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }

    /**
    * Converts the input into an instance of {@link SignatureSKey}.
    *
    * @param hex Hex code representation of the output.
    * @return An instance corresponding to the input.
    *
    * @throws EIOException If the input does not represent an
    * instance.
    */
    @SuppressWarnings("unchecked")
    public static SignatureSKey
        unmarshalHex_SignatureSKey(String hex)
    throws EIOException {
        Object obj = unmarshalHex(hex);
        if (obj instanceof SignatureSKey) {
            return (SignatureSKey)obj;
        } else {
            throw new EIOException("Type does not match cast!");
        }
    }



}