
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

import mixnet.eio.*;
import mixnet.ui.gen.*;
import mixnet.ui.opt.*;

/**
 * Generates a human oriented string representation of a
 * <code>HashfunctionHeuristic</code> suitable for initialization
 * files. Using {@link mixnet.ui.gen.GeneratorTool} this
 * functionality can be invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public class HashfunctionHeuristicGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid parameters.
     */
    protected Opt opt() {
        Opt opt = GeneratorTool
            .defaultOpt(HashfunctionHeuristic.class.getSimpleName(), 1);

        opt.setUsageComment("(where " +
                            HashfunctionHeuristic.class.getSimpleName() +
                            " = " + HashfunctionHeuristic.class.getName() +
                            ")");

        opt.addParameter("algorithm",
                         "Algorithm (SHA-256, SHA-384, or SHA-512).");

        String s = "Generates an instance of a heuristically secure " +
            "hashfunction from the SHA-2 family.";

        opt.appendToUsageForm(1, "##algorithm#");

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

        String algorithm = opt.getStringValue("algorithm");

        if (algorithm.equals("SHA-256")
            || algorithm.equals("SHA-384")
            || algorithm.equals("SHA-512")) {

            HashfunctionHeuristic hashfunction =
                new HashfunctionHeuristic(algorithm);
            return Marshalizer.marshalToHexHuman(hashfunction,
                                                 opt.getBooleanValue("-v"));

        } else {
            throw new GenException("The given hashfunction (" + algorithm +
                                   ") is not available!");
        }
    }

    public String briefDescription() {
        return "Standard heuristically secure hashfunction from the " +
            "SHA-2 family.";
    }
}