
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
 * Demonstrates the protocol for generating plain keys of a
 * cryptosystem.
 *
 * @author Douglas Wikstrom
 */
public class DemoPlainKeys extends DemoProtocolFactory {

    // These methods are documented in DemoProtocolFactory.java.

    public Runnable newProtocol(PrivateInfo privateInfo,
                                ProtocolInfo protocolInfo,
                                UI ui) {
        return new ExecPlainKeys(privateInfo,
                                 protocolInfo,
                                 ui);
    }

    public Opt generateOpt(Demo demo) {
        Opt opt = super.generateOpt(demo);
        opt.addOption("-statdist", "stat dist",
                      "Decides statistical error of distributions.");
        opt.appendToUsageForm(0, "#-statdist##");

        return opt;
    }

    public PrivateInfo newPrivateInfo() {
        PrivateInfo pi = super.newPrivateInfo();
        pi.addInfoField(new StringField("keyGen", 1, 1));
        return pi;
    }


    public ProtocolInfo newProtocolInfo() {
        ProtocolInfo pi = super.newProtocolInfo();
        pi.addInfoField(new IntField("statdist", 1, 1));
        pi.addInfoField(new IntField("certainty", 1, 1));
        return pi;
    }

    public void generateProtocolInfo(ProtocolInfo pi,
                                     Demo demo,
                                     Opt opt) {
        super.generateProtocolInfo(pi, demo, opt);
        pi.addValue("statdist",
                    opt.getIntValue("-statdist", ProtocolDefaults.STAT_DIST));
        pi.addValue("certainty",
                    opt.getIntValue("-cert", ProtocolDefaults.CERTAINTY));
    }

    public void generatePrivateInfo(PrivateInfo pi,
                                    Demo demo,
                                    Opt opt,
                                    int j) {
        super.generatePrivateInfo(pi, demo, opt, j);

        pi.addValue("keyGen",
                    demo.template(opt, "-keygen",
                                  ProtocolDefaults.CryptoKeyGen()));
    }

    /**
     * This is the main function called from the command line to
     * invoke the demonstration.
     *
     * @param args Arguments to the demonstration.
     */
    public static void main(String[] args) {

        Demo demo = new Demo(args, new DemoPlainKeys());
    }

    /**
     * Turns {@link PlainKeys} into a runnable object.
     */
    class ExecPlainKeys extends Protocol
        implements Runnable {

        public ExecPlainKeys(PrivateInfo privateInfo,
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

            } catch (Exception e) {
                throw new DemoError("Unable to run demonstration!", e);
            }
        }
    }
}
