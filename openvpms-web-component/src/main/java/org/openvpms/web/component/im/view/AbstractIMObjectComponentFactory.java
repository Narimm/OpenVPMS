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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Label;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.AbstractPropertyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.LabelFactory;


/**
 * Abstract implementation of the {@link IMObjectComponentFactory} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectComponentFactory
        extends AbstractPropertyComponentFactory
        implements IMObjectComponentFactory {

    /**
     * The layout context.
     */
    private final LayoutContext context;


    /**
     * Constructs an {@link AbstractIMObjectComponentFactory}.
     *
     * @param context the layout context.
     * @param style   the style name to use
     */
    public AbstractIMObjectComponentFactory(LayoutContext context, String style) {
        super(style);
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        this.context = context;
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return context;
    }

    /**
     * Returns a label to display a property.
     * <p/>
     * Any new lines will not be interpreted.
     *
     * @param property the property to display
     * @return a new label
     */
    protected Label createLabel(Property property) {
        return createLabel(property, false);
    }

    /**
     * Returns a label to display a property.
     *
     * @param property  the property to display
     * @param multiline if {@code true}, interprets new lines in the text
     * @return a new label
     */
    protected Label createLabel(Property property, boolean multiline) {
        Label label = LabelFactory.create(multiline);
        String value = property.getString();
        if (value != null) {
            label.setText(value);
        }
        return label;
    }

}
