
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

package vfork.protocol.demo;

import java.io.*;

import vfork.ui.*;
import vfork.ui.info.*;
import vfork.protocol.*;
import vfork.ui.opt.*;

/**
 * Interface that must be implemented to allow demonstration of a
 * protocol. The methods provide the information about a protocol
 * needed by {@link Demo} to run a demonstration.
 *
 * <ul>
 *
 * <li> {@link #newProtocol(String, String, UI)} must return a runnable
 * version of the protocol. Typically, the original protocol is
 * wrapped in a subclass of {@link Protocol} that implements {@link
 * java.lang.Runnable}. The protocol execution is started by calling
 * {@link java.lang.Runnable#run()}.
 *
 * <li> {@link #generateOpt(Demo)} must return an {@link Opt} instance
 * that has been configured to accept certain options. This instance
 * is used by {@link Demo} to parse the command line and print usage
 * information of the resulting demonstrator. Preferably, most options
 * should be optional.
 *
 * <li> {@link #generateProtocolInfoFile(Demo, File, File, Opt)} must,
 * given the options provided by the user, generate a protocol info
 * file. This file contains public information about all parties in
 * the protocol and joint parameters agreed on by the parties.
 *
 * <li> {@link #generatePrivateInfoFile(Demo, File, File, int, Opt)}
 * must, given the options provided by the user, generate a private
 * info file. This file contains private information needed by a
 * single party, e.g., seeds to random generators, or secret keys.
 *
 * </ul>
 *
 * We remark that for very simple protocols, it may suffice to provide
 * {@link #newProtocol(String, String, UI)} with an actual
 * implementation and implement dummy versions of the other methods,
 * but this is not recommended since the user can not instrument such
 * a demonstration.
 *
 * @author Douglas Wikstrom
 */
public abstract class DemoProtocolFactory {

    /**
     * Creates a root protocol.
     *
     * @param privateInfoFilename Name of file containing private info.
     * @param protocolInfoFilename Name of file containing protocol
     * info.
     * @param ui User interface.
     * @return Runnable version of this protocol.
     */
    public Runnable newProtocol(String privateInfoFilename,
                                String protocolInfoFilename,
                                UI ui) {
        try {
            return newProtocol(newPrivateInfo().parse(privateInfoFilename),
                               newProtocolInfo().parse(protocolInfoFilename),
                               ui);
        } catch (InfoException ie) {
            throw new ProtocolError("Malformed info files!", ie);
        }
    }

    /**
     * Creates a root protocol.
     *
     * @param pri Private info.
     * @param pi Protocol info.
     * @param ui User interface.
     * @return Runnable version of this protocol.
     */
    public abstract Runnable newProtocol(PrivateInfo pri,
                                         ProtocolInfo pi,
                                         UI ui);

    /**
     * Creates a new protocol info instance containing only the
     * default fields.
     *
     * @return Instance containing only default fields.
     */
    public ProtocolInfo newProtocolInfo() {
        return Protocol.newProtocolInfo();
    }

    /**
     * Creates a new private info instance containing only the default
     * fields.
     *
     * @return Instance containing only default fields.
     */
    public PrivateInfo newPrivateInfo() {
        return Protocol.newPrivateInfo();
    }

    /**
     * Initializes the default fields of the input protocol info.
     *
     * @param pi Protocol info.
     * @param demo Demonstraction instance.
     * @param opt Options given by the user.
     */
    public void generateProtocolInfo(ProtocolInfo pi, Demo demo, Opt opt) {
        pi.addValue(ProtocolInfo.VERSION, "demoversion");
        pi.addValue(ProtocolInfo.SESSIONID, "DemoID");
        pi.addValue(ProtocolInfo.NAME, "DemoName");
        pi.addValue(ProtocolInfo.DESCRIPTION, "DemoDescription");

        pi.addValue(ProtocolInfo.NOPARTIES, opt.getIntValue("-nopart"));
        pi.addValue(ProtocolInfo.THRESHOLD, opt.getIntValue("-thres"));

        demo.addDefaultPartyInfos(pi, opt);
    }

    /**
     * Initializes the default fields of the input private info.
     *
     * @param pi Private info.
     * @param demo Demonstraction instance.
     * @param opt Options given by the user.
     * @param j Index of party.
     */
    public void generatePrivateInfo(PrivateInfo pi, Demo demo, Opt opt, int j) {
        demo.addDefaultValues(pi, j, opt);
    }

    /**
     * Generates a command line parser.
     *
     * @param demo Demonstrator invoking this factory (allows call-backs).
     * @return Configured command line parser containing no content.
     * @throws OptException If the configuration data is extracted incorrectly.
     */
    public Opt generateOpt(Demo demo) {
        return demo.defaultOpt(this);
    }

    /**
     * Writes a private info file to <code>privateInfoFile</code>.
     *
     * @param demo Demonstrator invoking this factory (allows call-backs).
     * @param partyDir Directory of the party of which the protocol
     * info is generated.
     * @param protocolInfoFile Where the protocol info file is written.
     * @param opt Command line parser from which configuration data
     * may be extracted.
     */
    public void generateProtocolInfoFile(Demo demo,
                                         File partyDir,
                                         File protocolInfoFile,
                                         Opt opt) {
        ProtocolInfo pi = newProtocolInfo();
        generateProtocolInfo(pi, demo, opt);
        try {
            pi.toXML(protocolInfoFile);
        } catch (InfoException ie) {
            throw new ProtocolError("Could not write protocol info file!", ie);
        }
    }



    /**
     * Writes a private info file to <code>privateInfoFile</code>.
     *
     * @param demo Demonstrator invoking this factory (allows call-backs).
     * @param partyDir Directory of the party of which the private
     * info is generated.
     * @param privateInfoFile Where the private info file is written.
     * @param j Index of the party of which the private info is generated.
     * @param opt Command line parser from which configuration data
     * may be extracted.
     * @throws OptException If the configuration data is extracted incorrectly.
     */
    public void generatePrivateInfoFile(Demo demo,
                                        File partyDir,
                                        File privateInfoFile,
                                        int j,
                                        Opt opt) {
        PrivateInfo pi = newPrivateInfo();
        generatePrivateInfo(pi, demo, opt, j);
        try {
            pi.toXML(privateInfoFile);
        } catch (InfoException ie) {
            throw new ProtocolError("Could not write private info file!", ie);
        }
    }
}
