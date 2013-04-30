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
 */

package org.openvpms.web.component.bound;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.DocumentListener;
import org.openvpms.web.component.property.Property;


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
     * Construct a new <code>TextComponentBinder</code>.
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
}
