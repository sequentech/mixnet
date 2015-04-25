
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

package verificatum.eio;

/**
 * Utility class for converting hexadecimal strings to/from
 * <code>byte[]</code>.
 *
 * @author Douglas Wikstrom
 */
public class Hex {

    /**
     * Used to translate to hex code.
     */
    final static char[] hextable = {'0', '1', '2', '3', '4', '5',
                                    '6', '7', '8', '9', 'a', 'b',
                                    'c', 'd', 'e', 'f'};

    /**
     * Returns the hex code of the input. It is assumes that the input
     * is an integer between 0 and 15. Otherwise the output is
     * undefined.
     *
     * @param i Integer to be translated.
     * @return Hexadecimal code for the input integer.
     */
    public static char toHex(int i) {
        return hextable[i % 16];
    }

    /**
     * Returns the hex code representation of a <code>byte[]</code>.
     *
     * @param array Array to be translated.
     * @return Representation of the input in hexadecimal.
     */
    public static String toHexString(byte[] array) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            byte b1 = (byte)(array[i] & 0x0F);
            byte b2 = (byte)((array[i] & 0xF0) >>> 4);

            sb = sb.append(hextable[b2]).append(hextable[b1]);
        }
        return sb.toString();
    }

    /**
     * Converts a hexadecimal <code>String</code> of even length into
     * a <code>byte[]</code>. If the input does not have even length a
     * leading zero is prepended to the input before
     * processing. Characters that do not represent hexadecimal digits
     * are replaced by "0" before conversion.
     *
     * @param hexString Hexadecimal <code>String</code> of even length.
     * @return Representation of the input as a <code>byte[]</code>.
     */
    public static byte[] toByteArray(String hexString) {
        if (hexString.length() % 2 != 0) {
            hexString = hexString + "0";
        }
        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < result.length; i++) {
            String subStr = hexString.substring(2 * i, 2 * i + 2);
            try {
                result[i] = (byte)Integer.parseInt(subStr, 16);
            } catch (NumberFormatException nfe) {
                result[i] = 0;
            }
        }
        return result;
    }
}
