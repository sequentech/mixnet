
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
 * Generator of the key generation algorithm of the Cramer-Shoup
 * signature scheme.
 *
 * @author Douglas Wikstrom
 */
public class SignatureKeyGenCSGen implements Generator {

    /**
     * Default number of bits in modulus.
     */
    final static int DEFAULT_BITLENGTH = 2048;

    /**
     * Default statistical distance used when generating a hashfunction.
     */
    final static int DEFAULT_STATDIST = 200;

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
        Opt opt = GeneratorTool.
            defaultOpt(SignatureKeyGenCS.class.getSimpleName(), 1);

        opt.setUsageComment("(where " +
                            SignatureKeyGenCS.class.getSimpleName() +
                            " = " + SignatureKeyGenCS.class.getName() +
                            ")");

        opt.addParameter("bitlength", "Bits in modulus. Defaults to " +
                         DEFAULT_BITLENGTH + ".");
        opt.addOption("-crhf", "value",
                      "Collision-resistant hashfuntion. This must have " +
                      "small output bit length for efficiency reasons " +
                      "(instance of verificatum.crypto.Hashfunction). " +
                      "Defaults to SHA-256.");
        opt.addOption("-statDist", "bits", "Statistical error parameter. " +
                      "Defaults to " + DEFAULT_STATDIST + ".");
        opt.addOption("-cert", "value",
                      "Determines the probability that a composite is " +
                      "deemed to be a prime, i.e., a value " +
                      "of t gives a bound of 2^(-t)." +
                      " Defaults to " + DEFAULT_CERT + ".");

        String s =
"Generates an instance of a Cramer-Shoup signature key generator.";

        opt.appendToUsageForm(1,
                              "#-statDist,-cert,-crhf##bitlength");
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

        try {
            int bitlength = opt.getIntValue("bitlength", DEFAULT_BITLENGTH);
            int statDist = opt.getIntValue("-statDist", DEFAULT_STATDIST);
            int certainty = opt.getIntValue("-cert", DEFAULT_CERT);

            Hashfunction crhf = new HashfunctionHeuristic("SHA-256");
            if (opt.valueIsGiven("-crhf")) {
                String crhfString = opt.getStringValue("-crhf");

                crhf = Marshalizer.unmarshalHexAux_Hashfunction(crhfString,
                                                                randomSource,
                                                                certainty);
            }

            SignatureKeyGenCS keygen = new SignatureKeyGenCS(bitlength,
                                                             crhf,
                                                             statDist,
                                                             certainty);

            return Marshalizer.marshalToHexHuman(keygen,
                                                 opt.getBooleanValue("-v"));
        } catch (EIOException eioe) {

            throw new GenException("Unable to parse description of " +
                                   "hashfunction!",
                                   eioe);
        }
    }

    public String briefDescription() {
        return "Cramer-Shoup signature key generator.";
    }
}