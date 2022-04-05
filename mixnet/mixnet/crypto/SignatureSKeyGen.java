
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
 * Extracts a public key from a key pair.
 *
 * @author Douglas Wikstrom
 */
public class SignatureSKeyGen implements Generator {

    /**
     * Default statistical distance.
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
            defaultOpt(SignatureSKey.class.getSimpleName(), 1);

        opt.setUsageComment("(where " +
                            SignatureSKey.class.getSimpleName() +
                            " = " + SignatureSKey.class.getName() +
                            ")");

        opt.addParameter("keypair",
                         "Signature key pair " +
                         "(mixnet.crypto.SignatureKeyPair).");
        opt.addOption("-cert", "value",
                      "Determines the probability that the input key pair " +
                      "is malformed, i.e., a value " +
                      "of t gives a bound of 2^(-t)." +
                      " Defaults to " + DEFAULT_CERT + ".");

        String s = "Extracts a signature secret key from a key pair.";

        opt.appendToUsageForm(1, "#-cert#keypair#");
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

        String keypairString = opt.getStringValue("keypair");
        int certainty = opt.getIntValue("-cert", DEFAULT_CERT);

        try {
            SignatureKeyPair keypair =
                Marshalizer.unmarshalHexAux_SignatureKeyPair(keypairString,
                                                             randomSource,
                                                             certainty);

            return Marshalizer.marshalToHexHuman(keypair.getSKey(),
                                                 opt.getBooleanValue("-v"));
        } catch (EIOException eioe) {
            throw new GenException("Malformed key pair!", eioe);
        }
    }

    public String briefDescription() {
        return "Signature secret key extractor.";
    }
}
