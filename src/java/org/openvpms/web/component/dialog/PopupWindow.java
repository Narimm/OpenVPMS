/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.dialog;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.TabIndexer;


/**
 * Generic popup window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class PopupWindow extends WindowPane {

    /**
     * The layout pane.
     */
    private final SplitPane _layout;

    /**
     * The button row.
     */
    private final ButtonRow _row;



    /**
     * Construct a new <code>PopupWindow</code>.
     *
     * @param title the window title
     */
    public PopupWindow(String title) {
        this(title, null, null);
    }

    /**
     * Construct a new <code>PopupWindow</code>
     *
     * @param title the window title
     * @param style the window style
     * @param indexer the tab indexer. May be <code>null</code>
     */
    public PopupWindow(String title, String style, TabIndexer indexer) {
        super(title, null, null);
        setStyleName(style);

        _row = new ButtonRow(indexer);

        _layout = new SplitPane(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                                new Extent(32));     // @todo - stylehseet
        _layout.add(_row);
        add(_layout);
    }

    /**
     * Show the window.
     */
    public void show() {
        if (getParent() == null) {
            Window root = ApplicationInstance.getActive().getDefaultWindow();
            root.getContent().add(this);
        }
    }

    /**
     * Close the window.
     */
    public void close() {
        userClose();
    }

    /**
     * Adds a listener to receive notification when the user presses a button.
     * The listener receives events from all buttons.
     *
     * @param listener the listener to add
     */
    public void addActionListener(ActionListener listener) {
        _row.addActionListener(listener);
    }

    /**
     * Adds a listener to receive notification when the user presses a specific
     * button.
     *
     * @param id       the button identifier
     * @param listener the listener to add
     */
    public void addActionListener(String id, ActionListener listener) {
        _row.addActionListener(id, listener);
    }

    /**
     * Removes an <code>ActionListener</code> from receiving notification when
     * the user presses a button.
     *
     * @param listener the listener to remove
     */
    public void removeActionListener(ActionListener listener) {
        _row.removeActionListener(listener);
    }

    /**
     * Removes an <code>ActionListener</code> from receiving notification when
     * the user presses a specific button.
     *
     * @param id       the button identifier
     * @param listener the listener to remove
     */
    public void removeActionListener(String id, ActionListener listener) {
        _row.removeActionListener(id, listener);
    }

    /**
     * Returns the layout pane.
     */
    protected SplitPane getLayout() {
        return _layout;
    }

    /**
     * Add a button.
     *
     * @param id the button identifier
     */
    protected void addButton(String id, ActionListener listener) {
        _row.addButton(id, listener);
    }

}
