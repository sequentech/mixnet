
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

package vfork.crypto;

import vfork.eio.*;
import vfork.ui.gen.*;
import vfork.ui.opt.*;

import vfork.arithm.*;

/**
 * Generates a human oriented string representation of a
 * <code>HashfunctionMerkleDamgaard</code> suitable for initialization
 * files. Using {@link vfork.ui.gen.GeneratorTool} this
 * functionality can be invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public class HashfunctionMerkleDamgaardGen implements Generator {

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

        String simpleName = HashfunctionMerkleDamgaard.class.getSimpleName();

        Opt opt = GeneratorTool.defaultOpt(simpleName, 1);

	opt.setUsageComment("(where " + simpleName +
			    " = " +
                            HashfunctionMerkleDamgaard.class.getName() + ")");

	opt.addParameter("flHash",
			 "Underlying fixed length hashfunction, an instance " +
			 "of " + HashfunctionFixedLength.class.getName() + ".");
        opt.addOption("-cert", "value",
                      "Determines the probability that the underlying " +
                      "fixed-length hashfunction is malformed, i.e., a value " +
                      "of t gives a bound of 2^(-t)." +
                      " Defaults to " + DEFAULT_CERT + ".");

	String s = "Generates an instance of the Merkle-Damgaard " +
	    "hashfunction based on the given fixed length hashfunction.";

	opt.appendToUsageForm(1, "#-cert#flHash#");

	opt.appendDescription(s);

	return opt;
    }

    // Documented in crypto.Generator.java.

    public String gen(RandomSource randomSource, String[] args)
    throws GenException {
	Opt opt = opt();

	String res = GeneratorTool.defaultProcess(opt, args);
	if (res != null) {
	    return res;
	}

        int certainty = opt.getIntValue("-cert", DEFAULT_CERT);

	try {
	    String flhfString = opt.getStringValue("flHash");
	    HashfunctionFixedLength flh =
		Marshalizer.unmarshalHexAux_HashfunctionFixedLength(flhfString,
                                                                 randomSource,
                                                                 certainty);

	    HashfunctionMerkleDamgaard hf = new HashfunctionMerkleDamgaard(flh);

	    return Marshalizer.marshalToHexHuman(hf, opt.getBooleanValue("-v"));

	} catch (EIOException eioe) {
	    throw new GenException("");
	}
    }

    public String briefDescription() {
	return "The provably-secure Merkle-Damgaard construction.";
    }
}