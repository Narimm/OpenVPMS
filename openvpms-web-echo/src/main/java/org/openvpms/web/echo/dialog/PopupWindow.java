/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.dialog;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.keyboard.KeyStrokeHelper;


/**
 * Generic popup window.
 *
 * @author Tim Anderson
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
     * The focus group.
     */
    private final FocusGroup focusGroup;

    /**
     * The default button.
     */
    private String defaultButton;


    /**
     * Constructs a {@link PopupWindow}.
     *
     * @param title the window title
     */
    public PopupWindow(String title) {
        this(title, null, null);
    }

    /**
     * Constructs a {@link PopupWindow}.
     *
     * @param title the window title. May be {@code null}
     * @param style the window style. May be {@code null}
     * @param focus the focus group. May be {@code null}
     */
    public PopupWindow(String title, String style, FocusGroup focus) {
        super(title, null, null);
        if (style == null) {
            style = "PopupWindow";
        }
        setStyleName(style);
        focusGroup = new FocusGroup(getClass().getName());
        if (focus != null) {
            focusGroup.add(focus);
        }

        row = new ButtonRow(focusGroup, "DialogButtonRow", ButtonRow.BUTTON_STYLE);

        ContentPane root = new ContentPane();
        layout = createSplitPane();
        layout.add(row);
        root.add(layout);
        add(root);
    }

    /**
     * Show the window.
     */
    public void show() {
        if (getParent() == null) {
            doLayout();
            DialogManager.show(this);
        }
        if (defaultButton != null) {
            getButtons().setFocus(defaultButton);
        }
    }

    /**
     * Creates the layout split pane.
     *
     * @return a new split pane
     */
    protected SplitPane createSplitPane() {
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "PopupWindow.Layout");
    }

    /**
     * Sets the default button identifier.
     *
     * @param id the button identifier
     */
    public void setDefaultButton(String id) {
        defaultButton = id;
    }

    /**
     * Returns the default button identifier.
     *
     * @return the default button, or {@code null} if none has been set
     */
    public String getDefaultButton() {
        return defaultButton;
    }

    /**
     * Close the window.
     */
    public void close() {
        if (getParent() != null) {
            userClose();
        }
    }

    /**
     * Returns the buttons.
     *
     * @return the buttons
     */
    public ButtonSet getButtons() {
        return row.getButtons();
    }

    /**
     * Lays out the component prior to display.
     * This implementation is a no-op.
     */
    protected void doLayout() {
    }

    /**
     * Returns the layout pane.
     *
     * @return the layout pane
     */
    protected SplitPane getLayout() {
        return layout;
    }

    /**
     * Adds a button.
     *
     * @param id       the button identifier
     * @param listener the action listener
     * @return a new button
     */
    protected Button addButton(String id, ActionListener listener) {
        return addButton(id, listener, false);
    }

    /**
     * Adds a button.
     *
     * @param id              the button identifier
     * @param listener        the action listener
     * @param disableShortcut if {@code true} disable any keyboard shortcut
     * @return a new button
     */
    protected Button addButton(String id, ActionListener listener,
                               boolean disableShortcut) {
        return row.addButton(id, listener, disableShortcut);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    protected FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Notifies {@code WindowPaneListener}s that the user has requested to close this {@code WindowPane}.
     * <p/>
     * This implementation re-registers keystroke listeners as a workaround to bugs in Firefox.
     *
     * @see KeyStrokeHelper#reregisterKeyStrokeListeners
     */
    @Override
    protected void fireWindowClosing() {
        // re-register listeners for Firefox
        KeyStrokeHelper.reregisterKeyStrokeListeners();

        super.fireWindowClosing();
    }

    /**
     * Helper to set the focus.
     *
     * @param component the component to focus on
     */
    protected void setFocus(Component component) {
        FocusHelper.setFocus(component);
    }

}
