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

import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.focus.FocusTree;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Styles;


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
    private final SplitPane layout;

    /**
     * The button row.
     */
    private final ButtonRow row;


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
     * @param title the window title. May be <code>null</code>
     * @param style the window style. May be <code>null</code>
     * @param focus the focus group. May be <code>null</code>
     */
    public PopupWindow(String title, String style, FocusGroup focus) {
        super(title, null, null);
        if (style == null) {
            style = "PopupWindow";
        }
        style = Styles.getStyle(WindowPane.class, style);
        setStyleName(style);
        FocusTree focusTree = new FocusTree(getClass().getName());
        if (focus != null) {
            focusTree.add(focus);
        }

        row = new ButtonRow(focusTree);

        layout = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "PopupWindow.Layout");
        layout.add(row);
        add(layout);
    }

    /**
     * Show the window.
     */
    public void show() {
        if (getParent() == null) {
            DialogManager.show(this);
        }
    }

    /**
     * Close the window.
     */
    public void close() {
        userClose();
    }

    /**
     * Returns the layout pane.
     */
    protected SplitPane getLayout() {
        return layout;
    }

    /**
     * Returns the button row.
     *
     * @return the button row
     */
    protected ButtonRow getButtonRow() {
        return row;
    }

    /**
     * Add a button.
     *
     * @param id the button identifier
     */
    protected void addButton(String id, ActionListener listener) {
        row.addButton(id, listener);
    }

}
