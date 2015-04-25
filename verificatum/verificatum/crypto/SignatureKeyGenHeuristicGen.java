
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

import verificatum.arithm.*;
import verificatum.eio.*;
import verificatum.ui.gen.*;
import verificatum.ui.opt.*;

/**
 * Generator of the key generation algorithm of the standard RSA
 * signature scheme based on SHA-256.
 *
 * @author Douglas Wikstrom
 */
public class SignatureKeyGenHeuristicGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        Opt opt = GeneratorTool.
            defaultOpt(SignatureKeyGenHeuristic.class.getSimpleName(), 1);

        opt.setUsageComment("(where " +
                            SignatureKeyGenHeuristic.class.getSimpleName() +
                            " = " + SignatureKeyGenHeuristic.class.getName() +
                            ")");

        opt.addParameter("bitlength", "Bits in modulus.");

        String s =
"Generates an instance of a full domain hash (SHA-256) RSA signature " +
"key generator for keys of the given key length";

        opt.appendToUsageForm(1, "##bitlength#");
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

        int bitlength = opt.getIntValue("bitlength");

        SignatureKeyGenHeuristic keygen =
            new SignatureKeyGenHeuristic(bitlength);

        return Marshalizer.marshalToHexHuman(keygen,
                                             opt.getBooleanValue("-v"));
    }

    public String briefDescription() {
        return "RSA signature key generator.";
    }
}