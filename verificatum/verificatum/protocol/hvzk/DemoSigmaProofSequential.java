
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

package verificatum.protocol.hvzk;

import java.io.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.protocol.*;
import verificatum.protocol.coinflip.*;
import verificatum.protocol.demo.*;
import verificatum.protocol.distrkeygen.*;
import verificatum.ui.*;
import verificatum.ui.info.*;
import verificatum.ui.opt.*;

/**
 * Demonstrates the execution of a {@link SigmaProof}.
 *
 * @author Douglas Wikstrom
 */
public class DemoSigmaProofSequential extends DemoPlainKeys {

    // These methods are documented in DemoProtocolFactory.java.

    public Runnable newProtocol(PrivateInfo privateInfo,
                                ProtocolInfo protocolInfo,
                                UI ui) {
        return new ExecSigmaProof(privateInfo, protocolInfo, ui);
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

        Demo demo = new Demo(args, new DemoSigmaProofSequential());
    }

    /**
     * Turns {@link SigmaProof} into a runnable object.
     */
    class ExecSigmaProof extends Protocol
        implements Runnable {

        public ExecSigmaProof(PrivateInfo privateInfo,
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
                pGroup = new PPGroup(pGroup, 2);


                CoinFlipPRingSource coins =
                    new CoinFlipPRingSource("DemoCoinsSID",
                                             this,
                                             pGroup.getg().exp(7),
                                             plainKeys.getPKeys(),
                                             plainKeys.getSKey(),
                                             statDist);

                Challenger challenger1 = new ChallengerI(coins);

                int size = 100;
                BiPRingPGroup biExp1 = new BiFixedBaseExp(pGroup, size);
                HomPRingPGroup hom1 = biExp1.restrict(pGroup.getg());

                PRG prg1 = new PRGHeuristic();
                byte[] seed1 = new byte[prg1.minNoSeedBytes()];
                Arrays.fill(seed1, (byte)0);
                prg1.setSeed(seed1);

                PRingElement[] privateInput1 = new PRingElement[k + 1];
                PGroupElement[] commonInput1 = new PGroupElement[k + 1];

                for (int l = 1; l <= k; l++) {
                    privateInput1[l] =
                        hom1.getDomain().randomElement(prg1, statDist);
                    commonInput1[l] = hom1.map(privateInput1[l]);
                }

                 SigmaProofSequential sigma1 =
                    new SigmaProofSequential("SID",
                                             this,
                                             hom1,
                                             pGroup.getg(),
                                             challenger1,
                                             prg1,
                                             challengeBitLength,
                                             batchBitLength,
                                             statDist);
                 sigma1.execute(ui.getLog(), commonInput1, privateInput1[j],
                                null);

            } catch (Exception e) {
                throw new DemoError("Unable to run demonstration!", e);
            }
        }
    }
}

