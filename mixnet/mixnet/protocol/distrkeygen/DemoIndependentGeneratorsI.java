
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
import mixnet.protocol.coinflip.*;
import mixnet.protocol.demo.*;
import mixnet.protocol.secretsharing.*;
import mixnet.ui.*;
import mixnet.ui.info.*;
import mixnet.ui.opt.*;

/**
 * Demonstrates {@link IndependentGenerator}.
 *
 * @author Douglas Wikstrom
 */
class DemoIndependentGeneratorsI extends DemoPlainKeys {

    // These methods are documented in DemoProtocolFactory.java.

    public Runnable newProtocol(PrivateInfo privateInfo,
                                ProtocolInfo protocolInfo,
                                UI ui) {
        return new ExecIndependentGenerator(privateInfo, protocolInfo, ui);
    }


    public Opt generateOpt(Demo demo) {
        Opt opt = super.generateOpt(demo);

        opt.addOption("-pGroup", "pGroup",
                      "Prime order group used in the protocol.");
        opt.appendToUsageForm(0, "#-pGroup##");

        return opt;
    }

    public ProtocolInfo newProtocolInfo() {
        ProtocolInfo pi = super.newProtocolInfo();
        pi.addInfoField(new StringField("pgroup", 1, 1));
        pi.addInfoField(new IntField("challengeBitLength", 1, 1));
        pi.addInfoField(new IntField("batchBitLength", 1, 1));
        return pi;
    }

    public void generateProtocolInfo(ProtocolInfo pi,
                                     Demo demo,
                                     Opt opt) {
        super.generateProtocolInfo(pi, demo, opt);

        pi.addValue("pgroup",
                    opt.getStringValue("-pGroup", ProtocolDefaults.PGroup()));
        pi.addValue("batchBitLength",
                    opt.getIntValue("-sece",
                                    ProtocolDefaults.SEC_PARAM_BATCH));
        pi.addValue("challengeBitLength",
                    opt.getIntValue("-secc",
                                    ProtocolDefaults.SEC_PARAM_CHALLENGE));
    }

    /**
     * This is the main function called from the command line to
     * invoke the demonstration.
     *
     * @param args Arguments to the demonstration.
     */
    public static void main(String[] args) {

        Demo demo = new Demo(args, new DemoIndependentGeneratorsI());
    }

    /**
     * Turns {@link IndependentGenerator} into a runnable object.
     */
    class ExecIndependentGenerator extends Protocol
        implements Runnable {

        public ExecIndependentGenerator(PrivateInfo privateInfo,
                                        ProtocolInfo protocolInfo,
                                        UI ui) {
            super(privateInfo, protocolInfo, ui);
            startServers();
        }

        public void run() {
            try {

                int statDist = protocolInfo.getIntValue("statdist");
                int certainty = protocolInfo.getIntValue("certainty");
                int batchBitLength = protocolInfo.getIntValue("batchBitLength");
                int challengeBitLength =
                    protocolInfo.getIntValue("challengeBitLength");


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

                CoinFlipPRingSource coins =
                    new CoinFlipPRingSource("DemoCoinsSID",
                                             this,
                                             pGroup.getg().exp(7),
                                             plainKeys.getPKeys(),
                                             plainKeys.getSKey(),
                                             statDist);
                coins.prepareCoins(ui.getLog(), 3);

                IndependentGenerators ig =
                    new IndependentGeneratorsI("DemoSID",
                                               this,
                                               coins,
                                               new PRGHeuristic(),
                                               challengeBitLength,
                                               batchBitLength,
                                               statDist);
                PGroupElementArray generators =
                    ig.generate(ui.getLog(), pGroup, 3);
                ui.getLog().info("generators = " + generators.toString());

            } catch (Exception e) {
                throw new DemoError("Unable to run demonstration!", e);
            }
        }
    }
}
