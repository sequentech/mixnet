
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

package verificatum.crypto;

import verificatum.eio.*;
import verificatum.ui.gen.*;
import verificatum.ui.opt.*;

/**
 * Generates a human oriented string representation of a
 * <code>PRGHeuristic</code> suitable for initialization files. Using
 * {@link verificatum.ui.gen.GeneratorTool} this functionality can be
 * invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public class PRGHeuristicGen implements Generator {

    /**
     * Default statistical distance used when generating a hashfunction.
     */
    final static int DEFAULT_CERT = 100;

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        Opt opt =
            GeneratorTool.defaultOpt(PRGHeuristic.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + PRGHeuristic.class.getSimpleName() +
                            " = " + PRGHeuristic.class.getName() + ")");

        String s = "Generates an instance of a heuristically secure " +
            "pseudo-random generator. Internally is uses an instance of a " +
            "cryptographically strong hashfunction. A seed is expanded by " +
            "hashing the seed along with an integer counter that is " +
            "incremented for each iteration. The default hashfunction is " +
            "SHA-256.";

        opt.addParameter("hashfunction",
                         "Hashfunction used to expand the seed. This must " +
                         "be an instance of verificatum.crypto.Hashfunction. " +
                         "WARNING! Make sure that your hashfunction is " +
                         "suitable to be used as a pseudo-random function.");

        opt.addOption("-cert", "value",
                      "Determines the probability that the underlying " +
                      "fixed-length hashfunction is malformed, i.e., a value " +
                      "of t gives a bound of 2^(-t)." +
                      " Defaults to " + DEFAULT_CERT + ".");

        opt.appendToUsageForm(1, "###hashfunction");

        opt.appendDescription(s);

        return opt;
    }

    // Documented in verificatum.crypto.Generator.java.

    public String gen(RandomSource randomSource, String[] args)
    throws GenException {
        Opt opt = opt();

        String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }

       int certainty = opt.getIntValue("-cert", DEFAULT_CERT);

        try {
            Hashfunction hashfunction;
            if (opt.valueIsGiven("hashfunction")) {

                String s = opt.getStringValue("hashfunction");
                hashfunction =
                    Marshalizer.unmarshalHexAux_Hashfunction(s,
                                                             randomSource,
                                                             certainty);

            } else {

                hashfunction = new HashfunctionHeuristic("SHA-256");
            }

            PRGHeuristic prg = new PRGHeuristic(hashfunction);
            return Marshalizer.marshalToHexHuman(prg,
                                                 opt.getBooleanValue("-v"));
        } catch (EIOException eioe) {
            throw new GenException("Malformed hashfunction!", eioe);
        }
    }

    public String briefDescription() {
        return "Pseudo random generator based on a cryptographic " +
            "hashfunction with a counter.";
    }
}