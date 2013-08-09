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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.DocumentEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.DocumentListener;
import org.openvpms.web.echo.text.TextComponent;


/**
 * Helper to bind a property to a text component..
 *
 * @author Tim Anderson
 */
class TextComponentBinder extends Binder {

    /**
     * The text component to bind to.
     */
    private final TextComponent component;

    /**
     * The document update listener.
     */
    private final DocumentListener listener;


    /**
     * Constructs a {@link TextComponentBinder}.
     *
     * @param component the component to bind
     * @param property  the property to bind
     */
    public TextComponentBinder(TextComponent component, Property property) {
        super(property, false);
        this.component = component;

        listener = new DocumentListener() {
            public void onUpdate(DocumentEvent event) {
                setProperty();
            }
        };
        this.component.getDocument().addDocumentListener(listener);

        // Register an action listener to ensure document update events
        // are triggered in a timely fashion
        ActionListener actionListener = new ActionListener() {
            public void onAction(ActionEvent event) {
                // no-op.
            }
        };
        this.component.addActionListener(actionListener);
        bind();
    }

    /**
     * Returns the value of the field.
     *
     * @return the value of the field
     */
    protected Object getFieldValue() {
        return component.getText();
    }

    /**
     * Sets the value of the field.
     *
     * @param value the value to set
     */
    protected void setFieldValue(Object value) {
        component.getDocument().removeDocumentListener(listener);
        String text = (value != null) ? value.toString() : null;
        component.setText(text);
        component.getDocument().addDocumentListener(listener);
    }

    /**
     * Updates the property from the field.
     * <p/>
     * This moves the cursor position by the change in length, if a macro is expanded.
     *
     * @param property the property to update
     * @return {@code true} if the property was updated
     */
    @Override
    protected boolean setProperty(Property property) {
        Object fieldValue = getFieldValue();
        String oldValue = (fieldValue != null) ? fieldValue.toString() : "";
        boolean result = property.setValue(fieldValue);
        if (result) {
            String newValue = property.getString();
            if (!ObjectUtils.equals(fieldValue, newValue)) {
                setField();
                int oldLength = (oldValue != null) ? oldValue.length() : 0;
                int newLength = (newValue != null) ? newValue.length() : 0;
                if (oldLength < newLength) {
                    int diff = newLength - oldLength;
                    component.setCursorPosition(component.getCursorPosition() + diff);
                }
            }
        }
        return result;
    }
}
