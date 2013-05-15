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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.layout.PrintObjectLayoutStrategy;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;

import java.util.List;


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
     * the parent act has a product. Ignored if {@code showProductReadOnly}
     * is {@code true}
     */
    private boolean showProduct;

    /**
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly;

    /**
     * A component to display usage notes. May be {@code null}.
     */
    private Component usageNotes;

    /**
     * The nodes to display.
     */
    private ArchetypeNodes nodes;


    /**
     * Constructs a {@code PatientMedicationActLayoutStrategy}.
     */
    public PatientMedicationActLayoutStrategy() {
        super("button.printlabel");
    }

    /**
     * Determines if the data should be displayed read-only.
     *
     * @param readOnly if {@code true} display the date read-only.
     */
    public void setDateReadOnly(boolean readOnly) {
        showDateReadOnly = readOnly;
    }

    /**
     * Determines if the product should be displayed read-only.
     *
     * @param readOnly if {@code true} display the product read-only.
     */
    public void setProductReadOnly(boolean readOnly) {
        showProduct = true;
        showProductReadOnly = readOnly;
    }

    /**
     * Registers a component to display usage notes.
     * <p/>
     * If set, this is displayed immediately after the simple properties.
     *
     * @param notes the usage notes. May be {@code null}
     */
    public void setUsageNotes(Component notes) {
        usageNotes = notes;
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        if (!showProductReadOnly) {
            if (parent instanceof Act) {
                ActBean bean = new ActBean((Act) parent);
                showProduct = !bean.hasNode("product");
            } else {
                showProduct = true;
            }
        }
        nodes = (showProduct) ? DEFAULT_NODES : new ArchetypeNodes().exclude("product");
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object      the object to lay out
     * @param parent      the parent object. May be {@code null}
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                  PropertySet properties, Component container, LayoutContext context) {
        super.doSimpleLayout(object, parent, descriptors, properties, container, context);
        if (usageNotes != null) {
            container.add(ColumnFactory.create("InsetX", usageNotes));
        }
    }

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display {@code property}
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
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
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return nodes;
    }

    /**
     * Helper to return a read-only component.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a read-only component to display the property
     */
    private ComponentState getReadOnlyComponent(Property property, IMObject parent, LayoutContext context) {
        ReadOnlyComponentFactory factory = new ReadOnlyComponentFactory(context);
        return factory.create(property, parent);
    }

}
