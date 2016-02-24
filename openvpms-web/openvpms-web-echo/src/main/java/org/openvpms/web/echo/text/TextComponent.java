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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.text;

import nextapp.echo2.app.text.Document;
import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for text-entry components.
 * <p/>
 * This extends the echo2 text component by adding support for cursor positioning.
 *
 * @author Tim Anderson
 */
public abstract class TextComponent extends nextapp.echo2.app.text.TextComponent {

    /**
     * The cursor position property.
     */
    public static final String PROPERTY_CURSOR_POSITION = "cursorPosition";

    /**
     * The text received from the client. This only gets populated once the cursor position is received.
     */
    private String pending;

    /**
     * Determines if the cursor position has been received from the client.
     */
    private boolean haveCursorPosition;

    /**
     * Determines if the component supports cursor positioning.
     */
    private boolean supportsCursorPosition = true;

    /**
     * Determines if the text has been received from the client. Note that the text may be null.
     */
    private boolean haveText;

    /**
     * The text, prior to it being updated by {@link #processInput}. This is used to avoid sending
     * redundant updates back to the client.
     */
    private String textPreUpdate;

    /**
     * Determines if {@link #processInput} is currently being invoked.
     */
    private boolean inProcessInput = false;

    /**
     * Constructs a {@link TextComponent} with the specified {@code Document} as its model.
     *
     * @param document the desired model
     */
    public TextComponent(Document document) {
        super(document);
    }

    /**
     * Processes client input specific to the {@code Component} received from the {@code UpdateManager}.
     * <p/>
     * This implementation ensures that the {@link #PROPERTY_CURSOR_POSITION} is processed before
     * the {@link #TEXT_CHANGED_PROPERTY}. This can be received in any order, and the text may not be received at all.
     * This is required to ensure that if the listener for document updates changes the cursor position, this doesn't
     * get replaced by a value from the client.
     *
     * @param inputName  the name of the input
     * @param inputValue the value of the input
     */
    @Override
    public void processInput(String inputName, Object inputValue) {
        try {
            inProcessInput = true;
            if (TEXT_CHANGED_PROPERTY.equals(inputName)) {
                if (!supportsCursorPosition || haveCursorPosition) {
                    textPreUpdate = (String) inputValue;
                    setText(textPreUpdate);
                    haveCursorPosition = false;
                } else {
                    pending = (String) inputValue;
                    haveText = true;
                }
            } else if (PROPERTY_CURSOR_POSITION.equals(inputName)) {
                setProperty(PROPERTY_CURSOR_POSITION, inputValue);
                if (!commitPending()) {
                    haveCursorPosition = true;
                }
            } else {
                if (INPUT_ACTION.equals(inputName)) {
                    commitPending();
                    haveCursorPosition = false;
                }
                super.processInput(inputName, inputValue);
            }
        } finally {
            textPreUpdate = null;
            inProcessInput = false;
        }
    }

    /**
     * Reports a bound property change to <code>PropertyChangeListener</code>s
     * and to the <code>ApplicationInstance</code>'s update management system.
     *
     * @param propertyName the name of the changed property
     * @param oldValue     the previous value of the property
     * @param newValue     the present value of the property
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (inProcessInput && TEXT_CHANGED_PROPERTY.equals(propertyName)) {
            // don't update the client if the text hasn't changed.
            if (!StringUtils.equals(textPreUpdate, (String) newValue)) {
                super.firePropertyChange(propertyName, oldValue, newValue);
            }
        } else {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Returns the cursor position.
     *
     * @return the cursor position
     */
    public int getCursorPosition() {
        Integer value = (Integer) getProperty(PROPERTY_CURSOR_POSITION);
        return value == null ? 0 : value;
    }

    /**
     * Sets the cursor position.
     *
     * @param position the cursor position
     */
    public void setCursorPosition(int position) {
        if (position < 0) {
            setProperty(PROPERTY_CURSOR_POSITION, null);
        } else {
            setProperty(PROPERTY_CURSOR_POSITION, position);
        }
    }

    /**
     * Determines if the component supports cursor positioning.
     *
     * @param supportsCursorPosition if {@code true}, the component supports cursor positioning
     */
    protected void setSupportsCursorPosition(boolean supportsCursorPosition) {
        this.supportsCursorPosition = supportsCursorPosition;
    }

    /**
     * Commits any pending text.
     *
     * @return if there was pending text
     */
    private boolean commitPending() {
        if (haveText) {
            try {
                textPreUpdate = pending;
                setText(pending);
            } finally {
                pending = null;
                textPreUpdate = null;
                haveText = false;
            }
            return true;
        }
        return false;
    }

}
