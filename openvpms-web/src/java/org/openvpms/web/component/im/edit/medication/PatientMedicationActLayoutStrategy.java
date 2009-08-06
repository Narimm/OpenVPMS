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

package org.openvpms.web.component.im.edit.medication;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.layout.PrintObjectLayoutStrategy;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;


/**
 * Layout strategy that includes a 'Print Label' button to print the act.
 */
public class PatientMedicationActLayoutStrategy extends PrintObjectLayoutStrategy {

    /**
     * Determines if the date node should be displayed read-only.
     */
    private boolean showDateReadOnly;

    /**
     * Determines if the product node should be displayed. False if
     * the parent act has a product. Ignored if <tt>showProductReadOnly</tt>
     * is <tt>true</tt>
     */
    private boolean showProduct;

    /**
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly;


    /**
     * Constructs a new <tt>PatientMedicationActLayoutStrategy</tt>.
     */
    public PatientMedicationActLayoutStrategy() {
        super("button.printlabel");
    }

    /**
     * Determines if the data should be displayed read-only.
     *
     * @param readOnly if <tt>true</tt> display the date read-only.
     */
    public void setDateReadOnly(boolean readOnly) {
        showDateReadOnly = readOnly;

    }

    /**
     * Determines if the product should be displayed read-only.
     *
     * @param readOnly if <tt>true</tt> display the product read-only.
     */
    public void setProductReadOnly(boolean readOnly) {
        showProduct = true;
        showProductReadOnly = readOnly;
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <tt>Component</tt>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param context    the layout context
     * @return the component containing the rendered <tt>object</tt>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        if (!showProductReadOnly) {
            if (parent instanceof Act) {
                ActBean bean = new ActBean((Act) parent);
                showProduct = !bean.hasNode("product");
            } else {
                showProduct = true;
            }
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns a node filter to filter nodes. This implementation filters
     * out the product node if {@link #showProduct} is <tt>false</tt>.
     *
     * @param object  the object to filter nodes for
     * @param context the context
     * @return a node filter to filter nodes, or <tt>null</tt> if no filterering is required
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        NodeFilter filter;
        if (!showProduct) {
            filter = super.getNodeFilter(context, new NamedNodeFilter("product"));
        } else {
            filter = super.getNodeFilter(object, context);
        }
        return filter;
    }

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <tt>property</tt>
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent,
                                             LayoutContext context) {
        ComponentState result;
        String name = property.getName();
        if (showDateReadOnly && name.equals("startTime")) {
            result = getReadOnlyComponent(property, parent, context);
        } else if (showProductReadOnly && name.equals("product")) {
            result = getReadOnlyComponent(property, parent, context);
        } else {
            result = super.createComponent(property, parent, context);
        }
        return result;
    }

    /**
     * Helper to return a read-only component.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a read-only component to display the property
     */
    private ComponentState getReadOnlyComponent(Property property,
                                                IMObject parent,
                                                LayoutContext context) {
        ReadOnlyComponentFactory factory
                = new ReadOnlyComponentFactory(context);
        return factory.create(property, parent);
    }

}
