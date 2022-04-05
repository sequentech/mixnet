
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

package mixnet.protocol.distrkeygen;

import java.io.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.protocol.*;
import mixnet.protocol.demo.*;
import mixnet.ui.*;
import mixnet.ui.info.*;
import mixnet.ui.opt.*;

/**
 * Demonstrates the distributed key generation protocol {@link DKG}.
 *
 * @author Douglas Wikstrom
 */
class DemoDKG extends DemoIndependentGenerator {

    // These methods are documented in DemoProtocolFactory.java.

    public Runnable newProtocol(PrivateInfo privateInfo,
                                ProtocolInfo protocolInfo,
                                UI ui) {
        return new ExecDKG(privateInfo, protocolInfo, ui);
    }

    /**
     * This is the main function called from the command line to
     * invoke the demonstration.
     *
     * @param args Arguments to the demonstration.
     */
    public static void main(String[] args) {

        Demo demo = new Demo(args, new DemoDKG());
    }

    /**
     * Turns {@link DKG} into a runnable object.
     */
    class ExecDKG extends Protocol
        implements Runnable {

        public ExecDKG(PrivateInfo privateInfo,
                                    ProtocolInfo protocolInfo,
                                    UI ui) {
            super(privateInfo, protocolInfo, ui);
            startServers();
        }

        public void run() {
            try {

                int statDist = protocolInfo.getIntValue("statdist");
                int certainty = protocolInfo.getIntValue("certainty");


                String keyGenString = privateInfo.getStringValue("keyGen");
                CryptoKeyGen keyGen =
                    Marshalizer.unmarshalHexAux_CryptoKeyGen(keyGenString,
                                                             randomSource,
                                                             certainty);
                PlainKeys plainKeys =
                    new PlainKeys("DemoSID", this, keyGen, statDist);
                plainKeys.generate(ui.getLog());

                String pGroupString = protocolInfo.getStringValue("pgroup");
                PGroup pGroup = Marshalizer.unmarshalHexAux_PGroup(pGroupString,
                                                                   randomSource,
                                                                   certainty);
                BiPRingPGroup bi = new BiExp(pGroup);
                DKG dkg = new DKG("DemoSID",
                                  this,
                                  plainKeys.getPKeys(),
                                  plainKeys.getSKey(),
                                  statDist);

                dkg.generate(ui.getLog(), bi, bi.getPGroupDomain().getg());

                StringBuffer sb = new StringBuffer();
                sb.append(dkg.getPublicKey(1));
                for (int i = 2; i <= k; i++) {
                    sb.append("," + dkg.getPublicKey(i));
                }
                ui.getLog().info("Public keys: " + sb.toString());

                for (int i = 1; i <= k; i++) {
                    PRingElement esk = dkg.recoverSecretKey(ui.getLog(), i);
                    ui.getLog().info("sk = " + esk.toString());
                }

            } catch (Exception e) {
                throw new DemoError("Unable to run demonstration!", e);
            }
        }
    }
}
