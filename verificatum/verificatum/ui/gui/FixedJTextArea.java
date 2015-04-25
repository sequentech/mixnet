
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

package verificatum.ui.gui;

import javax.swing.*;

/**
 * Adds the ability to add a selection listener to {@link JTextArea}.
 *
 * @author Douglas Wikstrom
 */
public class FixedJTextArea extends JTextArea {

    /**
     * Listener of this instance.
     */
    protected SelectionListener sl;

    /**
     * Constructs an instance of the given size.
     *
     * @param defaultRows Default number of rows.
     * @param defaultColumns Default number of columns.
     */
    public FixedJTextArea(int defaultRows, int defaultColumns) {
        super(defaultRows, defaultColumns);
        this.sl = null;
    }

    /**
     * Adds a selection listener to this instance.
     *
     * @param sl Listener to be associated with this instance.
     */
    public void addSelectionListener(SelectionListener sl) {
        this.sl = sl;
    }

    // Documented in SelectionListener.java

    public void replaceSelection(String content) {
        if (sl != null) {
            sl.replaceSelection(content);
        }
        super.replaceSelection(content);
    }
}