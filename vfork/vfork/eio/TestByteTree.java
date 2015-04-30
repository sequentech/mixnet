
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

package vfork.eio;

import java.io.*;

import vfork.crypto.*;
import vfork.test.*;

/**
 * Tests some of the functionality of {@link ByteTree} and associated
 * classes.
 *
 * @author Douglas Wikstrom
 */
public class TestByteTree {

    public static boolean convertBoolean(TestParameters tp) throws Exception {

        boolean b;
        ByteTree bt;

        bt = ByteTree.booleanToByteTree(true);
        b = ByteTree.byteTreeToBoolean(bt);
        if (!b) {
            return false;
        }

        bt = ByteTree.booleanToByteTree(false);
        b = ByteTree.byteTreeToBoolean(bt);
        if (b) {
            return false;
        }
        return true;
    }

    public static boolean convertInt(TestParameters tp) throws Exception {
        int REPETITIONS = 100;

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        byte[] intAsByte = new byte[4];
        byte[] intAsByte2 = new byte[4];

        for (int i = 0; i < REPETITIONS; i++) {

            rs.getBytes(intAsByte);

            int k = ExtIO.readInt(intAsByte, 0);

            ExtIO.writeInt(intAsByte2, 0, k);

            int l = ExtIO.readInt(intAsByte2, 0);

            ByteTree bt = ByteTree.intToByteTree(l);

            int m = ByteTree.byteTreeToInt(bt);

            if (k != m) {
                return false;
            }
        }
        return true;
    }

    public static boolean convertShort(TestParameters tp) throws Exception {
        int REPETITIONS = 1000;

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        byte[] shortAsByte = new byte[2];
        byte[] shortAsByte2 = new byte[2];

        for (int i = 0; i < REPETITIONS; i++) {

            rs.getBytes(shortAsByte);

            short k = ExtIO.readShort(shortAsByte, 0);

            ExtIO.writeShort(shortAsByte2, 0, k);

            short l = ExtIO.readShort(shortAsByte2, 0);

            ByteTree bt = ByteTree.shortToByteTree(l);

            short m = ByteTree.byteTreeToShort(bt);

            if (k != m) {
                return false;
            }
        }
        return true;
    }

    public static boolean convertString(TestParameters tp) throws Exception {

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        int MAXSIZE = 100;

        byte[] stringContent = new byte[MAXSIZE];

        for (int i = 0; i < tp.testSize; i++) {

            rs.getBytes(stringContent);

            String s1 = new String(stringContent, "UTF-8");


            // This roundabout is needed to make sure that we start
            // from a valid string.
            System.arraycopy(s1.getBytes("UTF-8"), 0,
                             stringContent, 0, MAXSIZE);
            s1 = new String(stringContent, "UTF-8");

            ByteTree bt = ByteTree.stringToByteTree(s1);
            String s2 = ByteTree.byteTreeToString(bt);

            if (!s1.equals(s2)) {
                return false;
            }
        }
        return true;
    }

    protected static ByteTree generateByteTree(RandomSource rs,
                                               int totalSize) {
        byte[] decision = new byte[1];
        rs.getBytes(decision);

        if (totalSize == 1 || Math.abs(decision[0]) % 3 < 2) {
            byte[] content = new byte[totalSize];
            rs.getBytes(content);
            return new ByteTree(content);
        } else {
            ByteTree[] byteTrees = new ByteTree[5];
            for (int j = 0; j < 5; j++) {
                byteTrees[j] =
                    generateByteTree(rs, Math.max(1, totalSize - 5 * j));
            }
            return new ByteTree(byteTrees);
        }
    }

    public static boolean writeByteTree(TestParameters tp) throws Exception {

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        ByteTree bt = generateByteTree(rs, 100);

        byte[] content = bt.toByteArray();
        ByteTree bt2 = new ByteTree(content, null);
        byte[] content2 = bt2.toByteArray();

        for (int j = 0; j < content.length; j++) {
            if (content2[j] != content[j]) {
                return false;
            }
        }
        return true;
    }
}
