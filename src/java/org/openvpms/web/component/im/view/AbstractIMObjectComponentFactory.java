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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;


/**
 * Abstract implementation of the {@link IMObjectComponentFactory} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectComponentFactory
        implements IMObjectComponentFactory {

    /**
     * The layout context.
     */
    private final LayoutContext _context;

    /**
     * Construct a new <code>AbstractIMObjectComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public AbstractIMObjectComponentFactory(LayoutContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        _context = context;
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return _context;
    }

    /**
     * Returns a label to display a property.
     *
     * @param property the property to display
     * @return a label to display the property
     */
    protected Label getLabel(Property property) {
        Label label = LabelFactory.create();
        Object value = property.getValue();
        if (value != null) {
            label.setText(value.toString());
        }
        return label;
    }

    /**
     * Returns a check box to display a property.
     *
     * @param property the property to display
     * @return a check box to display the property
     */
    protected Component getCheckBox(Property property) {
        return new BoundCheckBox(property);
    }

    /**
     * Returns a text component to display a property.
     *
     * @param property the property to display
     * @return a text field to display the node, or a text area if it is large
     */
    protected TextComponent getTextComponent(Property property) {
        final int maxDisplayLength = 50;
        NodeDescriptor descriptor = property.getDescriptor();
        int length = descriptor.getMaxLength();
        int maxColumns = (length < maxDisplayLength) ? length : maxDisplayLength;
        return getTextComponent(property, maxColumns);
    }

    /**
     * Returns a text component to display a property.
     *
     * @param property the property to display
     * @param columns  the maximum no, of columns to display
     * @return a text field to display the node, or a text area if it is large
     */
    protected TextComponent getTextComponent(Property property, int columns) {
        TextComponent result;
        NodeDescriptor descriptor = property.getDescriptor();
        if (descriptor.isLarge()) {
            result = TextComponentFactory.createTextArea(property, columns);
        } else {
            result = TextComponentFactory.create(property, columns);
        }
        return result;
    }

}
