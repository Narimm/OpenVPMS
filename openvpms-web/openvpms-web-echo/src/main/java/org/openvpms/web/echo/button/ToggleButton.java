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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.button;

import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.button.DefaultToggleButtonModel;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.echo.event.ActionListener;

/**
 * Toggle button.
 * <p/>
 * This uses two styles to represent the default and pressed states.
 *
 * @author Tim Anderson
 */
public class ToggleButton extends AbstractButton {

    /**
     * Determines if the button is selected.
     */
    private boolean selected = false;

    /**
     * The deselected style.
     */
    private String style = "default";

    /**
     * The pressed style.
     */
    private String pressedStyle = "pressed";

    /**
     * Default constructor.
     */
    public ToggleButton() {
        setStyleName(style);
        setModel(new DefaultToggleButtonModel());
        addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                setSelected(!selected);
            }
        });
    }

    /**
     * Constructs a deselected {@link ToggleButton}.
     *
     * @param text the button text
     */
    public ToggleButton(String text) {
        this(text, false);
    }

    /**
     * Constructs a deselected {@link ToggleButton}.
     *
     * @param text     the button text
     * @param selected if {@code true}, selects the button
     */
    public ToggleButton(String text, boolean selected) {
        this();
        setText(text);
        setSelected(selected);
    }

    /**
     * Sets the default style name.
     *
     * @param styleName the style name
     */
    public void setDefaultStyle(String styleName) {
        this.style = styleName;
        setSelected(selected);
    }

    /**
     * Sets the pressed style name.
     *
     * @param styleName the pressed style name
     */
    public void setPressedStyle(String styleName) {
        this.pressedStyle = styleName;
        setSelected(selected);
    }

    /**
     * Sets the selected state of the button.
     *
     * @param selected if {@code true}, select the button, otherwise deselect it
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        setStyleName(selected ? pressedStyle : style);
    }

    /**
     * Determines if the button is selected.
     *
     * @return {@code true} if the button is selected
     */
    public boolean isSelected() {
        return selected;
    }
}
