
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

package verificatum.ui.info;

import verificatum.crypto.*;

/**
 * Defines the methods needed by {@link InfoTool} to allow
 * manipulation of info files for a given protocol, i.e., for every
 * protocol that is supposed to be used directly by an end user, this
 * interface should be implemented by a generator class.
 *
 * @author Douglas Wikstrom
 */
public interface InfoGenerator {

    /**
     * Returns the current protocol version.
     *
     * @return Current protocol version.
     */
    public String protocolVersion();

    /**
     * Returns a comma-separated list of protocol versions compatible
     * with this implementation.
     *
     * @return Comma-separated list of protocol versions compatible
     * with this implementation.
     */
    public String compatibleProtocolVersions();

    /**
     * Returns true if and only if the given protocol version is
     * compatible with this implementation.
     *
     * @param protocolVersion Protocol version to be tested.
     * @return True if and only if the given protocol version is
     * compatible with this implementation.
     */
    public boolean compatible(String protocolVersion);


    /**
     * Creates an instance containing all the fields needed by the
     * protocol.
     *
     * @return Instance containing all the needed fields.
     */
    public ProtocolInfo newProtocolInfo();

    /**
     * Creates an instance containing all the fields needed by the
     * protocol, and a number of default values.
     *
     * @return Instance containing all the needed fields and some
     * default values.
     */
    public ProtocolInfo defaultProtocolInfo();

    /**
     * Creates an instance containing all the fields needed by the
     * protocol.
     *
     * @return Instance containing all the needed fields.
     */
    public PrivateInfo newPrivateInfo();

    /**
     * Creates an instance containing all the fields needed by the
     * protocol, and a number of default values.
     *
     * @param protocolInfo Protocol info on which this instance is
     * based.
     * @param rs Source of randomness.
     * @return Instance containing all the needed fields and some
     * default values.
     */
    public PrivateInfo defaultPrivateInfo(ProtocolInfo protocolInfo,
                                          RandomSource rs);

    /**
     * Creates an instance containing all the fields needed by the
     * protocol.
     *
     * @param protocolInfo Protocol info on which this instance is
     * based.
     * @return Instance containing all the needed fields.
     */
    public PartyInfo newPartyInfo(ProtocolInfo protocolInfo);

    /**
     * Creates an instance containing all the fields needed by the
     * protocol, and a number of default values.
     *
     * @param protocolInfo Protocol info on which this instance is
     * based.
     * @param privateInfo Private info on which this instance is
     * based.
     * @param rs Source of randomness.
     * @return Instance containing all the needed fields and some
     * default values.
     */
    public PartyInfo defaultPartyInfo(ProtocolInfo protocolInfo,
                                      PrivateInfo privateInfo,
                                      RandomSource rs);
}
