
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

import java.util.*;

import verificatum.protocol.*;

/**
 * Factory for creating <code>PartyInfo</code> instances.
 *
 * @author Douglas Wikstrom
 */
public class PartyInfoFactory {

    /**
     * Additional fields.
     */
    ArrayList<InfoField> additionalFields;

    /**
     * Creates a factory that creates <code>PartyInfo</code> instances
     * with the given additional fields.
     *
     * @param infoFields Additional info fields.
     */
    public PartyInfoFactory(InfoField ... infoFields) {
        additionalFields = new ArrayList<InfoField>();
        addInfoFields(infoFields);
    }

    /**
     * Adds the given info fields to this instance.
     *
     * @param infoFields Additional info fields.
     */
    public void addInfoFields(InfoField ... infoFields) {
        for (int i = 0; i < infoFields.length; i++) {
            additionalFields.add(infoFields[i]);
        }
    }

    /**
     * Creates an instance of <code>PartyInfo</code>.
     *
     * @return A new instance.
     */
    public PartyInfo newInstance() {
        return new PartyInfo(additionalFields.toArray(new InfoField[0]));
    }
}
