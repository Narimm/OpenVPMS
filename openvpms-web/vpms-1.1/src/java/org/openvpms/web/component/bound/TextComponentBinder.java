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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.event.DocumentListener;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.component.property.Property;


/**
 * Helper to bind a property to a text component..
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
class TextComponentBinder extends Binder {

    /**
     * The text component to bind to.
     */
    private final TextComponent _component;

    /**
     * The document update listener.
     */
    private final DocumentListener _listener;
    private final ActionListener actionListener;


    /**
     * Construct a new <code>TextComponentBinder</code>.
     *
     * @param component the component to bind
     * @param property  the property to bind
     */
    public TextComponentBinder(TextComponent component, Property property) {
        super(property);
        _component = component;

        _listener = new DocumentListener() {
            public void documentUpdate(DocumentEvent event) {
                setProperty();
            }
        };
        _component.getDocument().addDocumentListener(_listener);

        // Register an action listener to ensure document update events
        // are triggered in a timely fashion
        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // no-op.
            }
        };
        _component.addActionListener(actionListener);
    }

    /**
     * Returns the value of the field.
     *
     * @return the value of the field
     */
    protected Object getFieldValue() {
        return _component.getText();
    }

    /**
     * Sets the value of the field.
     *
     * @param value the value to set
     */
    protected void setFieldValue(Object value) {
        _component.getDocument().removeDocumentListener(_listener);
        _component.removeActionListener(actionListener);
        String text = (value != null) ? value.toString() : null;
        _component.setText(text);
        _component.getDocument().addDocumentListener(_listener);
        _component.addActionListener(actionListener);
    }
}
