
import java.util.*;

import mixnet.crypto.*;
import mixnet.eio.*;

public class TestVectors {

    public static void print(String s) {
        System.out.print(s);
    }

    public static void println(String s) {
        System.out.println(s);
    }

    public static void printHex(byte[] data, int width, boolean end) {

        int index = 0;

        StringBuilder sb = new StringBuilder();

        while (index < data.length) {

            int len = Math.min(width, data.length - index);
            byte[] row = Arrays.copyOfRange(data, index, index + len);

            sb.append("&\\hex{" + Hex.toHexString(row) + "}\\\\\n");
            index += len;
        }

        if (end) {
            sb.delete(sb.length() - 3, sb.length());
            sb.append("\n");
        }
        print(sb.toString());
    }

    public static void printPRGTest(String algorithm) {

        int width = 32;

        Hashfunction sha2 = new HashfunctionHeuristic(algorithm);

        PRG prg = new PRGHeuristic(sha2);
        byte[] seed = new byte[prg.minNoSeedBytes()];

        for (int i = 0; i < seed.length; i++) {
            seed[i] = (byte)i;
        }
        prg.setSeed(seed);

        println("\\begin{align*}");
        println("&\\PRG(\\Hashfunction(\\leaf{\\code{\"" +
                algorithm + "}\"}))\\\\");
        println("&\\textrm{Seed (" + seed.length + " bytes):}\\\\");
        printHex(seed, width, false);

        int rows = 4;

        println("&\\textrm{Expansion (" + (width * rows) + " bytes):}\\\\");
        printHex(prg.getBytes(width * rows), width, true);

        println("\\end{align*}");
    }

    public static void printRandomOracleTest(String algorithm, int outputLen) {

        int width = 32;

        Hashfunction sha2 = new HashfunctionHeuristic(algorithm);

        RandomOracle ro = new RandomOracle(sha2, outputLen);

        byte[] input = new byte[width];

        for (int i = 0; i < input.length; i++) {
            input[i] = (byte)i;
        }

        println("\\begin{align*}");
        println("&\\RandomOracle(\\Hashfunction(\\leaf{\\code{\"" +
                algorithm + "}\"})," + outputLen + ")\\\\");
        println("&\\textrm{Input (" + input.length + " bytes):}\\\\");
        printHex(input, width, false);

        byte[] output = ro.hash(input);

        println("&\\textrm{Output (" + output.length +
                " bytes of which the last " + outputLen +
                " bits may be non-zero):}\\\\");

        printHex(output, width, true);

        println("\\end{align*}");
    }


    public static void main(String[] args) {

        printPRGTest("SHA-256");
        printPRGTest("SHA-384");
        printPRGTest("SHA-512");

        printRandomOracleTest("SHA-256", 65);
        printRandomOracleTest("SHA-256", 256 + 5);

        printRandomOracleTest("SHA-384", 93);
        printRandomOracleTest("SHA-384", 384 + 27);

        printRandomOracleTest("SHA-512", 111);
        printRandomOracleTest("SHA-512", 512 + 67);

    }
}