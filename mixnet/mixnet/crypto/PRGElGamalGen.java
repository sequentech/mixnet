
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

package mixnet.crypto;

import mixnet.arithm.*;
import mixnet.eio.*;
import mixnet.ui.gen.*;
import mixnet.ui.opt.*;

/**
 * Generates a human oriented string representation of a
 * <code>PRGElGamal</code> suitable for initialization files. Using
 * {@link mixnet.ui.gen.GeneratorTool} this functionality can be
 * invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public class PRGElGamalGen implements Generator {

    final static int CERTAINTY = 100;

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        Opt opt = GeneratorTool.defaultOpt(PRGElGamal.class.getSimpleName(), 2);

        opt.setUsageComment("(where " + PRGElGamal.class.getSimpleName() +
                            " = " + PRGElGamal.class.getName() + ")");

        opt.addParameter("modulus",
                     "Modulus in hexadecimal twos complement representation.");
        opt.addParameter("bitLen", "Bits in modulus.");

        opt.addOption("-fixed", "",
                      "Fixed modulus of given size from table in class "
                      + "mixnet.arithm.SafePrimeTable.");
        opt.addOption("-explic", "", "Explicit modulus.");
        opt.addOption("-width", "width",
                      "Number of generators in internal state.");
        opt.addOption("-statDist", "bits", "Statistical parameter.");
        opt.addOption("-cert", "",
                      "Certainty with which probabilistically checked " +
                      "parameters are verified, i.e., the probability of an " +
                      "error is bounded by 2^(-certainty). Default value " +
                      "is " + CERTAINTY + ".");


        String s = "Generates an instance of a provably secure pseudo-random " +
            "generator based on the Decision Diffie-Hellman assumption (DDH) " +
            "in the multiplicative group modulo a safe prime. The parameter " +
            "<width> does not influence security. It decides the number of " +
            "group elements in the internal state of the generator. The " +
            "default width is " + PRGElGamal.DEFAULT_WIDTH + ".";

        opt.appendToUsageForm(1, "-explic#-width,-statDist,-cert#modulus#");
        opt.appendToUsageForm(2, "-fixed#-width,-statDist,-cert#bitLen#");

        opt.appendDescription(s);

        return opt;
    }

    // Documented in mixnet.crypto.Generator.java.

    public String gen(RandomSource randomSource, String[] args)
    throws GenException {

        Opt opt = opt();

        String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }
        LargeInteger modulus = null;

        int certainty = CERTAINTY;
        if (opt.valueIsGiven("-cert")) {
            certainty = opt.getIntValue("-cert");
            if (certainty <= 0) {
                throw new GenException("Certainty must be positive!");
            }
        }

        if (opt.getBooleanValue("-explic")) {
            String hex = opt.getStringValue("modulus");
            try {
                modulus = Marshalizer.unmarshalHex_LargeInteger(hex);
            } catch (EIOException eioe) {
                throw new GenException("Malformed modulus!", eioe);
            }
        } else {

            int bitLen = opt.getIntValue("bitLen");

            try {
                modulus = SafePrimeTable.safePrime(bitLen);
            } catch (ArithmFormatException afe) {
                throw new GenException("Invalid bit length!", afe);
            }
        }

        int width = opt.getIntValue("-width", PRGElGamal.DEFAULT_WIDTH);
        int statDist =
            opt.getIntValue("-statDist", PRGElGamal.DEFAULT_STATDIST);

        PRGElGamal prg = new PRGElGamal(modulus, width, statDist);
        return Marshalizer.marshalToHexHuman(prg, opt.getBooleanValue("-v"));
    }

    public String briefDescription() {
        return "Pseudo random generator based on DDH in multiplicative "
            + "group modulo a safe prime.";
    }
}