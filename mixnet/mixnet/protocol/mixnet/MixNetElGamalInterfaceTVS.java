
/*
 * Copyright 2011 Chris Culnane
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

package mixnet.protocol.mixnet;

import mixnet.arithm.LargeInteger;
import mixnet.arithm.ModPGroup;
import mixnet.arithm.ModPGroupElement;
import mixnet.arithm.PGroupElement;
import mixnet.arithm.PPGroupElement;


/**
 * Implements the Trustworth Voting System (TVS) interface used by.
 *
 * @author Chris Culnane
 */
public class MixNetElGamalInterfaceTVS
    extends MixNetElGamalInterfaceNative {

    public String decodePlaintext(PGroupElement plaintext) {

	if (plaintext.getClass().equals(PPGroupElement.class)) {

	    PPGroupElement ppElem =((PPGroupElement)plaintext);

	    StringBuffer sb = new StringBuffer();

	    for (PGroupElement pge : ppElem.getFactors()){

		if (pge.getClass().equals(ModPGroupElement.class)) {

                    LargeInteger li = ((ModPGroupElement)pge).toLargeInteger();
		    sb.append(li.toString(10));
		    sb.append(",");

		} else {
		    return super.decodePlaintext(plaintext);
		}
	    }
	    return sb.toString();

	} else if (plaintext.getClass().equals(ModPGroupElement.class)) {

	    return ((ModPGroupElement)plaintext).toLargeInteger().toString(10);

	} else {

	    return super.decodePlaintext(plaintext);

	}
        //return ((ModPGroupElement)plaintext).toLargeInteger().toString(10);
	//try {
            //return Hex.toHexString(plaintext.toByteArray());
            //return filter(new String(plaintext.decode(), "UTF8"));
        //} catch (UnsupportedEncodingException uee) {
        //    throw new ProtocolError("Unable to decode plaintext!", uee);
        //}
    }
}
