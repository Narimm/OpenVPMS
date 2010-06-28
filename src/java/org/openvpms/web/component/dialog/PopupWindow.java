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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.KeyStrokeHelper;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.awt.Dimension;


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
     * The focus group.
     */
    private final FocusGroup focusGroup;

    /**
     * The default button.
     */
    private String defaultButton;


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
        setStyleName(style);
        focusGroup = new FocusGroup(getClass().getName());
        if (focus != null) {
            focusGroup.add(focus);
        }

        row = new ButtonRow(focusGroup, "DialogButtonRow",
                            ButtonRow.BUTTON_STYLE);

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
            doLayout();
            DialogManager.show(this);
            restrictDimensions();
        }
        if (defaultButton != null) {
            Button button = getButtons().getButton(defaultButton);
            if (button != null) {
                FocusHelper.setFocus(button);
            }
        }
    }

    /**
     * Restrict dialog dimensions to 70% of screen height and 90% of screen width to try and prevent dialogs from
     * exceeding screen bounds. See OVPMS-883
     */
    private void restrictDimensions() {
        Dimension size = ContextApplicationInstance.getInstance().getResolution();
        restrictSize(size.height, WindowPane.PROPERTY_HEIGHT, WindowPane.PROPERTY_POSITION_Y, 0.70);
        restrictSize(size.width, WindowPane.PROPERTY_WIDTH, WindowPane.PROPERTY_POSITION_X, 0.90);
    }

    /**
     * Restricts the size and offset of a dialog based on a factor and screen size.
     *
     * @param screenSize       the screen size, in pixels, or <tt>0</tt> if unknown
     * @param sizeProperty     the name of the size property
     * @param positionProperty the name of the position property
     * @param factor           the factor to restrict by
     */
    private void restrictSize(int screenSize, String sizeProperty, String positionProperty, double factor) {
        int size = getPixelSize(sizeProperty);
        int offset = getPixelSize(positionProperty);
        if (screenSize > 0 && size > 0) {
            int limitSize = (int) (screenSize * factor);
            if (size + offset > limitSize) {
                if (offset != 0) {
                    offset = (limitSize - size) / 2;
                    if (offset <= 0) {
                        offset = 5;
                    }
                    setProperty(positionProperty, new Extent(offset));
                }
                if (size > limitSize - offset) {
                    size = limitSize - offset;
                    setProperty(sizeProperty, new Extent(size));
                }
            }
        }
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
     * @return the default button, or <code>null</code> if none has been set
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
     * Returns the buttons.
     *
     * @return the buttons
     */
    protected ButtonSet getButtons() {
        return row.getButtons();
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
     * @param disableShortcut if <code>true</code> disable any keyboard shortcut
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
     * Notifies <tt>WindowPaneListener</tt>s that the user has requested
     * to close this <tt>WindowPane</tt>.
     * <p/>
     * This implementation re-registers keystroke listeners as a workaround
     * to bugs in Firefox.
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

    /**
     * Returns the size in pixels for the specified property.
     *
     * @param propertyName the property name
     * @return the size in pixels, or <tt>0</tt> if its not known
     */
    private int getPixelSize(String propertyName) {
        Extent result = (Extent) getRenderProperty(propertyName);
        return (result == null || result.getUnits() != Extent.PX) ? 0 : result.getValue();
    }
}
