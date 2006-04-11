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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.edit.CollectionProperty;
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
     * Returns a label to display a node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a label to display the node
     */
    protected Label getLabel(IMObject object, NodeDescriptor descriptor) {
        Property property = getProperty(object, descriptor);
        Label label = LabelFactory.create();
        Object value = property.getValue();
        if (value != null) {
            label.setText(value.toString());
        }
        return label;
    }

    /**
     * Returns a check box to display a node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a check box to display the node
     */
    protected Component getCheckBox(IMObject object,
                                    NodeDescriptor descriptor) {
        Property property = getProperty(object, descriptor);
        return new BoundCheckBox(property);
    }

    /**
     * Returns a text component to display a node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return a text field to display the node, or a text area if it is large
     */
    protected TextComponent getTextComponent(IMObject object,
                                             NodeDescriptor descriptor) {
        final int maxDisplayLength = 50;
        int length = descriptor.getMaxLength();
        int maxColumns = (length < maxDisplayLength) ? length : maxDisplayLength;
        return getTextComponent(object, descriptor, maxColumns);
    }

    /**
     * Returns a text component to display a node.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @param columns    the maximum no, of columns to display
     * @return a text field to display the node, or a text area if it is large
     */
    protected TextComponent getTextComponent(IMObject object,
                                             NodeDescriptor descriptor,
                                             int columns) {
        TextComponent result;
        Property property = getProperty(object, descriptor);
        if (descriptor.isLarge()) {
            result = TextComponentFactory.createTextArea(property, columns);
        } else {
            result = TextComponentFactory.create(property, columns);
        }
        return result;
    }

    /**
     * Helper to return a property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected abstract Property getProperty(IMObject object,
                                            NodeDescriptor descriptor);

    /**
     * Helper to return a collection property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected abstract CollectionProperty getCollectionProperty(
            IMObject object, NodeDescriptor descriptor);

}
