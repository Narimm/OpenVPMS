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

import java.util.Date;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.ReadOnlyProperty;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractReadOnlyComponentFactory
        extends AbstractIMObjectComponentFactory {


    /**
     * Construct a new <code>AbstractReadOnlyComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public AbstractReadOnlyComponentFactory(LayoutContext context) {
        super(context);
    }

    /**
     * Create a component to display the an object.
     *
     * @param context    the context object
     * @param descriptor the object's descriptor
     * @return a component to display <code>object</code>
     */
    public Component create(IMObject context, NodeDescriptor descriptor) {
        Component result;
        boolean enable = false;
        if (descriptor.isLookup()) {
            result = getLookup(context, descriptor);
        } else if (descriptor.isBoolean()) {
            result = getBoolean(context, descriptor);
        } else if (descriptor.isString()) {
            result = getTextComponent(context, descriptor);
        } else if (descriptor.isNumeric()) {
            result = getNumber(context, descriptor);
        } else if (descriptor.isDate()) {
            result = getDate(context, descriptor);
        } else if (descriptor.isCollection()) {
            result = getCollectionViewer(context, descriptor);
            // need to enable this otherwise table selection is disabled
            enable = true;
        } else if (descriptor.isObjectReference()) {
            result = getObjectViewer(context, descriptor);
            // need to enable this for hyperlinks to work
            enable = true;
        } else {
            Label label = LabelFactory.create();
            label.setText("No viewer for type " + descriptor.getType());
            result = label;
        }
        result.setEnabled(enable);
        result.setFocusTraversalParticipant(false);
        return result;
    }


    /**
     * Create a component to display an object.
     *
     * @param object     the object to display
     * @param context    the object's parent. May be <code>null</code>
     * @param descriptor the parent object's descriptor. May be
     *                   <code>null</code>
     */
    public Component create(IMObject object, IMObject context,
                            NodeDescriptor descriptor) {
        IMObjectViewer viewer = new IMObjectViewer(object);
        return viewer.getComponent();
    }

    /**
     * Helper to return a property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected Property getProperty(IMObject object, NodeDescriptor descriptor) {
        return new ReadOnlyProperty(object, descriptor);
    }

    /**
     * Helper to return a collection property given its descriptor.
     *
     * @param object     the object that owns the property
     * @param descriptor the property's descriptor
     * @return the property corresponding to <code>descriptor</code>.
     */
    protected CollectionProperty getCollectionProperty(
            IMObject object, NodeDescriptor descriptor) {
        return new ReadOnlyProperty(object, descriptor);
    }

    /**
     * Returns a component to display a lookup.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the lookup
     */
    protected abstract Component getLookup(IMObject context,
                                           NodeDescriptor descriptor);

    /**
     * Returns a component to display a boolean.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a componnet to display the boolean
     */
    protected Component getBoolean(IMObject context,
                                   NodeDescriptor descriptor) {
        return getCheckBox(context, descriptor);
    }

    /**
     * Returns a component to display a number.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected abstract Component getNumber(IMObject context,
                                           NodeDescriptor descriptor);

    /**
     * Returns a component to display a date.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected abstract Component getDate(IMObject context,
                                         NodeDescriptor descriptor);

    /**
     * Returns a viewer for an object.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @return an component to display the object.
     */
    protected Component getObjectViewer(IMObject parent,
                                        NodeDescriptor descriptor) {
        Property property = getProperty(parent, descriptor);
        IMObjectReference ref = (IMObjectReference) property.getValue();
        boolean link = true;
        if (getLayoutContext().isEdit()) {
            // disable hyperlinks if an edit is in progress.
            link = false;
        }
        return new IMObjectReferenceViewer(ref, link).getComponent();
    }

    /**
     * Returns a component to display a collection.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @return a collection to display the node
     */
    protected Component getCollectionViewer(IMObject parent,
                                            NodeDescriptor descriptor) {
        return new CollectionViewer(parent, descriptor);
    }

    /**
     * Helper to convert a numeric property to string.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return the string value of the property associated with
     *         <code>descriptor</code>
     */
    protected String getNumericValue(IMObject context,
                                     NodeDescriptor descriptor) {
        Property property = getProperty(context, descriptor);
        Number value = (Number) property.getValue();
        if (value != null) {
            return NumberFormatter.format(value, descriptor, false);
        }
        return null;
    }

    /**
     * Helper to convert a date value to a string.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return the string value of the property associated with
     *         <code>descriptor</code>
     */
    protected String getDateValue(IMObject context,
                                  NodeDescriptor descriptor) {
        Property property = getProperty(context, descriptor);
        Date value = (Date) property.getValue();
        return (value != null) ? DateFormatter.format(value, false) : null;
    }

}
