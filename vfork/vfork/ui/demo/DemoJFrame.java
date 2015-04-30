
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

package vfork.ui.demo;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import vfork.ui.*;
import vfork.ui.gui.*;

/**
 * Implements a demonstration user interface in which there is a
 * simulated embedded user interface for each party taking part in the
 * protocol.
 *
 * @author Douglas Wikstrom
 */
public class DemoJFrame extends JFrame {

    /**
     * Default width of the main window.
     */
    final static int DEFAULT_WINDOW_WIDTH = 1280;

    /**
     * Default height of the main window.
     */
    final static int DEFAULT_WINDOW_HEIGHT = 768;

    /**
     * Simulated user interfaces of all parties.
     */
    protected JTextualUI[] uiArray;

    /**
     * Creates an instance with the given number of simulated user
     * interfaces.
     *
     * @param k Number of user interfaces to simulate.
     * @param titleBar Titlebar of frame.
     */
    public DemoJFrame(int k, String titleBar) {
        super("Demonstration: " + titleBar);

        // Use standard window decoration
        setDefaultLookAndFeelDecorated(true);

        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                  screenSize.width  - inset*2,
                  screenSize.height - inset*2);

        setSize(new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));


        JDesktopPane jDesktopPane = new JDesktopPane();

        uiArray = new JTextualUI[k + 1];

        Dimension size = getSize();

        int xTotal = 0;
        int yTotal = 0;
        int xLoc = 0;
        int yLoc = 0;
         for (int i = 1; i <= k; i++) {

             uiArray[i] = new JTextualUI(i);
            uiArray[i].pack();
            Dimension internalSize = uiArray[i].getSize();
            uiArray[i].setLocation(xLoc, yLoc);
            xLoc += internalSize.width;
            xTotal = Math.max(xTotal, xLoc);
            yTotal = Math.max(yTotal, yLoc + internalSize.height);
             uiArray[i].setVisible(true);
             jDesktopPane.add(uiArray[i]);

            if (xLoc + internalSize.width > size.width) {
                xLoc = 0;
                yLoc += internalSize.height;
            }
         }

        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setPreferredSize(new Dimension(xTotal, yTotal));
        jPanel.add(jDesktopPane, BorderLayout.CENTER);

        setContentPane(new JScrollPane(jPanel,
                          ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS));

        // Make sure we die if user closes the window using the OSs
        // close button.
        addWindowListener(
          new WindowAdapter() {
              public void windowClosing(WindowEvent we) {
                  System.exit(0);
              }
          });

        pack();
    }

    /**
     * Returns the simulated user interface of the <code>i</code>th
     * party.
     *
     * @param i Index of a party.
     * @return User interface of the given party.
     */
    public UI uiAt(int i) {
        return uiArray[i];
    }
}
